package me.bcfh.neoorm.dashboard;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

import jo4neo.Nodeid;
import jo4neo.neo;

public class PersistableDashboard extends NeoEntity {

	public transient Nodeid neo;

	@neo(value = "dashboard_has_name", index = true)
	private String name;
	@neo
	public Collection<Column> columns;

	// public void removeItemFromColumn(String itemId, String colName) {
	// log.info("item : " + itemId + " colname : " + colName);
	// log.info("contains : "
	// + this.getNamedColumn(colName).getItems()
	// .contains(new Item(itemId, "")));
	//
	// Column c = getNamedColumn(colName);
	// LinkedList<Item> items = new LinkedList<>(c.getItems());
	//
	// for (Item i : items) {
	// if (i.getWidgetId().equals(itemId)) {
	// log.info("item removed : " + items.remove(i));
	// break;
	// }
	// }
	//
	// c.setItems(items);
	// }

	public Item getItemByWidgetId(String id) {
		for (Column c : getColumns()) {
			for (Item i : c.getItems()) {
				if (i.getWidgetId() != null) {
					if (i.getWidgetId().equals(id)) {
						return i;
					}
				}
			}
		}
		return null;
	}

	public Column getNamedColumn(String s) {
		if (columns != null) {
			for (Column c : columns) {
				if (c.getColumnName() != null && c.getColumnName().equals(s)) {
					return c;
				}
			}
		} else {
			try {
				Field[] fields = this.getClass().getDeclaredFields();
				for (Field f : fields) {
					if (f.getName().toLowerCase().contains(s)) {
						return ((Column) f.get(this));
					}
				}

			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		return null;
	}

	public String getName() {
		return name;
	}

	public PersistableDashboard withName(String name) {
		this.name = name;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Column> getColumns() {
		return columns;
	}

	public PersistableDashboard withColumns(Column... columns) {

		if (this.columns == null)
			this.columns = new LinkedList<Column>();

		for (Column c : columns) {
			this.columns.add(c);
		}
		return this;
	}

	public void setColumns(Collection<Column> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "PersistableDashboard [name=" + name + ", columns=" + columns
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columns == null) ? 0 : columns.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		PersistableDashboard other = (PersistableDashboard) obj;
		if (columns == null) {
			if (other.columns != null)
				return false;
		} else if (!columns.equals(other.columns))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
