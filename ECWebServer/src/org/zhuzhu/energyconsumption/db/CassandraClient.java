package org.zhuzhu.energyconsumption.db;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

@Deprecated
public class CassandraClient {

    public void testThrift() {
        TTransport tr = new TFramedTransport(new TSocket("localhost", 19160));
        TProtocol proto = new TBinaryProtocol(tr);
        Cassandra.Client client = new Cassandra.Client(proto);
        try {
            tr.open();
            client.set_keyspace("mykeyspace");
            SlicePredicate predicate = new SlicePredicate();
            SliceRange sliceRange = new SliceRange(ByteBuffer.wrap(new byte[0]),
                    ByteBuffer.wrap(new byte[0]), false, 10);
            // sliceRange.setStart(new byte[0]);
            // sliceRange.setFinish(new byte[0]);
            predicate.setSlice_range(sliceRange);
            System.out.println("testThrift start: " + new Date());
            ColumnParent parent = new ColumnParent("consumption");
            List<ColumnOrSuperColumn> results = client.get_slice(
                    ByteBuffer.wrap("660BF0945365A2A4".getBytes("UTF-8")), parent, predicate,
                    ConsistencyLevel.ONE);
            int size = 0;
            for (ColumnOrSuperColumn result : results) {
                Column column = result.column;
                System.out.println(toString(column.name) + " -> " + toString(column.value));
                size++;
            }
            System.out.println("testThrift end: " + new Date());
            System.out.println(size);
            tr.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String toString(ByteBuffer buffer) throws UnsupportedEncodingException {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, "UTF-8");
    }

}
