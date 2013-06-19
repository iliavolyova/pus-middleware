package hr.fer.zemris.pus.register;

import hr.fer.zemris.pus.provider.entities.FileInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class RegisterFilesServlet extends HttpServlet{

	private static final long serialVersionUID = 3407274424468642871L;
	private SqlJetDb database;
	
	public RegisterFilesServlet(SqlJetDb db){
		this.database = db;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String sample = "";
		
		try {
			ObjectInputStream obj = new ObjectInputStream(req.getInputStream());
			ArrayList<FileInfo> infos = (ArrayList<FileInfo>)obj.readObject();
			obj.close();
		
			database.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				ISqlJetTable filesTable = database.getTable("files");
				
				for (FileInfo fi : infos){ 
					sample = fi.getName();
					filesTable.insert(fi.getId(), fi.getName(), fi.getDetails(), fi.getOwner());
				}
			}finally {
				database.commit();
			}
		} catch (SqlJetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("added the files " + sample);
	}
}
