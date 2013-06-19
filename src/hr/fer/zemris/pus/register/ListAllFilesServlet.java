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

public class ListAllFilesServlet extends HttpServlet{

	private static final long serialVersionUID = 5495452731294502020L;
	private SqlJetDb database;
	
	public ListAllFilesServlet(SqlJetDb db){
		this.database = db;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try {
			database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			ISqlJetCursor cursor = database.getTable("files").open();
			StringBuilder sb = new StringBuilder("");
			try {
				if (!cursor.eof()) {
					do {
						sb.append(cursor.getString("id") + "@@");
						sb.append(cursor.getString("owner") + "@@");
						sb.append(cursor.getString("description") + "@@");
						sb.append(cursor.getString("name") + "&&&");
					} while(cursor.next());
				}
			} finally {
				cursor.close();
			}
			
			resp.setContentType("text/html");
	        resp.setStatus(HttpServletResponse.SC_OK);
	        resp.getWriter().println(sb.toString());
	        resp.flushBuffer();
	        System.out.println(sb.toString());
			
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		
	}
	
}
