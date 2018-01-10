package org.vt.locale;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Locale For Bundle Tools
 * 
 * @author Zerg Law
 */
public class ResourceManager {

    private static Logger logger = Logger.getLogger(ResourceManager.class
	    .getName());

    /**
     * Get useClass's Bundle.properties For current package,
     * 
     * @param useClass
     *            useClass
     * @return ResourceBundle
     */
    public static ResourceBundle getBundle(Class<?> useClass) {
	return getFHResourceBundle(ResourceBundle
		.getBundle(getPackageBundlePathByClass(useClass)));
    }

    /**
     * Get useClass's Bundle.properties , The Properties File Name same as Class
     * 
     * @param useClass
     *            useClass
     * @return ResourceBundle
     */
    public static ResourceBundle getFileBundle(Class<?> useClass) {
	return getFHResourceBundle(ResourceBundle
		.getBundle(getBundlePathByClass(useClass)));
    }

    private static VTResourceBundle getFHResourceBundle(ResourceBundle bundle) {
	ResourceMap<String, String> map = new ResourceMap<String, String>();
	try {
	    Enumeration<String> keys = bundle.getKeys();
	    while (keys.hasMoreElements()) {
		String key = keys.nextElement();
		map.put(key, bundle.getString(key));
	    }
	} catch (RuntimeException e) {
	    logger.log(Level.WARNING, e.getMessage());
	}
	return new VTResourceBundle(map);
    }

    private static String getBundlePathByClass(Class class1) {
	return class1.getCanonicalName();
    }

    private static String getPackageBundlePathByClass(Class class1) {
	String cls = class1.getCanonicalName();
	int index = cls.lastIndexOf(".");
	if (index == -1) {
	    return "Bundle";
	}
	return cls.substring(0, index) + ".Bundle";
    }

}
