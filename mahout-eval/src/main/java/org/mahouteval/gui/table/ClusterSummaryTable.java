package org.mahouteval.gui.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

public class ClusterSummaryTable extends SingleSelectionTableBase {
	public static enum CLUSTER_SUMMAY {
		CLUSTER_SIZE, TERM_WEIGHT
	};
	
	private CLUSTER_SUMMAY clusterSummary;
	
	public ClusterSummaryTable(CLUSTER_SUMMAY clusterSummary) {
		this.clusterSummary = clusterSummary;
	}
	
	public void displayCluster(List<String> header, List<String[]> clusters) {
		super.setAutoCreateRowSorter(false);
		setModel(new ClusterSummaryTableModel(clusters, header));
		TableRowSorter trs = new TableRowSorter(getModel());
		
		NumberComparator<Long> longComparator = new NumberComparator<Long>();
		if (clusterSummary == CLUSTER_SUMMAY.CLUSTER_SIZE) {
			trs.setComparator(1, longComparator);
		} else {
			NumberComparator<Double> doubleComparator = new NumberComparator<Double>();
			trs.setComparator(1, longComparator);
			trs.setComparator(2, doubleComparator);
		}
		
		setRowSorter(trs);
	}
	
	public String[] getSelectedCluster() {
		int[] rows = getSelectedRows();
		for (int row : rows) {
			return ((ClusterSummaryTableModel) getModel()).getSelectedCluster(row);
		}
		
		return null;
	}

	public List<String[]> getAllClusters() {
		return ((ClusterSummaryTableModel)getModel()).clusters;
	}

	public void updateCluster(List<String[]> clusters) {
		ClusterSummaryTableModel dataModel = ((ClusterSummaryTableModel) getModel());
		dataModel.clusters = clusters;
	}
	
	public void clearCluster() {
		ClusterSummaryTableModel dataModel = ((ClusterSummaryTableModel) getModel());
		dataModel.clusters = new ArrayList<String[]>();
	}

	class ClusterSummaryTableModel extends AbstractTableModel {
		private List<String> header;
		private List<String[]> clusters;
		
		public ClusterSummaryTableModel(List<String[]> clusters, List<String> header) {
			this.clusters = clusters;
			this.header = header;
		}

		public int getColumnCount() {
			return header.size();
		}

		public int getRowCount() {
			return clusters.size();
		}
		
		public String[] getSelectedCluster(int rowIndex) {
			return clusters.get(rowIndex);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= clusters.size()) {
				return "";
			}
			
			String[] cluster = clusters.get(rowIndex);
			Object value = "";
			if (clusterSummary == CLUSTER_SUMMAY.CLUSTER_SIZE) {
				return columnIndex == 0 ? cluster[columnIndex] : Long.parseLong(cluster[columnIndex]);
			} else {
				if (columnIndex == 0) {
					value = cluster[columnIndex];
				} else {
					value = columnIndex == 1 ? Long.parseLong(cluster[columnIndex]) : Double.parseDouble(cluster[columnIndex]);
				}
			}
			
			return value;
		}
		
		@Override
	    public String getColumnName(int columnIndex) {
			return header.get(columnIndex);
	    }
		
	}


}

