package me.bcfh.neoorm.dashboard;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;

import jo4neo.Nodeid;
import jo4neo.neo;

public class Column extends NeoEntity {

	@neo
	private int hPosition;

	@neo
	private String columnName;

	@neo
	private Collection<Item> items;

	public Column() {
	}

	public Column(String columnName, Collection<Item> items) {
		super();
		this.columnName = columnName;
		this.items = items;
	}

	public Column(String columnName) {
		super();
		this.columnName = columnName;
	}

	public Collection<Item> getItems() {
		return items;
	}

	public Column(int hPosition, String columnName) {
		super();
		this.hPosition = hPosition;
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Column withItems(Collection<Item> items) {
		this.items = items;
		return this;
	}

	public void setItems(Collection<Item> items) {
		this.items = items;
	}

	public int gethPosition() {
		return hPosition;
	}

	public void sethPosition(int hPosition) {
		this.hPosition = hPosition;
	}

	@Override
	public String toString() {
		return "Column [hPosition=" + hPosition + ", columnName=" + columnName
				+ ", items=" + items + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Column other = (Column) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		return true;
	}

}
