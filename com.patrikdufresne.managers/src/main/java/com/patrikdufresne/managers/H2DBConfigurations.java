/**
 * Copyright(C) 2013 Patrik Dufresne Service Logiciel <info@patrikdufresne.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.patrikdufresne.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.h2.Driver;
import org.h2.server.TcpServer;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.dialect.H2Dialect;

public class H2DBConfigurations {

    /**
     * Used to copy a file.
     * 
     * @param sourceFile
     * @param destFile
     * @throws IOException
     */
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static Configuration create(String dbUrl, boolean create, boolean autoServer) throws MalformedURLException {
        try {
            return create(dbUrl, create, autoServer, /* readonly */false);
        } catch (MalformedURLException e) {
            throw e;
        } catch (IOException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    public static Configuration create(String dbUrl, boolean create, boolean autoServer, boolean readonly) throws IOException {
        return create(dbUrl, create, autoServer, readonly, /* copy */false);
    }

    public static Configuration create(String dbUrl, boolean create, boolean autoServer, boolean readonly, boolean copy) throws IOException {
        return create(dbUrl, create, autoServer, readonly, copy, /* lock */null);
    }

    /**
     * Create a new configuration to prepare a Managers.
     * 
     * @param dbUrl
     *            the database url. Either a file url or a jdbc url
     * @param create
     *            True to create the file
     * @param autoServer
     *            True to automatically create a server.
     * @param readonly
     *            Open the database in read only mode.
     * @param copy
     *            Open a copy of the database.
     * @param lock
     *            Define the locking mechanic: FILE, FS, SOCKET or null to use default.
     * @return the configuration for the managers.
     * @throws IOException
     */
    public static Configuration create(String dbUrl, boolean create, boolean autoServer, boolean readonly, boolean copy, String lock) throws IOException {
        if (dbUrl == null) {
            throw new IllegalArgumentException();
        }
        H2DBDatabaseUrl url = new H2DBDatabaseUrl(dbUrl);

        Configuration config = new Configuration();
        // Use H2 DB dialect
        config.setProperty(Environment.DIALECT, H2Dialect.class.getCanonicalName());

        // Use H2 DB driver
        config.setProperty(Environment.DRIVER, Driver.class.getCanonicalName());

        // Set default username
        config.setProperty(Environment.USER, "sa");//$NON-NLS-1$

        // Set default password
        config.setProperty(Environment.PASS, "");//$NON-NLS-1$

        // Set default shema
        config.setProperty(Environment.DEFAULT_SCHEMA, "PUBLIC");//$NON-NLS-1$

        // Set a driver managed connection pool (to avoid using connection pool)
        config.setProperty(Environment.CONNECTION_PROVIDER, "org.hibernate.connection.DriverManagerConnectionProvider");

        // Enable Hibernate's automatic session context management
        config.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, ThreadLocalSessionContext.class.getCanonicalName());//$NON-NLS-1$ 

        // Copy the database
        if (copy && url.isLocal()) {
            File src = new File(url.getAbsolutePath() + ".h2.db");
            File dest = File.createTempFile("planimod-temp", ".h2.db");
            copyFile(src, dest);
            dest.deleteOnExit();
            url = new H2DBDatabaseUrl(dest.getAbsolutePath());
        }

        /*
         * Rebuild the url.
         */
        StringBuilder buf = new StringBuilder();
        if (url.isLocal()) {
            buf.append("jdbc:h2://");
            buf.append(url.getAbsolutePath());
        } else {
            buf.append(url.toString());
        }
        // Add auto server
        if (autoServer) {
            buf.append(";AUTO_SERVER=TRUE");
        }
        if (create) {
            config.setProperty(Environment.HBM2DDL_AUTO, "create");//$NON-NLS-1$
        } else if (url.isLocal()) {
            buf.append(";IFEXISTS=TRUE");
            config.setProperty(Environment.HBM2DDL_AUTO, "custom-update");//$NON-NLS-1$
        } else {
            config.setProperty(Environment.HBM2DDL_AUTO, "dummy");
        }
        if (readonly) {
            buf.append(";ACCESS_MODE_DATA=r");
        }
        // Lock
        lock = System.getProperty("managers.h2db.filelock", lock);
        if (lock != null) {
            lock = lock.toUpperCase();
            if (lock.equals("FILE") || lock.equals("FS") || lock.equals("SOCKET")) {
                buf.append(";FILE_LOCK=" + lock);
            }
        }
        config.setProperty(Environment.URL, buf.toString());
        // Drop and re-create the database schema on startup
        // if (create) {
        // this.mode = CREATE;
        // } else {
        // this.mode = UPDATE;
        // }
        // Need to open a H2DB server locallysFs
        // startServer(file.getParent());
        // } else {
        // Set the connection string
        // config.setProperty(Environment.URL, );
        // this.mode = VALIDATE;
        // }
        return config;
    }

    /**
     * Returns a list of possible url to connect remotely to the h2db server.
     * 
     * @return
     */
    public static String[] getServerUrl(H2DBDatabaseUrl url) {
        if (url == null || !url.isLocal()) {
            return new String[0];
        }
        // FIXME the following is H2DB specific.
        String name = url.getAbsolutePath();
        name = name.replaceFirst("\\.h2\\.db$", "");
        name = name + ".lock.db";
        // Check if the file exists.
        File file = new File(name);
        if (!file.exists() || !file.canRead()) {
            return new String[0];
        }
        // Read the property file.
        Properties prop = new Properties();
        try {
            prop.load(new FileReader(file));
        } catch (IOException e) {
            return new String[0];
        }
        String server = prop.get("server").toString();
        String port = server.replaceFirst("^127.0.1.1:", "");
        String[] ip = getInterfaces();
        if (ip == null) {
            return new String[0];
        }
        ArrayList<String> urls = new ArrayList<String>(ip.length);
        for (int i = 0; i < ip.length; i++) {
            if (!ip[i].matches("127.0.(0|1).1")) { //$NON-NLS-1$
                urls.add(String.format("jdbc:h2:tcp://%s:%s/%s", ip[i], port, url.getName()));
            }
        }
        String a[] = new String[urls.size()];
        return urls.toArray(a);
    }

    /**
     * Return a list of network interface. Used to retrieve a list of valid ip address.
     * 
     * @return
     */
    private static String[] getInterfaces() {
        try {
            LinkedList<String> adresses = new LinkedList<String>();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                Enumeration<InetAddress> e2 = ni.getInetAddresses();

                while (e2.hasMoreElements()) {
                    // Convert to dot representation
                    InetAddress ip = e2.nextElement();
                    if (!(ip instanceof Inet4Address)) {
                        continue;
                    }
                    byte[] ipAddr = ip.getAddress();
                    String ipAddrStr = ""; //$NON-NLS-1$
                    for (int i = 0; i < ipAddr.length; i++) {
                        if (i > 0) {
                            ipAddrStr += "."; //$NON-NLS-1$
                        }
                        ipAddrStr += ipAddr[i] & 0xFF;
                    }
                    adresses.add(ipAddrStr);
                }
            }
            String[] array = new String[adresses.size()];
            return adresses.toArray(array);
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * Used to stop the H2DB server started using auto server mode.
     */
    public static void stopServer(H2DBDatabaseUrl url) {
        try {
            // Get the list of server using reflection
            Field f = TcpServer.class.getDeclaredField("SERVERS");
            f.setAccessible(true);
            Map<Integer, TcpServer> servers = (Map<Integer, TcpServer>) f.get(null);

            // Find matching server using reflection
            for (TcpServer s : servers.values()) {
                Field f2 = s.getClass().getDeclaredField("keyDatabase");
                f2.setAccessible(true);
                Object value = f2.get(s);
                if (url.getAbsolutePath().equals(value)) {
                    s.stop();
                }
            }
        } catch (Exception e) {
            // Swallow
            return;
        }
    }

}
