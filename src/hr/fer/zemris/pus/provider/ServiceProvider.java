package hr.fer.zemris.pus.provider;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.server.UID;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Properties;

import hr.fer.zemris.pus.middleware.Middleware;
import hr.fer.zemris.pus.provider.entities.FileInfo;
import hr.fer.zemris.pus.provider.entities.ProviderInfo;
import hr.fer.zemris.pus.starter.ConsoleLogger;

import org.eclipse.jetty.rewrite.handler.RedirectRegexRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServiceProvider implements Middleware, Runnable {

	private int port;
	private String name;
	private String localFiles;
	private UID serverID;

	private Properties props;
	private String registerURL;
	private ConsoleLogger log;
	
	private X509Certificate registerCert;
	private X509Certificate myCert;

	public ServiceProvider(int serverPort, String name) {
		this.port = serverPort;
		this.name = name;
		this.localFiles = "./resources/" + name;
		this.serverID = new UID();
		this.log = new ConsoleLogger(name);

		try {
			this.props = new Properties();
			props.load(new FileInputStream("resources/properties"));

			registerURL = props.getProperty("registerURL");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		Server server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		server.addConnector(connector);

		//handle local resources
		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setDirectoriesListed(true);
		resource_handler.setWelcomeFiles(new String[]{ "index.html" });
		resource_handler.setResourceBase(localFiles);

		//handle GWT UI
		WebAppContext gwtHandler = new WebAppContext();
		gwtHandler.setResourceBase("./war");
		gwtHandler.setDescriptor("./war/WEB-INF/web.xml");
		gwtHandler.setContextPath("/app");
		gwtHandler.setParentLoaderPriority(true);

		//handle redirecting to GWT start page
		RewriteHandler rewrite = new RewriteHandler();
		rewrite.setRewriteRequestURI(true);
		rewrite.setRewritePathInfo(false);
		rewrite.setOriginalPathAttribute("requestedPath"); 

		RedirectRegexRule redirect = new RedirectRegexRule();
		redirect.setRegex("^((?!app).)*$");
		redirect.setReplacement("/app");
		rewrite.addRule(redirect);

		ServletContextHandler context = new ServletContextHandler();
		//		context.setContextPath("/");
		server.setHandler(context);

		context.addServlet(new ServletHolder(
				new NameServlet(name)), "/app/api/name/*");
		context.addServlet(new ServletHolder(
				new ProvidersServlet(registerURL)), "/app/api/listProviders/*");
		context.addServlet(new ServletHolder(
				new FileListServlet(registerURL)), "/app/api/listFiles/*");
		context.addServlet(new ServletHolder(
				new FileRequestServlet(localFiles, registerURL, registerCert)), "/app/api/requestFile/*");

		//add handlers
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { gwtHandler,context,rewrite});
		server.setHandler(handlers);

		//check into CentralRegister
		checkIntoRegister();
		log.log("Checked into CR");
		
		//register local files with CentralRegister
		registerLocalFiles();
		log.log("Registered my files with CR");
		
		try {
			server.setThreadPool(new QueuedThreadPool(20));
			server.start();
			server.join();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void checkIntoRegister() {
		try {
			URL checkIntoURL = new URL(registerURL + "api/add/");
			URLConnection urlConnection = checkIntoURL.openConnection();
			((HttpURLConnection)urlConnection).setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);

			DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());

			InetAddress addr = InetAddress.getLocalHost();
			String address = addr.getCanonicalHostName() + ":" + port;
			ProviderInfo info = new ProviderInfo(serverID.toString(), name, address);

			ObjectOutputStream objOut = new ObjectOutputStream(outStream);
			objOut.writeObject(info);

			objOut.flush();
			objOut.close();

			DataInputStream inStream = new DataInputStream(urlConnection.getInputStream());
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(inStream));

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerLocalFiles() {
		try {
			URL checkIntoURL = new URL(registerURL + "api/registerFiles/");
			URLConnection urlConnection = checkIntoURL.openConnection();
			((HttpURLConnection)urlConnection).setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);

			DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
			
			File localDir = new File(localFiles);
			File[] files = localDir.listFiles();
			ArrayList<FileInfo> infos = new ArrayList<FileInfo>();
			for (File f: files){
				FileInfo fi = new FileInfo(name, Long.toString(f.getTotalSpace()), f.getName());
				infos.add(fi);
			}

			ObjectOutputStream objOut = new ObjectOutputStream(outStream);
			objOut.writeObject(infos);

			objOut.flush();
			objOut.close();

			DataInputStream inStream = new DataInputStream(urlConnection.getInputStream());
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(inStream));
			String reply = reader.readLine();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void getCA() {
		try {
			URL checkIntoURL = new URL(registerURL + "api/getCA/");
			URLConnection urlConnection = checkIntoURL.openConnection();
			((HttpURLConnection)urlConnection).setRequestMethod("GET");

			ObjectInputStream inStream = new ObjectInputStream(urlConnection.getInputStream());
			registerCert = (X509Certificate)inStream.readObject();
			
			checkIntoURL = new URL(registerURL + "api/genCA/");
			urlConnection = checkIntoURL.openConnection();
			((HttpURLConnection)urlConnection).setRequestMethod("GET");
			
			inStream = new ObjectInputStream(urlConnection.getInputStream());
			SignedObject cert = (SignedObject)inStream.readObject();
			
			Signature verificationEngine = Signature.getInstance("RSA");
			if (cert.verify(registerCert.getPublicKey(), verificationEngine)){
				myCert = (X509Certificate)cert.getObject();
			}
			else {
				System.out.println("error");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		
	}
}