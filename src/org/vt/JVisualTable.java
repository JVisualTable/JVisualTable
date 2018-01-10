package org.vt;

import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.RowSorterEvent;

import org.vt.cache.orm.ColumnBean;
import org.vt.component.VBasicTableUI;
import org.vt.component.VisualTableModel;
import org.vt.core.VisualTableRowSorter;
import org.vt.util.VisualTableUtil;

/**
 * Java Swing Visual Table, Support million's data display.
 * 
 * @author Zerg Law
 * 
 */
public class JVisualTable extends JTable {

    public JVisualTable(VisualTableModel model) {
	super(model);
	model.setTable(this);
	this.setRowSorter(new VisualTableRowSorter(model));
	initDefaultSearchColumns();
	setUI(new VBasicTableUI());
    }

    private boolean hasDefaultSearchColumns() {
	ColumnBean[] columnBeans = ((VisualTableModel) getModel())
		.getColumnBeans();
	if (columnBeans != null) {
	    for (ColumnBean columnBean : columnBeans) {
		if (columnBean.isShowInTable() && !columnBean.isHidden()) {
		    if (columnBean.isDefaultSearch()) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    @Override
    public void removeNotify() {
	((VisualTableModel) getModel()).destroy();
	super.removeNotify();
    }

    private void initDefaultSearchColumns() {
	if (hasDefaultSearchColumns()) {
	    return;
	}
	ColumnBean[] columnBeans = ((VisualTableModel) getModel())
		.getColumnBeans();
	if (columnBeans != null) {
	    for (ColumnBean columnBean : columnBeans) {
		if (columnBean.isShowInTable() && !columnBean.isHidden()) {
		    columnBean.setDefaultSearch(true);
		}
	    }
	}
    }

    /**
     * Get Selected Row Id
     * 
     * @return getSelectedRowIds
     */
    public int[] getSelectedRowIds() {
	int[] lastSelectRows = getSelectedRows();
	int[] lastRowIds = null;
	if (lastSelectRows != null && lastSelectRows.length > 0) {
	    lastRowIds = ((VisualTableModel) getModel())
		    .getPrimaryKeys(lastSelectRows);
	}
	return lastRowIds;
    }

    public void setSelectByRowIds(int[] rowIds) {
	VisualTableModel model1 = (VisualTableModel) getModel();
	if (rowIds != null && rowIds.length > 0) {
	    int[] rowIndexs = model1.getRowsByPrimaryKeys(rowIds);
	    if (rowIndexs.length == 0) {
		return;
	    }
	    List<Integer> newSelectRows = VisualTableUtil.toList(rowIndexs);
	    Collections.sort(newSelectRows);

	    int fromIndex = newSelectRows.get(0);
	    int toIndex = newSelectRows.get(newSelectRows.size() - 1);
	    getSelectionModel().setSelectionInterval(fromIndex, toIndex);
	    List<Integer[]> ranges = VisualTableUtil.getRangeArr(newSelectRows);
	    if (ranges.size() > 1) {
		for (int i = 1; i < ranges.size(); i++) {
		    int from = ranges.get(i - 1)[1] + 1;
		    int to = ranges.get(i)[1] - 1;
		    if (to >= from) {
			getSelectionModel().removeSelectionInterval(from, to);
		    }
		}
	    }
	}
    }

    @Override
    public void sorterChanged(RowSorterEvent e) {
	VisualTableModel model1 = (VisualTableModel) getModel();
	List<RowSorter.SortKey> list = e.getSource().getSortKeys();
	model1.sortChange(list.size() > 0 ? list.get(0) : null);
    }

}
