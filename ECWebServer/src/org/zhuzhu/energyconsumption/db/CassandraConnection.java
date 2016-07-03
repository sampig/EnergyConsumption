/*
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.db;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

/**
 * This class provides a simple connection to Cassandra.
 *
 * @author Chenfeng Zhu
 *
 */
public class CassandraConnection {

    private final static String OPENSHIFT_JBOSSEWS_IP = System.getenv("OPENSHIFT_JBOSSEWS_IP");
    private final static String NODE_IP = (OPENSHIFT_JBOSSEWS_IP == null) ? "127.0.0.1" : OPENSHIFT_JBOSSEWS_IP;
    private final static int NODE_PORT = 9042;
    private final static String KEYSPACE = "mykeyspace";

    private Cluster cluster;
    private Session session;

    public CassandraConnection() {
        this.connect(NODE_IP, NODE_PORT);
    }

    /**
     *
     * Connect to the node.
     *
     * @param node
     * @return
     */
    protected boolean connect(String node) {
        try {
            this.close();
            cluster = Cluster.builder().addContactPoint(node).build();
            Metadata metadata = cluster.getMetadata();
            System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
            for (Host host : metadata.getAllHosts()) {
                System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n", host.getDatacenter(),
                        host.getAddress(), host.getRack());
            }
            session = cluster.connect(KEYSPACE);
        } catch (Exception e) {
            System.out.println("Connection error.");
            return true;
        }
        return false;
    }

    /**
     * Close connection.
     */
    protected void close() {
        if (session != null) {
            session.close();
        }
        if (cluster != null) {
            cluster.close();
            System.out.println("Connection is closed.");
        }
    }

    /**
     * Connect to the node with the port.
     *
     * @param node
     * @param port
     */
    protected boolean connect(String node, int port) {
        try {
            this.close();
            cluster = Cluster.builder().addContactPoint(node).withPort(port).build();
            Metadata metadata = cluster.getMetadata();
            System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
            session = cluster.connect(KEYSPACE);
        } catch (Exception e) {
            System.out.println("Connection error.");
            return false;
        }
        return true;
    }

    /**
     * Connect to the node with the port and authenticated with username and password.
     *
     * @param node
     * @param port
     * @param username
     * @param password
     */
    protected boolean connect(String node, int port, String username, String password) {
        try {
            this.close();
            cluster = Cluster.builder().addContactPoint(node).withPort(port)
                    .withCredentials(username, password).build();
            Metadata metadata = cluster.getMetadata();
            System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
            session = cluster.connect(KEYSPACE);
        } catch (Exception e) {
            System.out.println("Connection error.");
            return false;
        }
        return true;
    }

    /**
     * Get session.
     *
     * @return
     */
    protected Session getSession() {
        return session;
    }

    public static void main(String... strings) {
        CassandraConnection conn = new CassandraConnection();
        // conn.connect("127.0.0.1");
        conn.connect(NODE_IP, NODE_PORT, "cassandra", "cassandra");
        conn.close();
    }

}
