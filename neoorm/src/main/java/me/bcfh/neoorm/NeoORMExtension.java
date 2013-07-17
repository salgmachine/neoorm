package me.bcfh.neoorm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import jo4neo.ObjectGraph;
import jo4neo.ObjectGraphFactory;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the CDI Portable Extension which hooks the {@link ObjectGraph} into
 * the CDI Lifecycle by a proxy object ({@link NeoORM})
 * 
 * @author salgmachine
 * @version 0.5.0
 */
public class NeoORMExtension implements Extension {

	private static Map<String, Node> refNodes = new HashMap<String, Node>();

	/**
	 * This is an inner class which produces neo4j graphDatabaseService
	 * instances
	 */
	private static final class GraphDatabaseProducer {

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
			GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath).newGraphDatabase();
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
			GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath).newGraphDatabase();

			graphDBs.put(dbPath, graphDb);
			registerShutdownHook(graphDb);
			return graphDb;
		}

		public GraphDatabaseService getConfiguredInstance(String dbPath, String propertiesPath) {
			if (graphDBs.containsKey(dbPath)) {
				GraphDatabaseService svc = graphDBs.get(dbPath);
				if (svc != null) {
					return svc;
				}
			}
			GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath)
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

	/**
	 * This is an inner class containing repeatable tasks
	 */
	private static final class NeoormUtil {

		private static final Logger log = LoggerFactory.getLogger(NeoormUtil.class);

		private static final NeoormUtil util = new NeoormUtil();

		public String getPathFromEnv() {
			String path = System.getProperties().getProperty("neo.path");
			if (path != null) {
				if (path.endsWith("neo.properties")) {
					Properties prop = new Properties();
					try {
						prop.load(new FileInputStream(new File(path)));
						String value = prop.getProperty(NeoKey.NeoDbPath.getValue());
						return value;
					} catch (FileNotFoundException e) {
						log.error("File " + path + " could not be found");
					} catch (IOException e) {
						log.error("File " + path + " could not be read");
					}
				}
			}
			return System.getProperties().getProperty("neo.path");
		}

		public Properties loadFromClassPath() {
			Properties prop = new Properties();
			try {
				InputStream is = getInputStream();
				if (is != null) {
					prop.load(is);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return prop;
		}

		private InputStream getInputStream() {
			InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/neo.properties");
			return is;
		}

		public static NeoormUtil instance() {
			return util;
		}

		public boolean isNeoOrmAnnotationInClass(Class c) {
			boolean found = false;
			Field[] fields = c.getDeclaredFields();
			for (Field f : fields) {
				// look at field annotation
				Annotation[] annotations = f.getAnnotations();
				if (annotations != null) {
					for (Annotation a : annotations) {
						if (a instanceof NeoEntityManager) {
							found = true;
							break;
						}
					}
				}
			}

			return found;
		}

		public <X> void wrapInjectionTarget(final InjectionTarget<X> targetPoint, final ProcessInjectionTarget<X> targetPointCtx,
				final Map<Field, Object> targetValues) {
			InjectionTarget<X> wrapped = new InjectionTarget<X>() {

				@Override
				public void inject(X instance, CreationalContext<X> ctx) {
					targetPoint.inject(instance, ctx);

					for (Map.Entry<Field, Object> configuredValue : targetValues.entrySet()) {
						try {
							configuredValue.getKey().setAccessible(true);
							configuredValue.getKey().set(instance, configuredValue.getValue());
							configuredValue.getKey().setAccessible(false);
						} catch (Exception e) {
							throw new InjectionException(e);
						}
					}
				}

				@Override
				public void postConstruct(X instance) {
					targetPoint.postConstruct(instance);
				}

				@Override
				public void preDestroy(X instance) {
					targetPoint.dispose(instance);
				}

				@Override
				public void dispose(X instance) {
					targetPoint.dispose(instance);
				}

				@Override
				public Set<InjectionPoint> getInjectionPoints() {
					return targetPoint.getInjectionPoints();
				}

				@Override
				public X produce(CreationalContext<X> ctx) {
					return targetPoint.produce(ctx);
				}

			};

			targetPointCtx.setInjectionTarget(wrapped);
		}
	}

	private static final Logger log = LoggerFactory.getLogger(NeoORMExtension.class);

	private static Map<String, GraphDatabaseService> graphDBs = new HashMap<String, GraphDatabaseService>();

	private final Map<String, NeoORM> instances = new HashMap<String, NeoORM>();

	private String fromClasspath;

	private String fromVMArgs;

	public NeoORMExtension() {
		log.debug("Neo ORM Extension loaded");
	}

	private Boolean isRandom;

	/**
	 * This is a helper method for processing NeoEntityManager Annotations
	 */
	private <X> void processAnnotation(final Map<Field, Object> configuredValues, Annotation a, Field f, final InjectionTarget<X> it,
			ProcessInjectionTarget<X> pit) {
		if (a instanceof NeoEntityManager) {
			String path = ((NeoEntityManager) a).neopath();
			log.debug("found classmember annotation " + a + " value " + path);

			GraphDatabaseService svc = null;
			// found annotation has string value
			if (!path.isEmpty()) {

				if (instances.containsKey(path)) {
					// old instance
					svc = instances.get(path).getSvc();
					// getObjectGraph().getRefnode()
					// .getGraphDatabase();
				} else {
					// new instance
					svc = GraphDatabaseProducer.instance().getInstance(path);
					refNodes.put(path, svc.getReferenceNode());
				}
			} else {
				// found annotation is missing string value,
				// looking for defaults
				// at this point params from the environment
				// should already be available
				// if not we generate a temporary directory
				boolean existsInClassPath = fromClasspath != null;
				boolean existsInJVMArg = fromVMArgs != null;

				if (!existsInClassPath && !existsInJVMArg) {
					// switch to temporary
					// choose a random directory
					String tmpPath = System.getProperty("java.io.tmpdir");
					String salt = "neo4j-random";
					path = tmpPath + salt;
					File file = new File(path);
					if (file.exists()) {
						boolean del = file.delete();
						if (!del) {
							file.renameTo(new File(path + "-old"));
						}

					}
					svc = GraphDatabaseProducer.instance().getInstance(path);

				} else {
					if (existsInClassPath) {
						// first look in classpath
						path = fromClasspath;
						if (instances.containsKey(path)) {
							// old instance
							svc = instances.get(path).getSvc();
						} else {
							// new instance
							svc = GraphDatabaseProducer.instance().getInstance(path);
							refNodes.put(path, svc.getReferenceNode());
						}
						// continue, as from here on we ignore
						// any jvm args
					} else if (existsInJVMArg) {
						path = fromVMArgs;
						// then look for jvm param
						if (instances.containsKey(path)) {
							// old instance
							svc = instances.get(path).getSvc();
						} else {
							// new instance
							svc = GraphDatabaseProducer.instance().getInstance(path);
						}
					}
				}
			}
			NeoORM instance = GraphDatabaseProducer.instance().wrapNeoORM(svc);
			log.debug("instance created for path " + path);
			// put the instance in the field map
			configuredValues.put(f, instance);
			// call postconstruct
			instance.init();
			// store instance for later access
			instances.put(path, instance);
			// now wrap it up
			NeoormUtil.instance().wrapInjectionTarget(it, pit, configuredValues);
		}
	}

	public <X> void processClassMemberAnnotations(@Observes ProcessInjectionTarget<X> pit) {

		// This holds the mapping of the created instance and the target field
		final Map<Field, Object> configuredValues = new HashMap<Field, Object>();

		final InjectionTarget<X> it = pit.getInjectionTarget();
		AnnotatedType<X> at = pit.getAnnotatedType();
		Field[] fields = at.getJavaClass().getDeclaredFields();
		if (NeoormUtil.instance().isNeoOrmAnnotationInClass(at.getJavaClass())) {
			for (Field f : fields) {
				// look at field annotation classmember level
				Annotation[] annotations = f.getAnnotations();
				for (Annotation a : annotations) {
					processAnnotation(configuredValues, a, f, it, pit);
				}
			}
		}
	}

	/**
	 * This method performs cleanup tasks such as shutting down all running
	 * graphdb instances
	 */
	public void shutdownScheduler(@Observes BeforeShutdown event) {
		// cleanup
		// close all instances in the instances map
		for (String s : graphDBs.keySet()) {
			graphDBs.get(s).shutdown();
		}
	}

	/**
	 * This method performs initializtation during the BeforeBeanDiscovery Event<br/>
	 * Here we look for the params passed as
	 * <ul>
	 * <li>jvm argument</li>
	 * <li>file in classpath (put in meta-inf)</li>
	 * </ul>
	 */
	public void initialPhase(@Observes BeforeBeanDiscovery evt) {
		// do init stuff
		// look in environment
		String env = NeoormUtil.instance().getPathFromEnv();
		// scan class path
		Properties prop = NeoormUtil.instance().loadFromClassPath();
		String classpath = prop.getProperty("neo.path");

		log.debug("-------------------------------------------------------------");
		log.debug("\tfound path in environment (-D parameter) : " + env);
		this.fromVMArgs = env;
		log.debug("\tfound path in classpath (properties file) : " + classpath);
		this.fromClasspath = classpath;
		log.debug("-------------------------------------------------------------");
	}

	/**
	 * This method creates instances based upon the environment it runs in.<br/>
	 * Strings can either contain a Directory path (which will be the path the
	 * graphdb instance runs in) or it can contain a properties file (which must
	 * contain the path for the graphdb)<br/>
	 * <br/>
	 * This method creates grapbdb instances
	 */
	public void createInstancesAfterValidation(@Observes AfterDeploymentValidation evt) {
		List<String> paths = new ArrayList<String>();
		// make db paths
		for (String s : this.instances.keySet()) {
			if (s != null) {

				File f = new File(s);
				if (f.exists()) {
					if (f.isDirectory()) {
						paths.add(f.getAbsolutePath());
					} else {
						if (f.getName().toLowerCase().endsWith("neo.properties")) {
							Properties prop = new Properties();
							try {
								prop.load(new BufferedInputStream(new FileInputStream(f)));
								String path = prop.getProperty(NeoKey.NeoDbPath.getValue());
								log.debug("read path from properties file " + path);
							} catch (FileNotFoundException e) {
								log.error("File " + f.getAbsolutePath() + " could not be found");
							} catch (IOException e) {
								log.error("File " + f.getAbsolutePath() + " could not be read");
							}
						}
					}
				} else {
					f.mkdir();
					paths.add(f.getAbsolutePath());
				}
			}

		}

		// create graphdb instances with the found filepaths (in case of using
		// neo.properties)
		for (String path : paths) {
			if (path.contains("\\")) {
				path = path.replaceAll("\\\\", "/");
			}
			if (!graphDBs.containsKey(path)) {
				GraphDatabaseService service = null;
				if (path.endsWith("neo.properties")) {
					Properties prop = new Properties();
					try {
						prop.load(new FileInputStream(new File(path)));
						String db = prop.getProperty(NeoKey.NeoDbPath.getValue());
						service = GraphDatabaseProducer.instance().getConfiguredInstance(db, path);
					} catch (FileNotFoundException e) {
						log.error(path + " could not be found");
					} catch (IOException e) {
						log.error(path + " could not be read");
					}
				} else {
					if (new File(path).isDirectory()) {
						try {

							if (this.instances.containsKey(new File(path).getAbsolutePath())) {
								service = this.instances.get(new File(path).getAbsolutePath()).getSvc();

							} else {
								service = GraphDatabaseProducer.instance().getInstance(path);
							}
						} catch (Exception e) {
							log.error("ATTENTION! COULD NOT CREATE DB INSTANCE ON PATH " + path);
							e.printStackTrace();
						}
					}
				}
				// store graphdb instances in the map so we can retrieve it
				// later when shutting down
				graphDBs.put(path, service);
			}
		}

		log.info("Created {} graphdb instances {} ", instances.keySet().size(), instances.keySet());
	}

	class RefNodeProvider {

	}
}