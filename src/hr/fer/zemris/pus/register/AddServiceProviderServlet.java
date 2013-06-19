package hr.fer.zemris.pus.register;

import hr.fer.zemris.pus.provider.entities.ProviderInfo;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class AddServiceProviderServlet extends HttpServlet{

	private static final long serialVersionUID = 6539674065070622411L;
	
	private SqlJetDb database;
	
	public AddServiceProviderServlet(SqlJetDb db) throws ServletException{
		super.init();
		database = db;
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		ProviderInfo provider = null;
		
		try {
			ObjectInputStream obj = new ObjectInputStream(req.getInputStream());
			provider = (ProviderInfo)obj.readObject();
			obj.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println("adding " + provider.getName());
			database.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				ISqlJetTable providerTable = database.getTable("providers");
				providerTable.insert(provider.getId(), provider.getName(), provider.getAdress());
			}finally {
				database.commit();
			}
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		
		resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        System.out.println("added " + provider.getName() + " on " + provider.getAdress());
	}
	
}
