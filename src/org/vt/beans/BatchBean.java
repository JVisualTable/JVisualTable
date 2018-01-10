package org.vt.beans;

/**
 * batch put data
 * 
 * @author Zerg Law
 * 
 * @param <T>
 */
public class BatchBean<T> {
    private OperaType type;
    private int primaryKey;
    private T t;

    public OperaType getType() {
	return type;
    }

    public void setType(OperaType type) {
	this.type = type;
    }

    public int getPrimaryKey() {
	return primaryKey;
    }

    public void setPrimaryKey(int primaryKey) {
	this.primaryKey = primaryKey;
    }

    public T getT() {
	return t;
    }

    public void setT(T t) {
	this.t = t;
    }

}
