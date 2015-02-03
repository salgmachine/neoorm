package me.bcfh.neoorm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import jo4neo.ObjectGraph;
import jo4neo.ObjectGraphFactory;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;

/**
 * This is the CDI Portable Extension which hooks the {@link ObjectGraph} into
 * the CDI Lifecycle by a proxy object ({@link NeoORM})
 * 
 * @author salgmachine
 * @version 0.5.0
 */
public class NeoORMExtension implements Extension {

	/**
	 * This is an inner class containing repeatable tasks
	 */
	private static final Logger log = Logger.getLogger("Neo ORM");

	private static Map<String, GraphDatabaseService> graphDBs = new HashMap<String, GraphDatabaseService>();

	private final Map<String, NeoORM> instances = new HashMap<String, NeoORM>();

	private String fromClasspath;

	private String fromVMArgs;

	public NeoORMExtension() {
		log.info("Neo ORM Extension loaded");
	}

	private Boolean isRandom;

	/**
	 * This is a helper method for processing NeoEntityManager Annotations
	 */
	private <X> void processAnnotation(
			final Map<Field, Object> configuredValues, Annotation a, Field f,
			final InjectionTarget<X> it, ProcessInjectionTarget<X> pit) {
		if (a instanceof NeoEntityManager) {
			String path = ((NeoEntityManager) a).neopath();
			log.fine("found classmember annotation " + a + " value " + path);

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

					GraphDatabaseProducer.instance().getRefNodes()
							.put(path, svc.getReferenceNode());
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
							svc = GraphDatabaseProducer.instance().getInstance(
									path);
							GraphDatabaseProducer.instance().getRefNodes()
									.put(path, svc.getReferenceNode());
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
							svc = GraphDatabaseProducer.instance().getInstance(
									path);
						}
					}
				}
			}
			NeoORM instance = GraphDatabaseProducer.instance().wrapNeoORM(svc);
			log.fine("instance created for path " + path);
			// put the instance in the field map
			configuredValues.put(f, instance);
			// call postconstruct
			instance.init();
			// store instance for later access
			instances.put(path, instance);
			// now wrap it up
			NeoUtil.instance().wrapInjectionTarget(it, pit, configuredValues);
		}
	}

	public <X> void processClassMemberAnnotations(
			@Observes ProcessInjectionTarget<X> pit) {

		// This holds the mapping of the created instance and the target field
		final Map<Field, Object> configuredValues = new HashMap<Field, Object>();

		final InjectionTarget<X> it = pit.getInjectionTarget();
		AnnotatedType<X> at = pit.getAnnotatedType();
		Field[] fields = at.getJavaClass().getDeclaredFields();
		log.fine("looking at class " + at.getJavaClass());
		if (NeoUtil.instance().isNeoOrmAnnotationInClass(at.getJavaClass())) {
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
		String env = NeoUtil.instance().getPathFromEnv();
		// scan class path
		Properties prop = NeoUtil.instance().loadFromClassPath();
		String classpath = prop.getProperty("neo.path");

		log.info("-------------------------------------------------------------");
		log.info("\tfound path in environment (-D parameter) : " + env);
		this.fromVMArgs = env;
		log.info("\tfound path in classpath (properties file) : " + classpath);
		this.fromClasspath = classpath;
		log.info("-------------------------------------------------------------");
	}

	/**
	 * This method creates instances based upon the environment it runs in.<br/>
	 * Strings can either contain a Directory path (which will be the path the
	 * graphdb instance runs in) or it can contain a properties file (which must
	 * contain the path for the graphdb)<br/>
	 * <br/>
	 * This method creates grapbdb instances
	 */
	public void createInstancesAfterValidation(
			@Observes AfterDeploymentValidation evt) {
		List<String> paths = new ArrayList<String>();
		// make db paths
		for (String s : this.instances.keySet()) {
			if (s != null) {

				File f = new File(s);
				if (f.exists()) {
					if (f.isDirectory()) {
						paths.add(f.getAbsolutePath());
					} else {
						if (f.getName().toLowerCase()
								.endsWith("neo.properties")) {
							Properties prop = new Properties();
							try {
								prop.load(new BufferedInputStream(
										new FileInputStream(f)));
								String path = prop.getProperty(NeoKey.NeoDbPath
										.getValue());
								log.fine("read path from properties file "
										+ path);
							} catch (FileNotFoundException e) {
								log.severe("File " + f.getAbsolutePath()
										+ " could not be found");
							} catch (IOException e) {
								log.severe("File " + f.getAbsolutePath()
										+ " could not be read");
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
						String db = prop.getProperty(NeoKey.NeoDbPath
								.getValue());
						service = GraphDatabaseProducer.instance()
								.getConfiguredInstance(db, path);
					} catch (FileNotFoundException e) {
						log.severe(path + " could not be found");
					} catch (IOException e) {
						log.severe(path + " could not be read");
					}
				} else {
					if (new File(path).isDirectory()) {
						try {

							if (this.instances.containsKey(new File(path)
									.getAbsolutePath())) {
								service = this.instances.get(
										new File(path).getAbsolutePath())
										.getSvc();

							} else {
								service = GraphDatabaseProducer.instance()
										.getInstance(path);
							}
						} catch (Exception e) {
							log.severe("ATTENTION! COULD NOT CREATE DB INSTANCE ON PATH "
									+ path);
							e.printStackTrace();
						}
					}
				}
				// store graphdb instances in the map so we can retrieve it
				// later when shutting down
				graphDBs.put(path, service);
			}
		}

		log.info("Created " + instances.keySet().size()
				+ " graphdb instances {} " + instances.keySet());
	}

	class RefNodeProvider {

	}
}