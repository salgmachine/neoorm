package me.bcfh.neoorm;

import java.util.HashMap;
import java.util.Map;

import jo4neo.ObjectGraph;
import jo4neo.ObjectGraphFactory;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;

public class GraphDatabaseProducer {

	private Map<String, GraphDatabaseService> graphDBs = new HashMap<String, GraphDatabaseService>();

	private static Map<String, Node> refNodes = new HashMap<String, Node>();

	private GraphDatabaseService getExisting(String key) {
		return graphDBs.get(key);
	}

	public EmbeddedReadOnlyGraphDatabase getReadOnlyInstance(String dbPath) {
		if (graphDBs.containsKey(dbPath)) {
			GraphDatabaseService svc = graphDBs.get(dbPath);
			if (svc != null) {
				return (EmbeddedReadOnlyGraphDatabase) svc;
			}
		}
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(dbPath).newGraphDatabase();
		graphDBs.put(dbPath, graphDb);
		refNodes.put(dbPath, graphDb.getReferenceNode());
		registerShutdownHook(graphDb);
		EmbeddedReadOnlyGraphDatabase ro = (EmbeddedReadOnlyGraphDatabase) graphDb;
		return ro;
	}

	public GraphDatabaseService getInstance(String dbPath) {

		if (graphDBs.containsKey(dbPath)) {
			GraphDatabaseService svc = graphDBs.get(dbPath);
			if (svc != null) {
				return svc;
			}
		}
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(dbPath).newGraphDatabase();

		graphDBs.put(dbPath, graphDb);
		registerShutdownHook(graphDb);
		return graphDb;
	}

	public GraphDatabaseService getConfiguredInstance(String dbPath,
			String propertiesPath) {
		if (graphDBs.containsKey(dbPath)) {
			GraphDatabaseService svc = graphDBs.get(dbPath);
			if (svc != null) {
				return svc;
			}
		}
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(dbPath)
				.loadPropertiesFromFile(propertiesPath).newGraphDatabase();
		graphDBs.put(dbPath, graphDb);
		registerShutdownHook(graphDb);
		return graphDb;
	}

	public ObjectGraph wrapObjectGraph(GraphDatabaseService svc) {
		ObjectGraph og = ObjectGraphFactory.instance().get(svc);
		return og;
	}

	public NeoORM wrapNeoORM(GraphDatabaseService svc) {
		NeoORM orm = new NeoORM();
		orm.setSvc(svc);
		ObjectGraph og = ObjectGraphFactory.instance().get(svc);
		orm.setObjectGraph(og);
		return orm;
	}

	public static GraphDatabaseProducer instance() {
		return producer;
	}

	public Map<String, GraphDatabaseService> getGraphDBs() {
		return graphDBs;
	}

	public static Map<String, Node> getRefNodes() {
		return refNodes;
	}

	private static final GraphDatabaseProducer producer = new GraphDatabaseProducer();

	private void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}