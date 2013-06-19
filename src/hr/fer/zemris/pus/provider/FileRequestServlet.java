package hr.fer.zemris.pus.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileRequestServlet extends HttpServlet {
	private static final long serialVersionUID = -7294037787792460626L;
	private String localFiles;
	private String registerURL;
	private X509Certificate cert;
	private X509Certificate mycert = null;

	public FileRequestServlet(String localFilesLocation, String regURL, X509Certificate ca){
		this.localFiles = localFilesLocation;
		this.registerURL = regURL;
		this.cert = ca;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String fileName = req.getParameter("name");
		String fileLocation = req.getParameter("owner");

		if (localFiles.contains(fileLocation)){
			File local = new File(localFiles+"/" + fileName);

			resp.setContentType("text/html");
			resp.setStatus(HttpServletResponse.SC_OK);
			Scanner scan = new Scanner(local);
			String contents = "";
			while(scan.hasNextLine()){
				contents += scan.nextLine() + "<br>";
			}
			resp.getWriter().println(contents);
			resp.flushBuffer();
		}else {
			
			
			//get address from register
			String targetAddress = getTargetAddress(fileLocation);
			
			URL url;
			HttpURLConnection conn;
			BufferedReader rd;
			String line;
			String fileContents = "<i>This is a remote file hosted on SP " 
					+ fileLocation + "</i><br><br>";
			try {
				
				url = new URL("http://" + targetAddress + 
						"/app/api/requestFile?name=" + fileName +
						"&owner=" + fileLocation);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line = rd.readLine()) != null) {
					fileContents += line;
				}
				rd.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			resp.setContentType("text/html");
	        resp.setStatus(HttpServletResponse.SC_OK);
	        resp.getWriter().println(fileContents);
	        resp.flushBuffer();
			
		}
	}

	private String getTargetAddress(String fileLocation) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String fileAddress = "";
		try {
			url = new URL(registerURL + "api/getAddress?target=" + fileLocation);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				fileAddress += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fileAddress;
	}

}
