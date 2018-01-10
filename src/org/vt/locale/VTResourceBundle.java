package org.vt.locale;

import java.util.Enumeration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * VT Bundle, Make sure Value is translated
 * 
 * @author Zerg Law
 */
public class VTResourceBundle extends ResourceBundle {

    private Map<String, String> resourceMap;

    public VTResourceBundle(Map<String, String> resourceMap) {
	this.resourceMap = resourceMap;
    }

    @Override
    public Object handleGetObject(String key) {
	if (key == null) {
	    throw new NullPointerException();
	}
	return resourceMap.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
	return new ArrayEnumeration(resourceMap.keySet().toArray(
		new Object[resourceMap.keySet().size()]));
    }

    /**
     * 数组枚举实现
     */
    static class ArrayEnumeration implements Enumeration {

	/** Array. */
	private Object[] array;
	/** Index. */
	private int index;

	/**
	 * ArrayEnumeration
	 * 
	 * @param array
	 *            array
	 */
	public ArrayEnumeration(Object[] array) {
	    this.array = array;
	}

	/**
	 * Tests if this enumeration contains more elements.
	 * 
	 * @return <code>true</code> if this enumeration contains more elements;
	 *         <code>false</code> otherwise.
	 * @since JDK1.0
	 */
	public boolean hasMoreElements() {
	    return index < array.length;
	}

	/**
	 * Returns the next element of this enumeration.
	 * 
	 * @return the next element of this enumeration.
	 * @exception NoSuchElementException
	 *                if no more elements exist.
	 * @since JDK1.0
	 */
	public Object nextElement() {
	    if (index < array.length) {
		return array[index++];
	    }
	    throw new NoSuchElementException();
	}
    }
}