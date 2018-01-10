package org.vt.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.vt.cache.Element;
import org.vt.cache.ICache;

/**
 * bean enumeration for get all table db data;
 * 
 * @author Zerg Law
 * 
 * @param <T>
 */
public class BeansEnumeration<T> implements Enumeration {

    private Map<Integer, Object> map = new HashMap<Integer, Object>();
    /** Array. */
    private int[] primaryIds;
    /** Index. */
    private int index = 0;
    private int cacheSize = 1000;
    private ICache cache;

    /**
     * Constructs an array enumeration
     * 
     * @param primaryIds
     *            primaryIds
     */
    public BeansEnumeration(ICache cache, int[] primaryIds) {
	this.cache = cache;
	this.primaryIds = primaryIds;
    }

    /**
     * Tests if this enumeration contains more elements.
     * 
     * @return <code>true</code> if this enumeration contains more elements;
     *         <code>false</code> otherwise.
     * @since JDK1.0
     */
    public boolean hasMoreElements() {
	boolean flag = index < primaryIds.length;
	if (!flag) {
	    this.primaryIds = null;
	    map.clear();
	}
	return flag;
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
	if (index < primaryIds.length) {
	    int primaryId = primaryIds[index];
	    if (!map.containsKey(primaryId)) {
		map.clear();
		int toIndex = index + cacheSize;
		if (toIndex > primaryIds.length) {
		    toIndex = primaryIds.length;
		}
		int[] newArr = Arrays.copyOfRange(primaryIds, index, toIndex);
		List<Element<T>> elements = cache.get(newArr);
		for (Element element : elements) {
		    map.put(element.getKey(), element.getData());
		}
	    }
	    index++;
	    return map.get(primaryId);
	}
	throw new NoSuchElementException();
    }
}