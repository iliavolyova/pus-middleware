package hr.fer.zemris.pus.register;

import hr.fer.zemris.pus.middleware.Crypto;
import hr.fer.zemris.pus.provider.NameServlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.servlet.ServletException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class CentralRegister implements Runnable{

	private int port;
	private Properties properties;
	private String databaseName = "./resources/db.sqlite";
	private SqlJetDb db;
	private X509Certificate ca;
	private KeyPair pair;

	public CentralRegister(int registerPort){
		port = registerPort;

		try {
			properties = new Properties();
			properties.load(new FileInputStream("./resources/properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			createCA();
			startDatabase();
			startServer();
			
		} catch (ServletException e) {
			e.printStackTrace();
		}
	} 

	private void createCA() {
		try {
			pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
			ca = Crypto.generateCertificate("CN=Test, L=London, C=GB", pair, 30, "SHA1withRSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private void startDatabase() {
		File dbFile = new File(databaseName);
		dbFile.delete();

		try {
			db = SqlJetDb.open(dbFile, true);
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				db.createTable(properties.getProperty("create_providers_table"));
				db.createTable(properties.getProperty("create_files_table"));
			}
			finally {
				db.commit();
			}
		}catch (SqlJetException e){
			e.printStackTrace();
		}
	}

	private void startServer() throws ServletException {
		Server server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		server.addConnector(connector);
		
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);
		
		context.addServlet(new ServletHolder(
				new AddServiceProviderServlet(db)), "/api/add/*");
		context.addServlet(new ServletHolder(
				new ListProvidersServlet(db)), "/api/list/*");
		context.addServlet(new ServletHolder(
				new NameServlet("")), "/app/api/name/*");
		context.addServlet(new ServletHolder(
				new RegisterFilesServlet(db)), "/api/registerFiles/*");
		context.addServlet(new ServletHolder(
				new ListAllFilesServlet(db)), "/api/listFiles/*");
		context.addServlet(new ServletHolder(
				new ProviderAddressServlet(db)), "/api/getAddress/*");
		context.addServlet(new ServletHolder(
				new GetCaServlet(ca)), "/api/getCA/*");
		context.addServlet(new ServletHolder(
				new GeneratePrivateCA(pair)), "/api/genCA/*");
		
		try {
			server.start();
			server.join();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
