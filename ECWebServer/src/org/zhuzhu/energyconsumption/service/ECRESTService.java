package org.zhuzhu.energyconsumption.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.zhuzhu.energyconsumption.db.ConsumptionManager;
import org.zhuzhu.energyconsumption.model.DataModel;
import org.zhuzhu.energyconsumption.model.DeviceModel;

/**
 * This class provides the RESTful web service.
 *
 * @author Chenfeng Zhu
 *
 */
@Path("/")
public class ECRESTService {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @GET
    @Path("allDevices{limit:(/limit/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceModel> getDevices(@PathParam("limit") String limit) {
        ConsumptionManager cm = new ConsumptionManager();
        List<DeviceModel> list = null;
        int limitInt = 0;
        if (limit == null || "".equals(limit)) {
            System.out.println("No limit");
        } else {
            limit = limit.split("/")[2];
            System.out.println(limit);
            try {
                limitInt = Integer.parseInt(limit);
            } catch (Exception e) {
                limitInt = 0;
            }
        }
        list = cm.queryAllDeviceModel(limitInt);
        cm.close();
        return list;
    }

    @GET
    @Path("deviceID/{did}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getData(@PathParam("did") String deviceID) {
        return getData(deviceID, null);
    }

    @GET
    @Path("deviceID/{did}/{quantity}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getData(@PathParam("did") String deviceID, @PathParam("quantity") String quantity) {
        System.out.println("Request getData start: " + new Date());
        ConsumptionManager cm = new ConsumptionManager();
        int num = -1;
        try {
            num = Integer.parseInt(quantity);
        } catch (Exception e) {
            num = -1;
        }
        if (quantity == null || num <= 0) {
            num = 60;
        }

        Date current = new Date();
        current.setTime(current.getTime() - 100l * 24 * 60 * 60 * 1000); // a fake current time in order to get data.
        Date startTime = new Date(current.getTime() - 1 * 60 * 60 * 1000); // one hour ago
        // current.setTime(current.getTime() - 1 * 60 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strStartTime = sdf.format(startTime);
        String strDate = strStartTime.substring(0, 10);
        String strTime = strStartTime.substring(11);

        DataModel dataModel = null;
        dataModel = cm.queryDataModel(deviceID, strDate, strTime);
        if (dataModel.data == null || dataModel.data.size()==0) {
            return dataModel.toString();
        }

        List<Double> list = new ArrayList<>(0);
        int count = dataModel.data.size() / num;
        for (int i = 0; i < num; i++) {
            double sum = 0;
            for (int j = 0; j < count; j++) {
                sum += dataModel.data.get(i * count + j);
            }
            list.add(((int) (1000 * sum / count)) / 1000.0);
        }
        dataModel.data = list;
        cm.close();

        System.out.println("Request getData end: " + new Date());
        return dataModel.toString();
    }

    @GET
    @Path("json/deviceID/{did}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataModel getJsonData(@PathParam("did") String deviceID) {
        System.out.println("Request getData start: " + new Date());
        ConsumptionManager cm = new ConsumptionManager();
        int num = 60;

        Date current = new Date();
        current.setTime(current.getTime() - 100l * 24 * 60 * 60 * 1000); // a fake current time in order to get data.
        Date startTime = new Date(current.getTime() - 1 * 60 * 60 * 1000); // one hour ago
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strStartTime = sdf.format(startTime);
        String strDate = strStartTime.substring(0, 10);
        String strTime = strStartTime.substring(11);

        DataModel dataModel = null;
        dataModel = cm.queryDataModel(deviceID, strDate, strTime);
        if (dataModel.data == null || dataModel.data.size()==0) {
            return dataModel;
        }

        List<Double> list = new ArrayList<>(0);
        int count = dataModel.data.size() / num;
        for (int i = 0; i < num; i++) {
            double sum = 0;
            for (int j = 0; j < count; j++) {
                sum += dataModel.data.get(i * count + j);
            }
            list.add(((int) (1000 * sum / count)) / 1000.0);
        }
        dataModel.data = list;
        cm.close();

        System.out.println("Request getData end: " + new Date());
        return dataModel;
    }

}
