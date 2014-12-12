package org.mahouteval.gui.table;

import java.util.Comparator;

public class NumberComparator<T> implements Comparator<T> {

	public int compare(T num1, T num2) {
		if (num1 instanceof Long) {
			return ((Long)num1).compareTo((Long)num2);
		} else if (num1 instanceof Double) {
			return ((Double)num1).compareTo((Double)num2);
		}
		
		return 0;
	}

}
