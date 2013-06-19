package hr.fer.zemris.pus.register;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetCaServlet extends HttpServlet{
	private static final long serialVersionUID = -7792311572095719529L;
	private X509Certificate cert;
	
	public GetCaServlet(X509Certificate ca){
		this.cert = ca;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());
		oos.writeObject(cert);
		oos.flush();
		oos.close();
		
	}
}
