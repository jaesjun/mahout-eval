package org.mahouteval.gui.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.mahouteval.model.Document;

public class DocumentTable extends SingleSelectionTableBase {
	public void displayDocument(List<String> header, List<Document> documents) {
		super.setAutoCreateRowSorter(false);
		setModel(new DocumentTableModel(documents, header));
		getColumnModel().getColumn(0).setPreferredWidth(100);
		getColumnModel().getColumn(0).setMaxWidth(600);
		getColumnModel().getColumn(1).setPreferredWidth(150);
		getColumnModel().getColumn(1).setMaxWidth(150);
		getColumnModel().getColumn(2).setPreferredWidth(400);
		getColumnModel().getColumn(2).setMaxWidth(400);
		
		TableRowSorter trs = new TableRowSorter(getModel());
		setRowSorter(trs);
	}
	
	public Document getSelectedDocument() {
		int[] rows = getSelectedRows();
		for (int row : rows) {
			return ((DocumentTableModel) getModel()).getSelectedDocument(row);
		}
		
		return null;
	}

	public List<Document> getAllDocuments() {
		return ((DocumentTableModel)getModel()).documents;
	}

	public void updateCluster(String clusterId, List<String> docIds) {
		DocumentTableModel dataModel = ((DocumentTableModel) getModel());
		for (String docId : docIds) {
			List<String> clusters = dataModel.clusterMap.get(docId);
			if (clusters == null) {
				clusters = new ArrayList<String>();
				dataModel.clusterMap.put(docId, clusters);
			}
			
			clusters.add(clusterId);
		}
	}
	
	public void clearCluster() {
		DocumentTableModel dataModel = ((DocumentTableModel) getModel());
		dataModel.clusterMap.clear();
	}

	class DocumentTableModel extends AbstractTableModel {
		private List<Document> documents;
		private List<String> header;
		private Map<String, List<String>> clusterMap = new HashMap<String, List<String>>();
		
		public DocumentTableModel(List<Document> documents, List<String> header) {
			this.documents = documents;
			this.header = header;
		}

		public int getColumnCount() {
			return header.size();
		}

		public int getRowCount() {
			return documents.size();
		}
		
		public Document getSelectedDocument(int rowIndex) {
			return documents.get(rowIndex);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Document document = documents.get(rowIndex);
			String column = "";
			
			if (columnIndex == 0) {
				List<String> clusters = clusterMap.get(document.getId());
				if (clusters != null && clusters.size() > 0) {
					column = clusters.toString();
				}
			} else if (columnIndex == 1){
				column = document.getId();
			} else if (columnIndex == 2) {
				column = document.getTitle();
			} else {
				column = document.getContents();
			}
				
			return column;
		}
		
		@Override
	    public String getColumnName(int columnIndex) {
			return header.get(columnIndex);
	    }
		
	}


}
