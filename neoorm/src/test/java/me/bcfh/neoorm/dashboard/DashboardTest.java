package me.bcfh.neoorm.dashboard;

import java.util.Arrays;

import org.junit.Test;

import jo4neo.ObjectGraph;
import jo4neo.test.BaseTest;
import junit.framework.Assert;

public class DashboardTest extends BaseTest {

	void removeItemFromColumn(String itemId, String colName, ObjectGraph og,
			PersistableDashboard db) {

		Item toremove = null;

		for (Column c : db.getColumns()) {
			if (colName.equals(c.getColumnName())) {

				for (Item i : c.getItems()) {
					if (itemId.equals(i.getWidgetId())) {
						toremove = i;
						break;
					}
				}

				c.getItems().remove(toremove);
			}
		}

		og.persist(db);

	}

	@Override
	@Test
	public void simple() {

		PersistableDashboard dashboard = new PersistableDashboard()
				.withColumns(new Column("todo").withItems(Arrays.asList(
						new Item("1", "1"), new Item("2", "2"))), new Column(
						"busy").withItems(Arrays.asList(new Item("3", "3"),
						new Item("4", "4")))

				);
		graph.persist(dashboard);

		dashboard = graph.get(PersistableDashboard.class).iterator().next();

		removeItemFromColumn("1", "todo", graph, dashboard);

		Assert.assertEquals(2, dashboard.getColumns().size());
		Assert.assertEquals(1, dashboard.getColumns().iterator().next()
				.getItems().size());

		dashboard = graph.get(PersistableDashboard.class).iterator().next();

		Assert.assertEquals(1, dashboard.getColumns().iterator().next()
				.getItems().size());
		Assert.assertEquals("2", dashboard.getColumns().iterator().next()
				.getItems().iterator().next().getName());

	}

}
