package org.vt.core;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.vt.component.VisualTableModel;

/**
 * replace old table sorter
 * 
 * @author Zerg Law
 * 
 */
public class VisualTableRowSorter extends TableRowSorter {
    public VisualTableRowSorter(TableModel tableModel) {
	super(tableModel);
	setMaxSortKeys(3);
    }

    public int convertRowIndexToModel(int index) {
	// VisualTableModel model = (VisualTableModel) getModelWrapper()
	// .getModel();
	// return model.getVisualVector().convertRowIndexToModel(index);
	return index;
    }

    public void rowsDeleted(int firstRow, int endRow) {
	VisualTableModel model = (VisualTableModel) getModelWrapper()
		.getModel();

	return;
    }

    public void rowsInserted(int firstRow, int endRow) {

	return;
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow) {
	VisualTableModel model = (VisualTableModel) getModelWrapper()
		.getModel();
	return;
    }

    @Override
    public void toggleSortOrder(int column) {
	if (column < 0 || column >= getModelWrapper().getColumnCount()) {
	    throw new IndexOutOfBoundsException(
		    "column beyond range of TableModel");
	}
	if (isSortable(column)) {
	    List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
	    SortKey sortKey;
	    int sortIndex;
	    for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
		if (keys.get(sortIndex).getColumn() == column) {
		    break;
		}
	    }
	    if (sortIndex == -1) {
		// Key doesn't exist
		sortKey = new SortKey(column, SortOrder.ASCENDING);
		keys.add(0, sortKey);
	    } else if (sortIndex == 0) {
		// It's the primary sorting key, toggle it
		SortKey key = toggle(keys.get(sortIndex));
		if (key == null) {
		    keys.remove(sortIndex);
		} else {
		    keys.set(sortIndex, key);
		}
	    } else {
		// It's not the first, but was sorted on, remove old
		// entry, insert as first with ascending.
		keys.remove(sortIndex);
		keys.add(0, new SortKey(column, SortOrder.ASCENDING));
	    }
	    if (keys.size() > getMaxSortKeys()) {
		keys = keys.subList(0, getMaxSortKeys());
	    }
	    setSortKeys(keys);
	}
    }

    private SortKey toggle(SortKey key) {
	if (key.getSortOrder() == SortOrder.ASCENDING) {
	    return new SortKey(key.getColumn(), SortOrder.DESCENDING);
	} else {
	    return null;
	}
    }

    @Override
    public void sort() {
	return;
    }
}
