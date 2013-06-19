package hr.fer.zemris.pus.client;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class UserInterface implements EntryPoint {

	private Label nameLabel;
	private HTMLPanel providersPanel;
	private HTML providersHTML;
	private CellTable<FileInfo> cellTable;
	private TextColumn<FileInfo> ownerColumn;
	private HTMLPanel fileContentsPanel;
	private HTML textHtml;

	public void onModuleLoad() {

		RootPanel rootPanel = RootPanel.get();

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		rootPanel.add(horizontalPanel);
		Label lblWelcomeToService = new Label("Welcome to Service Provider:   ");
		lblWelcomeToService.setStyleName("titlebar");
		horizontalPanel.add(lblWelcomeToService);

		nameLabel = new Label("");
		horizontalPanel.add(nameLabel);
		nameLabel.setSize("168px", "19px");
		nameLabel.setStyleName("titlebar name");

		TabPanel tabPanel = new TabPanel();

		cellTable = new CellTable<FileInfo>();
		TextColumn<FileInfo> nameColumn = new TextColumn<FileInfo>() {
			@Override
			public String getValue(FileInfo contact) {
				return contact.getName();
			}
		};
		TextColumn<FileInfo> idColumn = new TextColumn<FileInfo>() {
			@Override
			public String getValue(FileInfo contact) {
				return contact.getId();
			}
		};
		TextColumn<FileInfo> descColumn = new TextColumn<FileInfo>() {
			@Override
			public String getValue(FileInfo contact) {
				return contact.getDetails();
			}
		};
		ownerColumn = new TextColumn<FileInfo>() {
			@Override
			public String getValue(FileInfo contact) {
				return contact.getOwner();
			}
		};
		ownerColumn.setSortable(true);

		cellTable.addColumn(nameColumn, "File name");
		cellTable.addColumn(ownerColumn, "File location");
		cellTable.addColumn(descColumn, "Description");
		cellTable.addColumn(idColumn, "UID");
		
		final SingleSelectionModel<FileInfo> selectionModel = new SingleSelectionModel<FileInfo>(); 
		cellTable.setSelectionModel(selectionModel); 
		
		selectionModel.addSelectionChangeHandler(new 
		SelectionChangeEvent.Handler() { 
		        public void onSelectionChange(SelectionChangeEvent event) { 
		                FileInfo selected = (FileInfo)selectionModel.getSelectedObject(); 
		                fetchFile(selected);
		        }
		}); 

		tabPanel.add(cellTable, "All Files", false);

		providersPanel = new HTMLPanel("");
		tabPanel.add(providersPanel, "Known Providers", false);
		providersPanel.setSize("433px", "241px");
		rootPanel.add(tabPanel);
		
		textHtml = new HTML("");
		fileContentsPanel = new HTMLPanel("<b>File Contents</b>");
		fileContentsPanel.add(textHtml);
		rootPanel.add(fileContentsPanel, 10, 239);
		tabPanel.selectTab(0);

		inquireName();
		setProviders();
		showAllFiles();
	}

	private void setProviders() {
		providersHTML = new HTML();
		providersPanel.add(providersHTML);
		askServiceProviderList("/app/api/listProviders/");
	}

	private void inquireName() {
		askServiceProviderName("/app/api/name/");
	}

	private void showAllFiles() {
		askFileList("/app/api/listFiles");
	}
	
	private void fetchFile(FileInfo selected) {
		String finalURL = "http://localhost:" + Window.Location.getPort() + "/app/api/requestFile";
		String params = "?name=" + selected.getName() + "&owner=" + selected.getOwner();
		
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, finalURL+params);

		class MyCallback implements RequestCallback {

			@Override
			public void onResponseReceived(Request req, Response resp) {
				String text = resp.getText();
				textHtml.setHTML(text);
			}

			@Override
			public void onError(Request res, Throwable throwable) {}

		}
		
		MyCallback rc = new MyCallback();
		try {
			rb.sendRequest(null, rc);
		} catch (RequestException e) {
			e.printStackTrace();
		}
		
	} 

	private void askServiceProviderName(String url){
		String finalURL = "http://localhost:" + Window.Location.getPort() + url;
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, finalURL);

		class MyCallback implements RequestCallback {

			@Override
			public void onResponseReceived(Request req, Response resp) {
				nameLabel.setText(resp.getText());
			}

			@Override
			public void onError(Request res, Throwable throwable) {}

		}

		MyCallback rc = new MyCallback();
		try {
			rb.sendRequest(null, rc);
		} catch (RequestException e) {
			e.printStackTrace();
		}
	}

	private void askServiceProviderList(String url){
		String finalURL = "http://localhost:" + Window.Location.getPort() + url;
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, finalURL);

		class MyCallback implements RequestCallback {

			@Override
			public void onResponseReceived(Request req, Response resp) {
				providersHTML.setHTML(resp.getText());
				System.out.println("provids: " + resp.getText());
			}

			@Override
			public void onError(Request res, Throwable throwable) {}

		}

		MyCallback rc = new MyCallback();
		try {
			rb.sendRequest(url, rc);
		} catch (RequestException e) {
			e.printStackTrace();
		}
	}

	private void askFileList(String url){
		String finalURL = "http://localhost:" + Window.Location.getPort() + url;
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, finalURL);

		class MyCallback implements RequestCallback {

			@Override
			public void onResponseReceived(Request req, Response resp) {
				String reply = resp.getText();
				String[] files = reply.split("&&&");
				FileInfo[] infos = new FileInfo[files.length];
				
				int i = 0;
				for(String s : files){
					String[] descriptor = s.split("@@");
					infos[i] = new FileInfo(descriptor[0],descriptor[1],
							descriptor[2],descriptor[3]);
					i++;
				}

				ListDataProvider<FileInfo> dataProvider = new ListDataProvider<FileInfo>();

				dataProvider.addDataDisplay(cellTable);

				List<FileInfo> list = dataProvider.getList();
				for (FileInfo info : infos) {
					list.add(info);
				}

				ListHandler<FileInfo> columnSortHandler = new ListHandler<FileInfo>(
						list);
				columnSortHandler.setComparator(ownerColumn,
						new Comparator<FileInfo>() {
					public int compare(FileInfo o1, FileInfo o2) {
						if (o1 == o2) {
							return 0;
						}

						if (o1 != null) {
							return (o2 != null) ? o1.getOwner().compareTo(o2.getOwner()) : 1;
						}
						return -1;
					}
				});
				cellTable.addColumnSortHandler(columnSortHandler);

				cellTable.getColumnSortList().push(ownerColumn);
				
				cellTable.redraw();
			}

			@Override
			public void onError(Request res, Throwable throwable) {}

		}

		MyCallback rc = new MyCallback();
		try {
			rb.sendRequest(url, rc);
		} catch (RequestException e) {
			e.printStackTrace();
		}
	}

	private class FileInfo implements Serializable{

		private static final long serialVersionUID = 6759425377115924022L;

		private String id;
		private String owner;
		private String details;
		private String name;

		public FileInfo(String uid, String owner, String details, String name) {
			this.id = uid;
			this.owner = owner;
			this.details = details;
			this.name = name;
		}

		public String getId() {
			return id;
		}
		public String getOwner() {
			return owner;
		}
		public String getDetails() {
			return details;
		}
		public String getName() {
			return name;
		}
	}
}
