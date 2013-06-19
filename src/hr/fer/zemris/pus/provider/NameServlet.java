package hr.fer.zemris.pus.provider;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NameServlet extends HttpServlet{

	private static final long serialVersionUID = 1705300056764438968L;
	private String name;
	
	public NameServlet(String spName){
		this.name = spName;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(name);
        resp.flushBuffer();
        System.out.println(name);
	}
}
