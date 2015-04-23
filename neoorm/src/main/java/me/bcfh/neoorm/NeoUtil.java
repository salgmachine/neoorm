package me.bcfh.neoorm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;


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
					String value = prop.getProperty(NeoKey.NeoDbPath.getValue());
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
		InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/neo.properties");
		return is;
	}

	public static NeoUtil instance() {
		return UTIL;
	}

}
