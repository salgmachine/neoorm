package me.bcfh.neoorm;

import java.util.ArrayList;

import jo4neo.test.BaseTest;
import jo4neo.test.City;
import jo4neo.test.State;
import jo4neo.test.Student;
import junit.framework.Assert;

import org.junit.Test;

public class NeoormTest extends BaseTest {

	@Test
	public void testOrm() {
		NeoORM orm = new NeoORM(graph);
		Assert.assertNotNull(orm);
		State state1 = new State();
		City city1 = new City();
		city1.setName("home1");
		City city2 = new City();
		city2.setName("home2");

		Student st1 = new Student();
		Student st2 = new Student();
		Student st3 = new Student();
		Student st4 = new Student();

		st1.setName("st1");
		st2.setName("st2");
		st3.setName("st3");
		st4.setName("st4");

		city1.getStudents().add(st1);
		city1.getStudents().add(st2);
		city2.getStudents().add(st3);
		city2.getStudents().add(st4);

		state1.getCities().add(city1);
		state1.getCities().add(city2);

		orm.persist(state1);

		Assert.assertEquals(1, orm.get(State.class).size());
		Assert.assertEquals(2, orm.get(City.class).size());
		Assert.assertEquals(4, orm.get(Student.class).size());

		State fromdb = orm.getSingle(State.class);
		Assert.assertNotNull(fromdb.getCities());
		Assert.assertTrue(fromdb.getCities().contains(city1));
		Assert.assertTrue(fromdb.getCities().contains(city2));

		Assert.assertTrue(new ArrayList<City>(fromdb.getCities()).get(0)
				.getStudents().contains(st1));
		Assert.assertTrue(new ArrayList<City>(fromdb.getCities()).get(0)
				.getStudents().contains(st2));

		Assert.assertTrue(new ArrayList<City>(fromdb.getCities()).get(1)
				.getStudents().contains(st3));
		Assert.assertTrue(new ArrayList<City>(fromdb.getCities()).get(1)
				.getStudents().contains(st4));

		// remove
		new ArrayList<City>(fromdb.getCities()).get(0).getStudents()
				.remove(st1);

		Assert.assertFalse(new ArrayList<City>(fromdb.getCities()).get(0)
				.getStudents().contains(st1));
		Assert.assertTrue(new ArrayList<City>(fromdb.getCities()).get(0)
				.getStudents().contains(st2));

		// persist
		orm.persist(new ArrayList<City>(fromdb.getCities()).get(0));

		Assert.assertEquals(1, orm.get(State.class).size());
		Assert.assertEquals(2, orm.get(City.class).size());
		Assert.assertEquals(4, orm.get(Student.class).size());

		
		fromdb = orm.getSingle(State.class);
		Assert.assertNotNull(fromdb.getCities());
		Assert.assertTrue(fromdb.getCities().contains(city1));
		Assert.assertTrue(fromdb.getCities().contains(city2));

		Assert.assertFalse(new ArrayList<City>(fromdb.getCities()).get(0)
				.getStudents().contains(st1));
		Assert.assertTrue(new ArrayList<City>(fromdb.getCities()).get(0)
				.getStudents().contains(st2));

		Assert.assertTrue(new ArrayList<City>(fromdb.getCities()).get(1)
				.getStudents().contains(st3));
		Assert.assertTrue(new ArrayList<City>(fromdb.getCities()).get(1)
				.getStudents().contains(st4));
	}

}
