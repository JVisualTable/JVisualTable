package org.vt.locale;

import java.util.HashMap;

/**
 * resource map ,make sure no error bundle source
 * 
 * @author Zerg Law
 * 
 * @param <K>
 * @param <V>
 */
public class ResourceMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 8430325783320721685L;

    @SuppressWarnings("unchecked")
    public V get(Object key) {
	return (V) (super.containsKey(key) == true ? super.get(key) : key);
    }
}
