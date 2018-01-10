package org.vt.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.RowSorter;

import org.vt.beans.SearchCondition;

/**
 * the context for table data change or view condition change
 * 
 * @author Zerg Law
 * 
 */
public class TableShowContext<T> {
    private int[] indexs;
    /**
     * start show row that scroll to view for table
     */
    private int startShowRow = -1;

    /**
     * end show row that scroll to view for table
     */
    private int endShowRow = -1;

    /**
     * last cache start and end range
     */
    private int[] lastCacheRowRange = null;

    private RowSorter.SortKey sortKey = null;
    private SearchCondition searchCondition = null;

    private Map<Integer, T> beanMap = new HashMap<Integer, T>();
    private Map<Integer, T> beanBackMap = new HashMap<Integer, T>();
    private Map<Integer, Vector> cacheMap = new HashMap<Integer, Vector>();
    private Map<Integer, Vector> cacheBackMap = new HashMap<Integer, Vector>();

    public int getStartShowRow() {
	return startShowRow;
    }

    public void setStartShowRow(int startShowRow) {
	this.startShowRow = startShowRow;
    }

    public int getEndShowRow() {
	return endShowRow;
    }

    public void setEndShowRow(int endShowRow) {
	this.endShowRow = endShowRow;
    }

    public RowSorter.SortKey getSortKey() {
	return sortKey;
    }

    public void setSortKey(RowSorter.SortKey sortKey) {
	this.sortKey = sortKey;
    }

    public SearchCondition getSearchCondition() {
	return searchCondition;
    }

    public void setSearchCondition(SearchCondition searchCondition) {
	this.searchCondition = searchCondition;
    }

    public Map<Integer, T> getBeanMap() {
	return beanMap;
    }

    public void setBeanMap(Map<Integer, T> beanMap) {
	this.beanMap = beanMap;
    }

    public Map<Integer, Vector> getCacheMap() {
	return cacheMap;
    }

    public void setCacheMap(Map<Integer, Vector> cacheMap) {
	this.cacheMap = cacheMap;
    }

    public Map<Integer, T> getBeanBackMap() {
	return beanBackMap;
    }

    public void setBeanBackMap(Map<Integer, T> beanBackMap) {
	this.beanBackMap = beanBackMap;
    }

    public Map<Integer, Vector> getCacheBackMap() {
	return cacheBackMap;
    }

    public void setCacheBackMap(Map<Integer, Vector> cacheBackMap) {
	this.cacheBackMap = cacheBackMap;
    }

    public int[] getLastCacheRowRange() {
	return lastCacheRowRange;
    }

    public void setLastCacheRowRange(int[] lastCacheRowRange) {
	this.lastCacheRowRange = lastCacheRowRange;
    }

    public int getRowCount() {
	return this.indexs == null ? 0 : this.indexs.length;
    }

    public int[] getIndexs() {
	return indexs;
    }

    public void setIndexs(int[] indexs) {
	this.indexs = indexs;
    }

    @Override
    public TableShowContext<T> clone() {
	TableShowContext<T> context = new TableShowContext<T>();
	context.setStartShowRow(startShowRow);
	context.setEndShowRow(endShowRow);
	context.setSearchCondition(searchCondition);
	context.setSortKey(sortKey);
	context.setIndexs(indexs);
	context.setLastCacheRowRange(lastCacheRowRange);
	context.getBeanMap().putAll(this.beanMap);
	context.getCacheMap().putAll(this.cacheMap);
	context.getBeanBackMap().putAll(this.beanBackMap);
	context.getCacheBackMap().putAll(this.cacheBackMap);
	return context;
    }
}
