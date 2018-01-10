package org.vt.component;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.RowSorter;

import org.vt.beans.BatchBean;
import org.vt.beans.OperaType;
import org.vt.beans.SearchCondition;
import org.vt.core.SandGlassQueue;
import org.vt.util.VisualTableUtil;

/**
 * table show change or data change process handle
 * 
 * @author Zerg Law
 * 
 */
public class TableChangeHandle<T> {

    private Logger logger = Logger.getLogger(getClass().getName());
    private VisualTableModel<T> model;
    private TableCacheHandle<T> tableCacheHandle;
    private SandGlassQueue<BatchBean<T>> queue;
    private boolean destroyed = false;
    private static String sortAndSearchLock = "lock";
    private static boolean sortAndSearch = false;

    public TableChangeHandle(VisualTableModel<T> model, Class<T> cls) {
	this.model = model;
	this.tableCacheHandle = new TableCacheHandle<T>(model, cls);
	this.queue = new SandGlassQueue<BatchBean<T>>(5000, 1000);
	this.start();
    }

    private void start() {
	Thread thread = new Thread() {
	    @Override
	    public void run() {
		while (!destroyed) {
		    try {
			List<BatchBean<T>> batchBeans = queue.take();
			List<BatchBean<T>> norepeatBeans = VisualTableUtil
				.deleteRepeatBeans(
					TableChangeHandle.this.model,
					batchBeans);
			List<List<BatchBean<T>>> beanLists = VisualTableUtil
				.subListForBatchSingle(norepeatBeans);
			for (final List<BatchBean<T>> beans : beanLists) {
			    if (beans.isEmpty()) {
				continue;
			    }
			    OperaType msgType = beans.get(0).getType();
			    if (msgType == OperaType.ADD) {
				int[] indexs = getTableCacheHandle().add(
					VisualTableUtil.toBeans(beans));
				TableShowContext<T> newTableShowContext = model
					.getTableShowContext().clone();
				newTableShowContext.setIndexs(indexs);
				tableAdd(model.getTableShowContext(),
					newTableShowContext);
			    } else if (msgType == OperaType.UPDATE) {
				getTableCacheHandle().setRowDatas(
					VisualTableUtil.toUpdateBeans(beans));

				tableUpdate(model.getTableShowContext(), model
					.getTableShowContext().clone());
			    } else if (msgType == OperaType.REMOVE) {
				int[] indexs = getTableCacheHandle().remove(
					VisualTableUtil.toPrimaryKeys(beans));

				TableShowContext<T> newTableShowContext = model
					.getTableShowContext().clone();
				newTableShowContext.setIndexs(indexs);
				tableRemove(model.getTable()
					.getSelectedRowIds(), model
					.getTableShowContext(),
					newTableShowContext);
			    }
			}
		    } catch (InterruptedException e) {
			logger.log(Level.WARNING, e.getMessage());
		    } catch (IllegalArgumentException e) {
			logger.log(Level.WARNING, e.getMessage());
		    } catch (IllegalAccessException e) {
			logger.log(Level.WARNING, e.getMessage());
		    } catch (InvocationTargetException e) {
			logger.log(Level.WARNING, e.getMessage());
		    }
		}
	    }
	};
	thread.start();
    }

    public void add(List<T> beans, boolean sync) {
	if (!sync) {
	    try {
		List<BatchBean<T>> batchBeans = VisualTableUtil
			.beanToBatchBeanForAdd(beans);
		this.queue.put(batchBeans);

	    } catch (InterruptedException e) {
		logger.log(Level.WARNING, e.getMessage());
	    }
	    return;
	}

	int[] indexs = getTableCacheHandle().add(beans);

	TableShowContext<T> newTableShowContext = this.model
		.getTableShowContext().clone();
	newTableShowContext.setIndexs(indexs);
	tableAdd(this.model.getTableShowContext(), newTableShowContext);
    }

    public void update(Map<Integer, T> beanMap, boolean sync) {
	if (!sync) {
	    try {
		List<BatchBean<T>> batchBeans = VisualTableUtil
			.beanToBatchBeanForUpdate(beanMap);
		queue.put(batchBeans);
	    } catch (InterruptedException e) {
		logger.log(Level.WARNING, e.getMessage());
	    }
	    return;
	}
	getTableCacheHandle().setRowDatas(beanMap);
	tableUpdate(this.model.getTableShowContext(), this.model
		.getTableShowContext().clone());
    }

    public void remove(int[] primaryKeys, boolean sync) {
	if (!sync) {
	    try {
		List<BatchBean<T>> batchBeans = VisualTableUtil
			.beanToBatchBeanForRemove(primaryKeys);
		queue.put(batchBeans);
	    } catch (InterruptedException e) {
		logger.log(Level.WARNING, e.getMessage());
		;
	    }
	    return;
	}
	int[] indexs = getTableCacheHandle().remove(primaryKeys);

	TableShowContext<T> newTableShowContext = this.model
		.getTableShowContext().clone();
	newTableShowContext.setIndexs(indexs);
	tableRemove(this.model.getTable().getSelectedRowIds(), this.model
		.getTableShowContext(), newTableShowContext);
    }

    public void sortChange(RowSorter.SortKey newSortKey) {
	synchronized (this.sortAndSearchLock) {
	    int[] lastSelectPrimaryKeys = model.getTable().getSelectedRowIds();
	    this.sortAndSearch = true;
	    int[] indexs = getTableCacheHandle().getKeys(newSortKey,
		    this.model.getTableShowContext().getSearchCondition());

	    TableShowContext<T> oldTableShowContext = this.model
		    .getTableShowContext();
	    TableShowContext<T> newTableShowContext = this.model
		    .getTableShowContext().clone();
	    newTableShowContext.setIndexs(indexs);
	    newTableShowContext.setSortKey(newSortKey);
	    this.getTableCacheHandle().updateCacheMap(oldTableShowContext,
		    newTableShowContext);
	    this.model.setTableShowContext(newTableShowContext);
	    this.model.getTable().setSelectByRowIds(lastSelectPrimaryKeys);
	    this.model.getTable().revalidate();
	    this.model.getTable().repaint();
	    this.sortAndSearch = false;
	}
    }

    public void searchChange(SearchCondition newSearchCondition) {
	synchronized (this.sortAndSearchLock) {
	    int[] lastSelectPrimaryKeys = model.getTable().getSelectedRowIds();
	    this.sortAndSearch = true;
	    int[] indexs = getTableCacheHandle().getKeys(
		    this.model.getTableShowContext().getSortKey(),
		    newSearchCondition);

	    TableShowContext<T> oldTableShowContext = this.model
		    .getTableShowContext();
	    TableShowContext<T> newTableShowContext = this.model
		    .getTableShowContext().clone();
	    newTableShowContext.setIndexs(indexs);
	    newTableShowContext.setSearchCondition(newSearchCondition);
	    this.getTableCacheHandle().updateCacheMap(oldTableShowContext,
		    newTableShowContext);

	    this.model.setTableShowContext(newTableShowContext);
	    this.model.getTable().setSelectByRowIds(lastSelectPrimaryKeys);
	    if (oldTableShowContext.getRowCount() != newTableShowContext
		    .getRowCount()) {
		model.firePropertyChange(new PropertyChangeEvent(this,
			VisualTableModel.PROPERTY_MAX_COUNT_CHANGE,
			oldTableShowContext.getRowCount(), newTableShowContext
				.getRowCount()));
	    }
	    this.model.getTable().revalidate();
	    this.model.getTable().repaint();
	    this.sortAndSearch = false;
	}
    }

    /**
     * table view port change
     * 
     * @param oldTableShowContext
     * @param newTableShowContext
     */
    public void tableViewportChange(TableShowContext<T> oldTableShowContext,
	    TableShowContext<T> newTableShowContext) {
	// 视口变化
	if (oldTableShowContext.getStartShowRow() != newTableShowContext
		.getStartShowRow()
		|| oldTableShowContext.getEndShowRow() != newTableShowContext
			.getEndShowRow()) {
	    getTableCacheHandle().updateCacheMap(oldTableShowContext,
		    newTableShowContext);
	    model.setTableShowContext(newTableShowContext);
	    model.getTable().revalidate();
	    model.getTable().repaint();
	}
    }

    public void tableAdd(TableShowContext oldTableShowContext,
	    TableShowContext newTableShowContext) {
	this.model.setTableShowContext(newTableShowContext);
	this.getTableCacheHandle().updateCacheMap(oldTableShowContext,
		newTableShowContext);
	this.model.getTable().revalidate();
	this.model.getTable().repaint();

	if (oldTableShowContext.getRowCount() != newTableShowContext
		.getRowCount()) {
	    model.firePropertyChange(new PropertyChangeEvent(this,
		    VisualTableModel.PROPERTY_MAX_COUNT_CHANGE,
		    oldTableShowContext.getRowCount(), newTableShowContext
			    .getRowCount()));
	}
    }

    public void tableUpdate(TableShowContext oldTableShowContext,
	    TableShowContext newTableShowContext) {
	this.getTableCacheHandle().updateCacheMap(oldTableShowContext,
		newTableShowContext);
	this.model.getTable().revalidate();
	this.model.getTable().repaint();
    }

    public void tableRemove(int[] lastSelectKeys,
	    TableShowContext oldTableShowContext,
	    TableShowContext newTableShowContext) {
	getTableCacheHandle().updateCacheMap(oldTableShowContext,
		newTableShowContext);
	model.getTable().revalidate();
	model.getTable().repaint();
	model.getTable().setSelectByRowIds(lastSelectKeys);
	if (oldTableShowContext.getRowCount() != newTableShowContext
		.getRowCount()) {
	    model.firePropertyChange(new PropertyChangeEvent(this,
		    VisualTableModel.PROPERTY_MAX_COUNT_CHANGE,
		    oldTableShowContext.getRowCount(), newTableShowContext
			    .getRowCount()));
	}
    }

    public TableCacheHandle<T> getTableCacheHandle() {
	return tableCacheHandle;
    }

    public void destroy() {
	this.destroyed = true;
	this.queue.destroy();
	getTableCacheHandle().destroy();
    }
}
