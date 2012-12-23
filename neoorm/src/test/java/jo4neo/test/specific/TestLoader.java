package jo4neo.test.specific;

import jo4neo.test.BaseTest;

import org.junit.Test;
import org.neo4j.graphdb.Node;


public class TestLoader extends BaseTest {
	
	@Test
	public void basic() {
		for (Node n :neo.getAllNodes())
			graph.get(n);
	}
}
