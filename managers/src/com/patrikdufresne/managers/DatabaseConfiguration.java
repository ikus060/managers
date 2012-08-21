package com.patrikdufresne.managers;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.h2.tools.Server;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

/**
 * This class is used to define the database configuration.
 * 
 * @author patapouf
 * 
 */
public class DatabaseConfiguration extends Configuration implements
		SessionFactoryObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6930279004819582903L;

	/**
	 * Dialect to used.
	 */
	private Dialect dialect;

	/**
	 * A H2db server instance.
	 */
	private Server server;

	/**
	 * True if the database need to be updated.
	 */
	private DatabaseMetadata update;

	private IDatabaseUpdater updater;

	/**
	 * Define the url to open by this database configuration
	 */
	private DatabaseUrl url;

	/**
	 * Database meta data to valide.
	 */
	private DatabaseMetadata validate;

	/**
	 * Create a new database updater.
	 */
	public DatabaseConfiguration() {
		// Setup to open a H2 database file
		this.setProperty(Environment.DIALECT,
				H2Dialect.class.getCanonicalName());
		this.setProperty(Environment.DRIVER, "org.h2.Driver"); //$NON-NLS-1$ 
		this.setProperty(Environment.USER, "sa");//$NON-NLS-1$ 
		this.setProperty(Environment.PASS, "");//$NON-NLS-1$ 
		this.setProperty(Environment.DEFAULT_SCHEMA, "PUBLIC");//$NON-NLS-1$ 

		// Enable Hibernate's automatic session context management
		this.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS,
				"org.hibernate.context.ThreadLocalSessionContext");//$NON-NLS-1$ 

		// Display SQL statement
		this.setProperty(Environment.SHOW_SQL, "true");//$NON-NLS-1$

		// Set a session listener
		setSessionFactoryObserver(this);
	}

	/**
	 * This implementation determine if the h2db need to be started.
	 */
	@Override
	public SessionFactory buildSessionFactory() throws HibernateException {

		// Test the url
		try {
			this.url.test();
		} catch (IOException e) {
			throw new HibernateException(e);
		}

		if (this.url.isLocal()) {
			// Get the file location
			File file = this.url.localfile();
			this.setProperty(Environment.URL,
					"jdbc:h2:tcp://localhost/" + this.url.getName()); //$NON-NLS-1$

			// Drop and re-create the database schema on startup
			if (!file.exists()) {
				this.setProperty(Environment.HBM2DDL_AUTO, "create");//$NON-NLS-1$ 			
			} else {
				this.setProperty(Environment.HBM2DDL_AUTO, "update");//$NON-NLS-1$
			}

			// Need to open a H2DB server locallysFs
			startServer(file.getParent());
		} else {
			// Set the connection string
			this.setProperty(Environment.URL, this.url.toString());
			//
			this.setProperty(Environment.HBM2DDL_AUTO, "validate");//$NON-NLS-1$
		}

		try {
			SessionFactory factory = super.buildSessionFactory();
			return factory;
		} catch (Exception e) {
			// If the server got started, let stop it
			stopServer();
			throw new HibernateException(e);
		}
	}

	/**
	 * This implementation update the database manually without using hmdb2dll.
	 */
	@Override
	public String[] generateSchemaUpdateScript(Dialect dialect,
			DatabaseMetadata databaseMetadata) throws HibernateException {
		this.dialect = dialect;
		this.update = databaseMetadata;
		return new String[0];

	}

	/**
	 * Return the database used by this database configuration.
	 * 
	 * @return a database url or null
	 */
	public DatabaseUrl getDatabaseUrl() {
		return this.url;
	}

	/**
	 * Return a list of network interface. Used to retrieve a list of valid ip
	 * address.
	 * 
	 * @return
	 */
	private String[] getInterfaces() {
		try {
			List<String> adresses = new LinkedList<String>();
			Enumeration<NetworkInterface> e = NetworkInterface
					.getNetworkInterfaces();

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
	 * Returns a list of possible url to connect remotely to the h2db server.
	 * 
	 * @return
	 */
	public String[] getServerUrl() {
		if (this.server != null && this.url != null
				&& this.url.getName() != null) {
			String[] ip = getInterfaces();
			if (ip != null) {
				ArrayList<String> urls = new ArrayList<String>(ip.length);
				for (int i = 0; i < ip.length; i++) {
					if (!ip[i].matches("127.0.(0|1).1")) { //$NON-NLS-1$
						urls.add(String
								.format("jdbc:h2:tcp://%s/%s", ip[i], this.url.getName())); //$NON-NLS-1$
					}
				}
				String a[] = new String[urls.size()];
				return urls.toArray(a);
			}
		}
		return new String[0];
	}

	/**
	 * Return true if the server is running.
	 * 
	 * @return
	 */
	private boolean isServerRunning() {
		if (this.server != null) {
			return this.server.isRunning(true);
		}
		return false;
	}

	/**
	 * This implementation close the h2db server.
	 */
	@Override
	public void sessionFactoryClosed(SessionFactory factory) {
		// Close the database
		stopServer();
	}

	/**
	 * This implementation does nothing.
	 */
	@Override
	public void sessionFactoryCreated(SessionFactory factory) {
		// Do validation
		if (this.validate != null) {
			validateDatabase(factory, this.dialect, this.validate);
		}
		// Do update
		if (this.update != null) {
			updateDatabase(factory, this.dialect, this.update);
		}
	}

	/**
	 * Sets the database url to open.
	 * 
	 * @param url
	 * @throws IOException
	 */
	public void setDatabaseUrl(DatabaseUrl url) {
		this.url = url;
	}

	/**
	 * Add a database updater.
	 * 
	 * @param updater
	 */
	public void setUpdater(IDatabaseUpdater updater) {
		this.updater = updater;
	}

	/**
	 * Start the h2db server.
	 * 
	 * @param baseDir
	 */
	private void startServer(String baseDir) {
		if (isServerRunning()) {
			throw new RuntimeException("Already started"); //$NON-NLS-1$
		}

		try {
			// List available arguments : java -cp h2*.jar org.h2.tools.Server
			// -?
			// -tcpAllowOthers : to allow other computer to connect
			this.server = Server.createTcpServer(
					new String[] { "-tcpAllowOthers", "-baseDir", baseDir }) //$NON-NLS-1$ //$NON-NLS-2$
					.start();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stop the h2db server
	 */
	private void stopServer() {
		if (!isServerRunning()) {
			throw new RuntimeException("Server not running"); //$NON-NLS-1$
		}
		this.server.stop();
	}

	private void updateDatabase(SessionFactory factory, final Dialect dialect,
			DatabaseMetadata metadata) {
		if (this.updater == null) {
			return;
		}
		if (!this.updater.needUpdate(metadata)) {
			return;
		}
		this.updater.runUpdate(factory, dialect, metadata);
	}

	/**
	 * Validate the database metadata.
	 * 
	 * @param factory
	 */
	private void validateDatabase(SessionFactory factory, Dialect dialect,
			DatabaseMetadata metadata) {
		if (this.updater.needUpdate(metadata)) {
			throw new HibernateException("Database need to be updated."); //$NON-NLS-1$
		}
		// The database is valid.
	}

	/**
	 * This implementation check if the shema is valid.
	 */
	@Override
	public void validateSchema(Dialect dialect,
			DatabaseMetadata databaseMetadata) throws HibernateException {
		this.dialect = dialect;
		this.validate = databaseMetadata;
	}

}
