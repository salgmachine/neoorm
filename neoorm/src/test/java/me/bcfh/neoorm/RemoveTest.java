package me.bcfh.neoorm;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import jo4neo.test.BaseTest;
import junit.framework.Assert;

public class RemoveTest extends BaseTest {

	@Override
	@Test
	public void simple() {
		Parent p = new Parent();
		p.setName("parent");
		List<Child> children = new ArrayList<Child>();
		Child child1 = new Child();
		Child child2 = new Child();
		child1.setName("1");
		child2.setName("2");
		children.add(child1);
		children.add(child2);

		p.setChildren(children);

		graph.persist(p);

		p = graph.get(Parent.class).iterator().next();

		Assert.assertEquals("parent", p.getName());
		Assert.assertEquals(2, p.getChildren().size());

		Child toremove = null;

		for (Child c : p.getChildren()) {
			if (c.getName().equals("1")) {
				toremove = c;
			}
		}
		p.getChildren().remove(toremove);
		graph.persist(p);
		
		p = graph.get(Parent.class).iterator().next();
		Assert.assertEquals(1, p.getChildren().size());
		Assert.assertEquals("2", p.getChildren().iterator().next().getName());
		
	}
}
