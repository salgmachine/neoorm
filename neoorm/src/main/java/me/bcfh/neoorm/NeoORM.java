package me.bcfh.neoorm;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import jo4neo.ObjectGraph;
import jo4neo.ObjectGraphFactory;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Neo4J Tooling Container Class<br/>
 * This class wraps the ObjectGraph of jo4neo
 * */
@Stateless
@LocalBean
public class NeoORM {

	private Long timestamp = System.currentTimeMillis();

	private ObjectGraph objectGraph;

	private GraphDatabaseService svc;

	GraphDatabaseService getSvc() {
		return svc;
	}

	void setSvc(GraphDatabaseService svc) {
		this.svc = svc;
	}

	private Node refnode;

	/**
	 * Executes a Cypher Query String<br/>
	 * This can be a written String or a constructed Query from CypherDSL
	 * */
	public ExecutionResult executeQuery(String str) {
		return new ExecutionEngine(svc).execute(str);
	}

	@PreDestroy
	protected void preDestroy() {
		svc.shutdown();
	}

	public ObjectGraph getObjectGraph() {
		return objectGraph;
	}

	void setObjectGraph(ObjectGraph objectGraph) {
		this.objectGraph = objectGraph;
	}

	@PostConstruct
	protected void init() {
		// System.out.println("postconstruct on neoORm called");
		// System.out.println("og is  " + og);
	}

	public void test() {
		System.err.println("test");
		System.out.println("Object graph is " + objectGraph);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
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
		NeoORM other = (NeoORM) obj;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

}
