package me.bcfh.neoorm;

import jo4neo.impl.ObjectGraphProvider;

import org.neo4j.graphdb.GraphDatabaseService;


public class OrmProvider {

	private static final GraphDatabaseService SERVICE = getService();

	private static GraphDatabaseService getService() {
		return GraphDatabaseProducer.instance().getInstance(NeoUtil.instance().getPathFromEnv());
	}

	private static final NeoORM instance = new NeoORM(new ObjectGraphProvider().create(SERVICE));

	private OrmProvider() {
	}

	public static NeoORM getOrm() {
		return instance;
	}

	public static void shutdown() {
		SERVICE.shutdown();
	}

}
