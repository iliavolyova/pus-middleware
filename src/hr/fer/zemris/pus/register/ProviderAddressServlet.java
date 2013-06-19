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

public class ProviderAddressServlet extends HttpServlet{

	private static final long serialVersionUID = -6980200446162575848L;
	private SqlJetDb db;
	
	public ProviderAddressServlet(SqlJetDb db){
		this.db = db;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String address = "";
		String target = req.getParameter("target");
		
		
		try {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			ISqlJetCursor cursor = db.getTable("providers").open();
			try {
				if (!cursor.eof()) {
					do {
						String name = cursor.getString("name");
						if (name.equals(target))
							address = cursor.getString("address");
					} while(cursor.next());
				}
			} finally {
				cursor.close();
			}
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(address);
        resp.flushBuffer();
	}

}
