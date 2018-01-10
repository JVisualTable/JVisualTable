package org.vt.component;

import java.util.Collections;
import java.util.LinkedList;

import javax.swing.DefaultListSelectionModel;

/**
 * checkbox list select model
 * 
 * @author Zerg Law
 * 
 */
public class CheckboxListSelectionModel extends DefaultListSelectionModel {
    private static final long serialVersionUID = -1431940227742917947L;
    private LinkedList<Integer> indexs = new LinkedList<Integer>();

    @Override
    public void setSelectionInterval(int index0, int index1) {
	super.setSelectionInterval(index0, index1);
	int setMin = Math.min(index0, index1);
	int setMax = Math.max(index0, index1);
	for (int i = setMin; i <= setMax; i++) {
	    if (indexs.contains(i)) {
		indexs.remove(Integer.valueOf(i));
	    } else {
		indexs.add(Integer.valueOf(i));
	    }
	}

	super.fireValueChanged(index0, index1);
    }

    /**
     * superSetSelectionInterval
     * 
     * @param index0
     *            .
     * @param index1
     *            .
     */
    protected void superSetSelectionInterval(int index0, int index1) {
	super.setSelectionInterval(index0, index1);
    }

    /**
     * add
     * 
     * @param index0
     *            .
     * @param index1
     *            .
     */
    public void addCheckedIndexs(int index0, int index1) {
	int setMin = Math.min(index0, index1);
	int setMax = Math.max(index0, index1);
	for (int i = setMin; i <= setMax; i++) {
	    if (!indexs.contains(i)) {
		indexs.add(Integer.valueOf(i));
	    }
	}

	super.fireValueChanged(index0, index1);
    }

    /**
     * set
     * 
     * @param values
     *            .
     */
    public void setCheckedIndexs(int... values) {
	for (int i = 0; i < values.length; i++) {
	    if (!indexs.contains(i)) {
		indexs.add(values[i]);
	    }
	}
	super.fireValueChanged(values[0], values[values.length - 1]);
    }

    /**
     * add new checkbox
     * 
     * @param index
     *            .
     */
    public void addCheckedIndex(int index) {
	if (!indexs.contains(index)) {
	    indexs.add(Integer.valueOf(index));
	}
	super.fireValueChanged(index, index);
    }

    /**
     * remove single
     * 
     * @param index
     *            .
     */
    public void removeCheckedIndex(int index) {
	indexs.remove(Integer.valueOf(index));
	super.fireValueChanged(index, index);
    }

    /**
     * remove some
     * 
     * @param index0
     *            .
     * @param index1
     *            .
     */
    public void removeCheckedIndexs(int index0, int index1) {
	int setMin = Math.min(index0, index1);
	int setMax = Math.max(index0, index1);
	for (int i = setMin; i <= setMax; i++) {
	    indexs.remove(Integer.valueOf(i));
	}

	super.fireValueChanged(index0, index1);
    }

    /**
     * remove checked indexs
     * 
     * @param values
     *            .
     */
    public void removeCheckedIndexs(int... values) {
	for (int i = 0; i < values.length; i++) {
	    indexs.remove(values[i]);
	}
	super.fireValueChanged(values[0], values[values.length - 1]);
    }

    /**
     *remove index from index0 to index1
     * 
     * @param index0
     *            .
     * @param index1
     *            .
     */
    public void removeCheckedIndex(int index0, int index1) {
	int setMin = Math.min(index0, index1);
	int setMax = Math.max(index0, index1);
	for (int i = setMin; i <= setMax; i++) {
	    indexs.remove(Integer.valueOf(i));
	}

	super.fireValueChanged(index0, index1);
    }

    /**
     * Get Checked Indexs
     * 
     * @return .
     */
    public int[] getCheckedIndexs() {
	Collections.sort(indexs);
	int[] values = new int[indexs.size()];
	for (int i = 0; i < values.length; i++) {
	    values[i] = indexs.get(i);
	}
	return values;
    }

    @Override
    public boolean isSelectedIndex(int index) {
	if (indexs.contains(index)) {
	    return true;
	}
	return false;
    }

    /**
     * contains index
     * 
     * @param index
     *            index .
     * @return .
     */
    public boolean containIndex(int index) {
	return indexs.contains(index);
    }

    @Override
    public void clearSelection() {
	super.clearSelection();
	indexs.clear();

    }

    @Override
    public boolean isSelectionEmpty() {
	return indexs.isEmpty();
    }

    public LinkedList<Integer> getIndexs() {
	return indexs;
    }

}