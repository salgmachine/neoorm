package me.bcfh.neoorm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

public class NeoUtil {
	private static final Logger log = Logger.getLogger("NeoUtil");

	private NeoUtil() {
	}

	private static final NeoUtil UTIL = new NeoUtil();

	public String getPathFromEnv() {
		String path = System.getProperties().getProperty("neo.path");
		if (path != null) {
			if (path.endsWith("neo.properties")) {
				Properties prop = new Properties();
				try {
					prop.load(new FileInputStream(new File(path)));
					String value = prop
							.getProperty(NeoKey.NeoDbPath.getValue());
					return value;
				} catch (FileNotFoundException e) {
					log.severe("File " + path + " could not be found");
				} catch (IOException e) {
					log.severe("File " + path + " could not be read");
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
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"META-INF/neo.properties");
		return is;
	}

	public static NeoUtil instance() {
		return UTIL;
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

	public <X> void wrapInjectionTarget(final InjectionTarget<X> targetPoint,
			final ProcessInjectionTarget<X> targetPointCtx,
			final Map<Field, Object> targetValues) {
		InjectionTarget<X> wrapped = new InjectionTarget<X>() {

			@Override
			public void inject(X instance, CreationalContext<X> ctx) {
				targetPoint.inject(instance, ctx);

				for (Map.Entry<Field, Object> configuredValue : targetValues
						.entrySet()) {
					try {
						configuredValue.getKey().setAccessible(true);
						configuredValue.getKey().set(instance,
								configuredValue.getValue());
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
