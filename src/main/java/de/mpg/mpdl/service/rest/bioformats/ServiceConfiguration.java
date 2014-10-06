package de.mpg.mpdl.service.rest.bioformats;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ServiceConfiguration {

	public class Pathes {
		public static final String PATH_EXPLAIN = "/explain";
		public static final String PATH_CONVERT = "/convert";
		public static final String PATH_FORMATS = "/formats";
	}

	public static final String SERVICE_NAME = "bio-formats";
	private static final String PROPERTIES_FILENAME = "bio-formats-service.properties";


    private Properties properties = new Properties();


	public ServiceConfiguration() {
		load();
	}

	public String getServiceUrl() {
		if (properties.containsKey("service.url"))
			return normalizeServiceUrl((String) properties.get("service.url"));
		return "http://localhost:8080/" + SERVICE_NAME;
	}

	private String normalizeServiceUrl(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

	public String getServiceApiUrl() {
		return getServiceUrl() + "/api";
	}

	/**
	 * Load the properties
	 */
	private void load() {

        String loc = "";
        try {
            if (System.getProperty("jboss.server.config.dir") != null) {
                loc = System.getProperty("jboss.server.config.dir");
            } else if (System.getProperty("catalina.home") != null) {
                loc = System.getProperty("catalina.home") + "/conf";
            } else  {

                //if no app server is defined, take props from WEB-INF
                //(this is the test case)
                properties.load(RestUtils.getResourceAsInputStream(PROPERTIES_FILENAME));
                return;
            }

            properties.load(new FileInputStream(new File(
                    FilenameUtils.concat(loc, PROPERTIES_FILENAME))));

        } catch (Exception e) {
            e.printStackTrace();
        }

	}

    public Properties getProperties() {
        return properties;
    }


}
