package org.mahouteval.gui.table;

import java.util.List;

import javax.swing.RowSorter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.mahouteval.model.User;

public class UserTable extends SingleSelectionTableBase {
	
	public void displayUser(List<String> header, List<User> users) {
		super.setAutoCreateRowSorter(false);
		UserTableModel tableModel = new UserTableModel(users, header);
		setModel(tableModel);
		getColumnModel().getColumn(0).setMaxWidth(45);
		
		TableRowSorter<UserTableModel> trs = new TableRowSorter<UserTableModel>(tableModel);
		NumberComparator<Long> numberComparator = new NumberComparator<Long>();
		trs.setComparator(0, numberComparator);
		setRowSorter(trs);
	}

	public User getSelectedUser() {
		return ((UserTableModel) getModel()).getSelectedUser(getSelectedRow());
	}

	public List<User> getAllUsers() {
		return ((UserTableModel)getModel()).users;
	}

	class UserTableModel extends AbstractTableModel {
		private List<User> users;
		private List<String> header;
		
		public UserTableModel(List<User> users, List<String> header) {
			this.users = users;
			this.header = header;
		}

		public int getColumnCount() {
			return header.size();
		}

		public int getRowCount() {
			return users.size();
		}
		
		private User getSelectedUser(int rowIndex) {
			RowSorter<TableModel> trs = (RowSorter<TableModel>) UserTable.this.getRowSorter();
			return users.get(trs.convertRowIndexToModel(rowIndex));
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			User user = users.get(rowIndex);
			Object column = "";
			
			if (columnIndex == 0) {
				column = user.getId();
			} else {
				column = user.getColumns().get(columnIndex);
			}
				
			return column;
		}
		
		@Override
	    public String getColumnName(int columnIndex) {
			return header.get(columnIndex);
	    }

	}


}
