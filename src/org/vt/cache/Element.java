package org.vt.cache;

/**
 * cache element
 * 
 * @author Zerg Law
 * 
 * @param <T>
 *            t
 */
public class Element<T> {
    private int key = Integer.MIN_VALUE;
    private T data;

    /**
     * When key is Integer.MIN_VALUE， Id will producted auto
     * 
     * @param key
     * @param data
     */
    public Element(int key, T t) {
	super();
	this.key = key;
	this.data = t;
    }

    public Element(T t) {
	this.data = t;
    }

    public int getKey() {
	return key;
    }

    public void setKey(int key) {
	this.key = key;
    }

    public T getData() {
	return data;
    }

    public void setData(T data) {
	this.data = data;
    }

}
