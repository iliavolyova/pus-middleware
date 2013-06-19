package hr.fer.zemris.pus.register;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class ListProvidersServlet extends HttpServlet{

	private static final long serialVersionUID = -4507372620337805361L;

	private SqlJetDb database;

	public ListProvidersServlet(SqlJetDb db){
		this.database = db;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String providers = "";
		
		try {
			providers = aggregateProviders();
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		
		resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(providers);

	}

	private String aggregateProviders() throws SqlJetException{
		database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetCursor cursor = database.getTable("providers").open();
		StringBuilder sb = new StringBuilder(
			"<table border=\"1\"> <tr><td>name</td><td>address</td><td>id</td></tr>");
		try {
			if (!cursor.eof()) {
				do {
					sb.append("<tr><td><b>" + cursor.getString("name") + "</b></td>");
					sb.append("<td>" + cursor.getString("address") + "</td>");
					sb.append("<td>" + cursor.getString("id") + "</td></tr>");
				} while(cursor.next());
			}
		} finally {
			cursor.close();
		}
		sb.append("</table>");
		return sb.toString();
	}



}
