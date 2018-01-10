package org.vt.component;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JScrollBar;
import javax.swing.RowSorter;
import javax.swing.table.AbstractTableModel;

import org.vt.JVisualTable;
import org.vt.beans.SearchCondition;
import org.vt.cache.Element;
import org.vt.cache.orm.ColumnBean;
import org.vt.util.CacheUtil;
import org.vt.util.VisualTableUtil;

/**
 * Visual table model
 * 
 * @author Zerg Law
 * 
 * @param <T>
 *            row bean
 */
public class VisualTableModel<T> extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    /**
     * Max Row Change Event Flag
     */
    public static final String PROPERTY_MAX_COUNT_CHANGE = "PROPERTY_MAX_COUNT_CHANGE";
    /**
     * Header Hidden Change Event Flag
     */
    public static final String PROPERTY_COLUMN_CHANGE = "PROPERTY_COLUMN_CHANGE";
    private ColumnBean[] columnBeans;
    private PropertyChangeSupport changeSupport;

    private SimpleDateFormat format = new SimpleDateFormat(
	    "yyyy-MM-dd HH:mm:ss");
    private JVisualTable table = null;
    /**
     * data is reversal shown?
     */
    private boolean reversal = false;
    private static boolean sortAndSearch = false;
    private static String sortAndSearchLock = "lock";
    private TableShowContext<T> tableShowContext = null;
    private TableChangeHandle<T> tableChangeHandle;

    /**
     * Visual TableModel
     * 
     * @param cls
     *            row data class
     */
    public VisualTableModel(Class<T> cls) {
	this.tableChangeHandle = new TableChangeHandle<T>(this, cls);
	this.columnBeans = this.tableChangeHandle.getTableCacheHandle()
		.getColumnBeans();
	this.changeSupport = new PropertyChangeSupport(this);
	this.tableShowContext = new TableShowContext<T>();
    }

    /**
     * Visual TableModel
     * 
     * @param cls
     *            row data class
     * @param bundle
     *            locale bundle for table header name
     */
    public VisualTableModel(Class<T> cls, ResourceBundle bundle) {
	this(cls);
	this.setResourceBundle(bundle);
    }

    /**
     * set bundle for header
     * 
     * @param bundle
     *            locale bundle for table header name
     */
    public void setResourceBundle(ResourceBundle bundle) {
	for (ColumnBean columnBean : columnBeans) {
	    if (columnBean.isShowInTable()) {
		columnBean.setTableColumnName(bundle.getString(columnBean
			.getTableColumnName()));
	    }
	}
	table.createDefaultColumnsFromModel();
    }

    /**
     * add data
     * 
     * @param beans
     *            beans
     */
    public void addBeans(List<T> beans, boolean sync) {
	if (beans.isEmpty()) {
	    return;
	}
	tableChangeHandle.add(beans, sync);
    }

    public void update(Map<Integer, T> beanMap, boolean sync) {
	this.tableChangeHandle.update(beanMap, sync);
    }

    /**
     * update bean
     * 
     * @param beans
     *            beans
     */
    public void updateBeans(List<T> beans, boolean sync) {
	if (beans.isEmpty()) {
	    return;
	}
	String keyName = getKeyNameForId();
	Map<Integer, T> beanMap = changeIdMap(keyName, beans);
	this.tableChangeHandle.update(beanMap, sync);
    }

    /**
     * Set Row Datas
     * 
     * @param newRows
     *            newRows
     */
    public void updateByRowMap(Map<Integer, T> newRows, boolean sync) {
	if (newRows.isEmpty()) {
	    return;
	}
	Map<Integer, T> beanMap = getKeyDataMap(newRows);
	this.tableChangeHandle.update(beanMap, sync);
    }

    /**
     * Remove id by primary keys
     * 
     * @param ids
     *            ids
     */
    public void remove(int[] primaryKeys, boolean sync) {
	if (primaryKeys == null || primaryKeys.length == 0) {
	    return;
	}
	this.tableChangeHandle.remove(primaryKeys, sync);
    }

    /**
     * Remove row show in table
     * 
     * @param rows
     *            rows
     */
    public void removeRows(int[] rows, boolean sync) {
	if (rows == null || rows.length == 0) {
	    return;
	}
	int[] primaryKeys = this.tableChangeHandle.getTableCacheHandle()
		.getKeysByRows(rows);
	this.tableChangeHandle.remove(primaryKeys, sync);
    }

    /**
     * remove all data ,clear table
     */
    public void removeAll() {
	this.tableChangeHandle.remove(null, true);
    }

    /**
     * Sort Change
     * 
     * @param newSortKey
     *            newSortKey
     */
    public void sortChange(RowSorter.SortKey newSortKey) {
	this.tableChangeHandle.sortChange(newSortKey);
    }

    /**
     * search change
     * 
     * @param newSearchCondition
     *            newSearchCondition
     */
    public void searchChange(SearchCondition newSearchCondition) {
	this.tableChangeHandle.searchChange(newSearchCondition);
    }

    private Map<Integer, T> changeIdMap(String keyName, List<T> datas) {
	int columnIndex = indexOfKeyName(keyName);
	List<Object> values = new ArrayList<Object>();
	for (T data : datas) {
	    values.add(CacheUtil.toVector(data).get(columnIndex - 1));
	}
	int[] primaryIds = this.tableChangeHandle.getTableCacheHandle()
		.getKeysByColumnValues(columnIndex,
			values.toArray(new Object[values.size()]));
	Map<Integer, T> idValueMap = new HashMap<Integer, T>();
	for (int i = 0; i < primaryIds.length; i++) {
	    if (datas.size() > i) {
		idValueMap.put(primaryIds[i], datas.get(i));
	    }
	}
	return idValueMap;
    }

    /**
     * Get Select Row Data Beans
     * 
     * @param rowIndexs
     *            rowIndexs
     * @return Vector Vector
     */
    public List<T> getSelectBeans(int[] rowIndexs) {
	if (rowIndexs == null || rowIndexs.length == 0) {
	    return new ArrayList<T>();
	}
	return getRowDatas(rowIndexs);
    }

    /**
     * Get Bean Map by column id
     * 
     * @param ids
     *            ids
     * @return Map
     */
    public Map<Object, T> getBeans(Object[] ids) {
	if (ids == null || ids.length == 0) {
	    return new HashMap<Object, T>();
	}
	int columnIndex = indexOfKeyName(getKeyNameForId());
	int[] primaryKeys = this.tableChangeHandle.getTableCacheHandle()
		.getKeysByColumnValues(columnIndex, ids);
	List<Element<T>> elements = this.tableChangeHandle
		.getTableCacheHandle().getElementByKeys(primaryKeys);
	Map<Object, T> map = new HashMap<Object, T>();
	for (Element<T> data : elements) {
	    Object keyObject = CacheUtil.toVector(data.getData()).get(
		    columnIndex);
	    map.put(keyObject, (T) data.getData());
	}
	return map;
    }

    public JVisualTable getTable() {
	return table;
    }

    private void initReadyScrollListener(JScrollBar scrollBar) {
	if (scrollBar == null) {
	    return;
	}
	scrollBar.addAdjustmentListener(new AdjustmentListener() {

	    @Override
	    public void adjustmentValueChanged(AdjustmentEvent e) {
		if (!e.getValueIsAdjusting()) {
		    int[] result = VisualTableUtil.getViewportRows(getTable(),
			    e.getValue());
		    TableShowContext<T> tableShowContext = getTableShowContext()
			    .clone();
		    tableShowContext.setStartShowRow(result[0]);
		    tableShowContext.setEndShowRow(result[1]);

		    tableChangeHandle.tableViewportChange(
			    getTableShowContext(), tableShowContext);

		}
	    }
	});
    }

    private void initScrollListener() {
	JScrollBar scrollBar = VisualTableUtil.getVerticalScrollBar(getTable());
	if (scrollBar == null) {
	    getTable().addHierarchyListener(new HierarchyListener() {

		@Override
		public void hierarchyChanged(HierarchyEvent e) {
		    if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
			initReadyScrollListener(VisualTableUtil
				.getVerticalScrollBar(getTable()));
		    }
		}
	    });
	} else {
	    initReadyScrollListener(scrollBar);
	}

    }

    public void setTable(JVisualTable table) {
	this.table = table;
	initScrollListener();
    }

    /**
     * Get Column Index by show index
     * 
     * @param index
     *            index
     * @return int
     */
    public int getColumnIndexByShowInTableIndex(int index) {
	for (ColumnBean bean : columnBeans) {
	    if (bean.isShowInTable() && bean.getShowInTableIndex() == index) {
		return bean.getIndex();
	    }
	}
	return -1;
    }

    /**
     * Data change
     * 
     * @param evt
     *            evt
     */
    public void firePropertyChange(PropertyChangeEvent evt) {
	changeSupport.firePropertyChange(evt);
    }

    /**
     * addPropertyChangeListener
     * 
     * @param listener
     *            listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	if (listener == null) {
	    return;
	}
	if (changeSupport == null) {
	    changeSupport = new PropertyChangeSupport(this);
	}
	changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * removePropertyChangeListener
     * 
     * @param listener
     *            listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	if (listener == null || changeSupport == null) {
	    return;
	}
	changeSupport.removePropertyChangeListener(listener);
    }

    private List<ColumnBean> getShowColumnBean() {
	List<ColumnBean> beans = new ArrayList<ColumnBean>();
	for (ColumnBean bean : columnBeans) {
	    if (bean.isShowInTable()) {
		if (!bean.isHidden()) {
		    beans.add(bean);
		}
	    }
	}
	return beans;
    }

    @Override
    public String getColumnName(int column) {
	List<ColumnBean> columnBeans1 = getShowColumnBean();
	Object id = columnBeans1.size() > column ? columnBeans1.get(column)
		.getTableColumnName() : null;
	return (id == null) ? super.getColumnName(column) : id.toString();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
	// int index = getColumnIndexByShowInTableIndex(column);
	// if (index >= 0) {
	// T t = getRowData(row);
	// Vector vector = CacheUtil.toVector(t);
	// vector.set(index - 1, aValue);
	// Map<Integer, T> map = new HashMap<Integer, T>();
	// T obj = CacheUtil.toBean(this.cls, vector);
	// map.put(row, obj);
	// setRows(map);
	// fireTableCellUpdated(row, column);
	// }
	return;
    }

    private Map<Integer, T> getKeyDataMap(Map<Integer, T> rowDataMap) {
	Map<Integer, T> map = new HashMap<Integer, T>();

	List<Integer> rows = new ArrayList<Integer>();
	rows.addAll(rowDataMap.keySet());

	int[] primaryKeys = this.tableChangeHandle.getTableCacheHandle()
		.getKeysByRows(CacheUtil.toIntArr(rows));
	Map<Integer, Integer> rowToKeyMap = new HashMap<Integer, Integer>();
	for (int i = 0; i < rows.size(); i++) {
	    rowToKeyMap.put(rows.get(i), primaryKeys[i]);
	}

	Map<Integer, T> primayKeyMap = new HashMap<Integer, T>();
	for (Map.Entry<Integer, T> entry : rowDataMap.entrySet()) {
	    Integer key = rowToKeyMap.get(entry.getKey());
	    if (key != null) {
		primayKeyMap.put(key.intValue(), entry.getValue());
	    }
	}
	return map;
    }

    private boolean isSame(Object obj1, Object obj2) {
	if (obj1 == null && obj2 == null) {
	    return true;
	}
	if (obj1 != null && obj2 != null && obj1.equals(obj2)) {
	    return true;
	}
	return false;
    }

    public String getSortAndSearchLock() {
	return sortAndSearchLock;
    }

    /**
     *Get Real Row Data
     * 
     * @param rowIndex
     *            rowIndex
     * @return t
     */
    public T getRowData(int rowIndex) {
	if (this.getTableShowContext().getIndexs().length <= rowIndex) {
	    return null;
	}
	int dbRowId = this.getTableShowContext().getIndexs()[rowIndex];
	// 先从表格显示的缓存中查找，如果没有，启用备用缓存，再获取
	Map<Integer, T> beanMap = this.getTableShowContext().getBeanMap();
	return beanMap.get(dbRowId);
    }

    /**
     * Get Row Datas include hidden data
     * 
     * @param rowIndexs
     *            rowIndex
     * @return list
     */
    public List<T> getRowDatas(int[] rowIndexs) {
	if (rowIndexs == null || rowIndexs.length == 0) {
	    return new ArrayList<T>();
	}
	int[] primaryKeys = getPrimaryKeys(rowIndexs);
	List<Element<T>> elements = tableChangeHandle.getTableCacheHandle()
		.getElementByKeys(primaryKeys);
	List<T> list = new ArrayList<T>();
	for (Element<T> element : elements) {
	    list.add(element.getData());
	}
	return list;
    }

    public int[] getPrimaryKeys(int[] rowIndexs) {
	List<Integer> list = new ArrayList<Integer>();
	int[] lastIndexs = getTableShowContext().getIndexs();
	for (int row : rowIndexs) {
	    if (lastIndexs.length <= row) {
		continue;
	    }
	    list.add(lastIndexs[row]);

	}
	return CacheUtil.toIntArr(list);
    }

    private Map<Integer, Integer> toMap(int[] lastIndexs) {
	Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	if (lastIndexs != null) {
	    for (int i = 0; i < lastIndexs.length; i++) {
		map.put(lastIndexs[i], i);
	    }
	}
	return map;
    }

    public int[] getRowsByPrimaryKeys(int[] primaryKeys) {
	List<Integer> list = new ArrayList<Integer>();
	if (primaryKeys != null) {
	    Map<Integer, Integer> rowIndexMap = toMap(this
		    .getTableShowContext().getIndexs());
	    for (int i = 0; i < primaryKeys.length; i++) {
		Integer row = rowIndexMap.get(primaryKeys[i]);
		if (row != null && row.intValue() >= 0) {
		    list.add(row.intValue());
		}
	    }
	}
	return CacheUtil.toIntArr(list);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
	int[] lastIndexs = getTableShowContext().getIndexs();
	Map<Integer, Vector> cacheMap = getTableShowContext().getCacheMap();
	if (lastIndexs.length <= rowIndex) {
	    return null;
	}
	int primaryKey = lastIndexs[rowIndex];
	Vector vector = cacheMap.get(primaryKey);
	if (vector == null) {
	    // System.err.println("error show:" + rowIndex);
	    return null;
	}
	int index = getColumnIndexByShowInTableIndex(columnIndex) - 1;
	Object value = null;
	if (index >= 0 && vector.size() > index) {
	    value = vector.get(index);
	    if (value != null && value instanceof Date) {
		return format.format(value);
	    }
	}
	// System.out.println(rowIndex + "," + columnIndex + ":" + value);
	return value;
    }

    /**
     * Index for Column by KeyName
     * 
     * @param keyName
     *            keyName
     * @return int
     */
    public int indexOfKeyName(String keyName) {
	for (int i = 0; i < columnBeans.length; i++) {
	    if (keyName.equals(columnBeans[i].getKeyName())) {
		return i;
	    }
	}
	return -1;
    }

    @Override
    public int getRowCount() {
	int rowCount = getTableShowContext().getRowCount();
	// System.out.println("rowCount:" + rowCount);
	return rowCount;
    }

    @Override
    public int getColumnCount() {
	return getShowColumnBean().size();
    }

    public ColumnBean[] getColumnBeans() {
	return columnBeans;
    }

    public void setColumnBeans(ColumnBean[] columnBeans) {
	this.columnBeans = columnBeans;
    }

    private String getKeyNameForId() {
	for (ColumnBean bean : columnBeans) {
	    if (bean.isId()) {
		return bean.getKeyName();
	    }
	}
	throw new RuntimeException("can not find id column.");
    }

    /**
     * 
     * Get All data
     * 
     * @return Enumeration
     */
    public Enumeration<T> getAllBeans() {
	return this.tableChangeHandle.getTableCacheHandle().getAllBeans();
    }

    public boolean isReversal() {
	return reversal;
    }

    public void setReversal(boolean reversal) {
	this.reversal = reversal;
    }

    public boolean isSortAndSearch() {
	return sortAndSearch;
    }

    public TableShowContext getTableShowContext() {
	return tableShowContext;
    }

    public void setTableShowContext(TableShowContext tableShowContext) {
	this.tableShowContext = tableShowContext;
    }

    /**
     * destroy all resource
     */
    public void destroy() {
	this.tableChangeHandle.getTableCacheHandle().destroy();
    }

    /**
     * return bean class
     * 
     * @return Class
     */
    public Class getBeanClass() {
	return this.tableChangeHandle.getTableCacheHandle().getCls();
    }
}
