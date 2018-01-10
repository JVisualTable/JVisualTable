package org.vt.cache;

import java.util.List;
import java.util.Map;

import javax.swing.RowSorter;

import org.vt.beans.SearchCondition;
import org.vt.cache.orm.ColumnBean;

/**
 * cache interface for sqlite
 * 
 * @author Zerg Law
 * 
 */
public interface ICache<T> {

    /**
     * cache name
     * 
     * @return String
     */
    public String getName();

    /**
     * all keys
     * 
     * @return int[]
     */
    public int[] getKeys();

    /**
     * cache size
     * 
     * @return int
     */
    public int getSize();

    /**
     * cache data
     * 
     * @param keys
     *            keys
     * @return data
     */
    public List<Element<T>> get(int[] keys);

    /**
     * add
     * 
     * @param elements
     *            elements
     * @return boolean
     */
    public boolean add(List<Element<T>> elements);

    /**
     * update by map
     * 
     * @param objMap
     *            key is primaryKey，value is data
     * @return
     */
    public boolean update(Map<Integer, Element<T>> elementMap);

    /**
     * delete row by column index
     */
    public int removeKeys(int columnIndex, Object[] values);

    /**
     * remove
     * 
     * @param keys
     *            primary keys
     * @return influence row sum
     */
    public int remove(int[] keys);

    /**
     * remove all
     * 
     * @return remove sum
     */
    public int removeAll();

    /**
     * sort and search by condition
     * 
     * @param sortKey
     *            sort key
     * @param searchCondition
     *            Search Condition
     * @return primary keys
     */
    public int[] getKeys(RowSorter.SortKey sortKey,
	    SearchCondition searchCondition);

    /**
     * return keys for values
     * 
     * @param columnIndex
     *            columnIndex
     * @param values
     *            values
     * @return int[]
     */
    public int[] getKeys(int columnIndex, Object[] values);

    /**
     * column define
     * 
     * @return ColumnBean Array
     */
    public ColumnBean[] getColumnBeans();
}
