/*
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.zhuzhu.energyconsumption.model.DataModel;
import org.zhuzhu.energyconsumption.model.DeviceModel;
import org.zhuzhu.energyconsumption.utils.CommonUtils;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/**
 * This class is used to manipulate data in Consumption.
 *
 * @author Chenfeng Zhu
 *
 */
public class ConsumptionManager {

    private final static String KEYSPACE = "mykeyspace";
    private final static String TABLE_CONSUMPTION = "consumption";

    private final static String COLUMN_DEVICEID = "deviceid";
    private final static String COLUMN_DATE = "date";
    private final static String COLUMN_TIME = "time";
    private final static String COLUMN_POWER = "power";

    private CassandraConnection connection;
    private Session session;

    public ConsumptionManager() {
        connection = new CassandraConnection();
        session = connection.getSession();
    }

    /**
     * Query data according to device ID. Only get the data in last one hour.
     *
     * @param deviceID the Device ID
     * @param quantity the quantity of data in one hour
     * @return a DataModel
     */
    public DataModel queryDataModel(String deviceID, int quantity) {
        Date current = new Date();
        // current.setTime(current.getTime() - 1 * 60 * 60 * 1000); // one hour ago
        Date startTime = new Date(current.getTime() - 1 * 60 * 60 * 1000); // one hour ago
        int diff = 1 * 60 * 60 * 1000 / quantity;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strStartTime = sdf.format(startTime);
        String strDate = strStartTime.substring(0, 10);
        String strTime = strStartTime.substring(11);
        Statement select = QueryBuilder.select().all().from(KEYSPACE, TABLE_CONSUMPTION)
                .where(QueryBuilder.eq(COLUMN_DEVICEID, deviceID))
                .and(QueryBuilder.gte(Arrays.asList(COLUMN_DATE, COLUMN_TIME), Arrays.asList(strDate, strTime)))
                .orderBy(QueryBuilder.asc(COLUMN_DATE), QueryBuilder.asc(COLUMN_TIME));
        ResultSet results = session.execute(select);
        DataModel dm = new DataModel();
        List<Double> list = new ArrayList<>(0);
        Date timeTag = new Date(startTime.getTime());
        List<Row> rows = results.all();
        System.out.println(rows.size());
        for (int i = 0; i<rows.size();) {
            double sum = 0;
            int j = 0;
            for (j = 0;; i++, j++) {
                Row row = rows.get(i);
                Date date = null;
                try {
                    date = sdf.parse(row.getString(COLUMN_DATE) + " " + row.getString(COLUMN_TIME));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (timeTag.before(date)) {
                    break;
                }
                sum += row.getDouble(COLUMN_POWER);
                timeTag.setTime(timeTag.getTime() + diff);
            }
            list.add(sum / (j + 1));
        }
        if (list.size() > 0) {
            dm.data = list;
            dm.deviceID = deviceID;
        }
        return dm;
    }

    /**
     *
     * Query data according to device ID. Only get the data in last one hour.
     *
     * @param deviceID the Device ID
     * @param strDate
     * @param strTime
     * @return a DataModel
     */
    public DataModel queryDataModel(String deviceID, String strDate, String strTime) {
        if (session == null) {
            return null;
        }
        System.out.println("queryDataModel() start:"+ new Date());
        Statement select = QueryBuilder.select().all().from(KEYSPACE, TABLE_CONSUMPTION)
                .where(QueryBuilder.eq(COLUMN_DEVICEID, deviceID))
                .and(QueryBuilder.gte(Arrays.asList(COLUMN_DATE, COLUMN_TIME), Arrays.asList(strDate, strTime)))
                .orderBy(QueryBuilder.asc(COLUMN_DATE), QueryBuilder.asc(COLUMN_TIME));
        ResultSet results = session.execute(select);
        DataModel dm = new DataModel();
        List<Double> list = new ArrayList<>(0);
        for (Row row : results) {
            // String datetime = row.getString(COLUMN_DATE) + " " + row.getString(COLUMN_TIME);
            list.add(new Double(row.getDouble(COLUMN_POWER)));
        }
        System.out.println("queryDataModel() end:" + new Date());
        if (list.size() > 0) {
            dm.data = list;
            dm.deviceID = deviceID;
        }
        return dm;
    }

    /**
     * Get all devices.
     *
     * @param limit
     * @return a list of all devices
     */
    public List<DeviceModel> queryAllDeviceModel(int limit) {
        if (session == null) {
            return null;
        }
        List<DeviceModel> list = new ArrayList<>(0);
        String sql = "SELECT distinct " + COLUMN_DEVICEID + " FROM " + TABLE_CONSUMPTION;
        if (limit > 0) {
            sql += " LIMIT " + limit + ";";
        } else {
            sql += ";";
        }
        ResultSet results = session.execute(sql);
        for (Row row : results) {
            DeviceModel dm = new DeviceModel();
            dm.deviceID = row.getString(COLUMN_DEVICEID);
            list.add(dm);
        }
        return list;
    }

    /**
     * Close the connection.
     */
    public void close() {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Query data.
     *
     * @param deviceId
     * @param limits
     * @return
     */
    @Deprecated
    protected DataModel queryData(String deviceId, int limits) {
        if (session == null) {
            return null;
        }
        Date current = new Date();
        current.setTime(current.getTime() - 100 * 24 * 60 * 60 * 1000); // a fake current time in order to get data.
        current.setTime(current.getTime() - 1 * 60 * 60 * 1000); // one hour ago
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String currentStr = sdf.format(current);
        String dateStr = currentStr.substring(0, 10);
        String timeStr = currentStr.substring(11);
        Statement select = QueryBuilder.select().all().from(KEYSPACE, TABLE_CONSUMPTION)
                .where(QueryBuilder.eq(COLUMN_DEVICEID, deviceId))
                .and(QueryBuilder.gte(Arrays.asList(COLUMN_DATE, COLUMN_TIME), Arrays.asList(dateStr, timeStr)))
                .orderBy(QueryBuilder.desc(COLUMN_DATE), QueryBuilder.desc(COLUMN_TIME))
                .limit(limits);
        ResultSet results = session.execute(select);
        DataModel dm = new DataModel();
        dm.deviceID = deviceId;
        List<Double> list = new ArrayList<>(0);
        for (Row row : results) {
            // String datetime = row.getString(COLUMN_DATE) + " " + row.getString(COLUMN_TIME);
            // map.put(datetime, row.getDouble(COLUMN_POWER));
            list.add(row.getDouble(COLUMN_POWER));
        }
        dm.data = list;
        return dm;
    }

    /**
     * Query data.
     */
    @Deprecated
    protected void queryData() {
        if (session == null) {
            System.out.println("null");
            return;
        }
        System.out.println("queryData() Start: " + new Date());
        ResultSet results = session.execute("SELECT * FROM mykeyspace.consumption;");
        for (Row row : results) {
            row.getDouble(COLUMN_POWER);
            //            System.out.println(String.format("%-30s\t%-20s\t%-20s", row.getString(COLUMN_DEVICEID),
            //                    row.getString(COLUMN_DATE) + " " + row.getString(COLUMN_TIME),
            //                    row.getDouble(COLUMN_POWER)));
        }
        System.out.println("queryData() End: " + new Date());
    }

    /**
     * Generate some testing data.
     */
    @Deprecated
    protected void loadData() {
        if (session == null) {
            return;
        }
        DataModel dm = DataModel.generateInstance(CommonUtils.getRandomHex(16));
        String sql = "INSERT INTO " + TABLE_CONSUMPTION + " (deviceId, date, time, power)"
                + "VALUES (?,?,?,?);";
        // PreparedStatement statement = session.prepare(sql);
        BatchStatement batch = new BatchStatement();
        // BoundStatement boundStatement = new BoundStatement(statement);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println("Start: " + date);
        int size = CommonUtils.FREQUENCY * 60 * 60;
        PreparedStatement[] ps = new PreparedStatement[size];
        for (int i = 0; i < size; i++) { // 1/s * 60s/min * 60min/h * 1h
            // map.put(formatter.format(date), CommonUtils.getRandomValue(start, end));
            ps[i] = session.prepare(sql);
            String datetime = sdf.format(date);
            batch.add(ps[i].bind(dm.deviceID, datetime.substring(0, 10), datetime.substring(11),
                    dm.data.get(i)));
            // session.execute(boundStatement.bind(dm.deviceID, datetime.substring(0, 10),
            // datetime.substring(11), dm.data.get(i)));
            date.setTime(date.getTime() - 100);
        }
        // for (Entry<String, Double> e : dm.data.entrySet()) {
        // String datetime = e.getKey();
        // session.execute(boundStatement.bind(dm.deviceID, datetime.substring(0, 10),
        // datetime.substring(11), e.getValue()));
        // }
        session.execute(batch);
        System.out.println("End: " + (new Date()));
        ResultSet results = session.execute("SELECT * FROM mykeyspace.consumption;");
        System.out.println("rows: " + results.all().size());
    }

    public static void main(String... strings) {
        ConsumptionManager cm = new ConsumptionManager();
        cm.loadData();
        cm.queryData();
        cm.close();
    }

}
