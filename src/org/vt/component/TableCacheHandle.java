package org.vt.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.RowSorter.SortKey;

import org.vt.beans.SearchCondition;
import org.vt.cache.CacheManager;
import org.vt.cache.Element;
import org.vt.cache.ICache;
import org.vt.cache.orm.ColumnBean;
import org.vt.util.BeansEnumeration;
import org.vt.util.CacheUtil;
import org.vt.util.VisualTableUtil;

/**
 * Table data change to db data
 * 
 * @author Zerg Law
 * 
 */
public class TableCacheHandle<T> {
    private ICache cache;
    private Class<T> cls;
    private VisualTableModel<T> model;

    public TableCacheHandle(VisualTableModel<T> model, Class<T> cls) {
	this.model = model;
	this.cls = cls;
	this.cache = CacheManager.getInstance().createCache(cls,
		"cache" + System.currentTimeMillis());
    }

    /**
     * add data ,then return the new show indexs
     * 
     * @param datas
     * @return new show indexs
     */
    public int[] add(List<T> datas) {
	List<Element> elements = buildElement(datas);
	this.cache.add(elements);

	List<Integer> primaryKeys = new ArrayList<Integer>();
	for (Element element : elements) {
	    primaryKeys.add(element.getKey());
	}

	return CacheUtil
		.concat(
			this.model.getTableShowContext().getIndexs() == null ? new int[] {}
				: this.model.getTableShowContext().getIndexs(),
			CacheUtil.toIntArr(primaryKeys));
    }

    public int[] remove(int[] primaryKeys) {
	this.cache.remove(primaryKeys);

	return CacheUtil.disconcat(
		this.model.getTableShowContext().getIndexs(), primaryKeys);
    }

    public void setRowDatas(Map<Integer, T> primaryKeyMap) {
	Map<Integer, Element<T>> idValueMap = new HashMap<Integer, Element<T>>();
	for (Map.Entry<Integer, T> entry : primaryKeyMap.entrySet()) {
	    idValueMap.put(entry.getKey().intValue(), new Element(entry
		    .getKey().intValue(), entry.getValue()));
	}
	this.cache.update(idValueMap);
    }

    public int[] getKeysByColumnValues(int columnIndex, Object[] values) {
	return cache.getKeys(columnIndex, values);
    }

    public int[] getKeysByRows(int[] rowIndexs) {
	int[] lastIndexs = model.getTableShowContext().getIndexs();
	List<Integer> indexs = new ArrayList<Integer>();
	for (int i = 0; i < rowIndexs.length; i++) {
	    // 多线程问题引起的越界
	    if (rowIndexs[i] < lastIndexs.length) {
		indexs.add(lastIndexs[rowIndexs[i]]);
	    }
	}
	return CacheUtil.toIntArr(indexs);
    }

    public List<Element<T>> getElementByKeys(int[] keys) {
	return cache.get(keys);
    }

    public int[] getKeys(SortKey sortKey, SearchCondition searchCondition) {
	return cache.getKeys(sortKey, searchCondition);
    }

    public void removeAll() {
	this.cache.removeAll();
    }

    public Enumeration<T> getAllBeans() {
	return new BeansEnumeration(TableCacheHandle.this.cache, cache
		.getKeys());
    }

    public ColumnBean[] getColumnBeans() {
	return cache.getColumnBeans();
    }

    public void updateCacheMap(TableShowContext<T> oldContext,
	    TableShowContext<T> newContext) {
	int startRow = newContext.getStartShowRow();
	int endRow = newContext.getEndShowRow();

	int startIndex = 0;
	int maxRow = VisualTableUtil.getScreenMaxRow(model.getTable());
	int pageSize = Math.max(100, maxRow) * 2;
	if (endRow - startRow > Math.max(maxRow, 400)) {
	    System.err.println("error view table:" + startRow + "," + endRow);
	    return;
	}
	if (startRow >= 0 || oldContext.getLastCacheRowRange() == null) {
	    startIndex = startRow - pageSize / 2 >= 0 ? startRow - pageSize / 2
		    : 0;
	    newContext.setLastCacheRowRange(new int[] { startIndex, pageSize });
	}
	// long t1 = System.currentTimeMillis();

	List<Integer> dbRowIds = getPageRowIds(newContext.getIndexs(),
		newContext.getLastCacheRowRange()[0], newContext
			.getLastCacheRowRange()[1]);

	Map<Integer, T> beanMap = new HashMap<Integer, T>();
	Map<Integer, Vector> cacheMap = new LinkedHashMap<Integer, Vector>();

	for (int i = 0; i < dbRowIds.size(); i++) {
	    T t = oldContext.getBeanMap().get(dbRowIds.get(i));
	    Vector vector = oldContext.getCacheMap().get(dbRowIds.get(i));
	    if (t != null && vector != null) {
		beanMap.put(dbRowIds.get(i), t);
		cacheMap.put(dbRowIds.get(i), vector);
		dbRowIds.remove(i);
		i--;
	    }
	}
	if (dbRowIds.isEmpty()) {
	    beanMap.clear();
	    cacheMap.clear();
	    // System.out.println("End updateCacheMap no change...");
	    return;
	}

	List<Element<T>> datas = this.cache.get(CacheUtil.toIntArr(dbRowIds));
	try {
	    Vector<Vector> vectors = CacheUtil.toShowVectors(datas);

	    for (int i = 0; i < dbRowIds.size(); i++) {
		if (vectors.size() > i) {
		    cacheMap.put(dbRowIds.get(i), vectors.get(i));
		    if (datas.get(i).getData() != null) {
			beanMap.put(dbRowIds.get(i), datas.get(i).getData());
		    }
		}
	    }
	} catch (InstantiationException e) {
	    throw new RuntimeException(e);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	// long t2 = System.currentTimeMillis();
	newContext.getBeanMap().clear();
	newContext.getBeanMap().putAll(beanMap);
	newContext.getCacheMap().clear();
	newContext.getCacheMap().putAll(cacheMap);
	// System.out.println("End updateCacheMap Spend:" + (t2 - t1));
    }

    private List<Element> buildElement(List<T> datas) {
	List<Element> elements = new ArrayList<Element>();
	for (T t : datas) {
	    elements.add(new Element(t));
	}
	return elements;
    }

    private List<Integer> getPageRowIds(int[] indexs, int pageIndex,
	    int pageSize) {
	List<Integer> list = new ArrayList<Integer>();
	if (indexs == null) {
	    return list;
	}
	for (int j = pageIndex; j < pageIndex + pageSize; j++) {
	    if (indexs.length <= j) {
		break;
	    }
	    list.add(indexs[j]);
	}
	return list;
    }

    private boolean isInOldRange(int[] lastRowRange, int startRow, int endRow) {
	if (lastRowRange != null) {
	    if (startRow >= lastRowRange[0]
		    && endRow <= (lastRowRange[0] + lastRowRange[1])) {
		return true;
	    }
	}
	return false;
    }

    public Class<T> getCls() {
	return cls;
    }

    public void destroy() {
	CacheManager.getInstance().deleteCache(cache.getName());
    }
}
