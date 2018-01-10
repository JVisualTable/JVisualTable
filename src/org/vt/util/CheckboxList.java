package org.vt.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.vt.component.CheckboxListSelectionModel;

/**
 * CheckboxList
 * 
 * @author Zerg Law
 * 
 */
public class CheckboxList extends JList {
    private static final long serialVersionUID = 4038056168865938775L;

    private CheckboxListSelectionModel selectModel;

    public CheckboxList() {
	super();
	initDefaults();
    }

    public CheckboxList(ListModel dataModel) {
	super(dataModel);
	initDefaults();
    }

    protected void initDefaults() {
	this.selectModel = createDefaultSelectionModel();
	setSelectionModel(selectModel);
	setCellRenderer(createDefaultCellRenderer());
	setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void addCheckedIndex(int index) {
	selectModel.addCheckedIndex(index);
    }

    public void addCheckedIndexs(int index0, int index1) {
	selectModel.addCheckedIndexs(index0, index1);
    }

    public void setCheckedIndexs(int... values) {
	selectModel.setCheckedIndexs(values);
    }

    public void removeCheckedIndex(int index) {
	selectModel.removeCheckedIndex(index);
    }

    public void removeCheckedIndexs(int... values) {
	selectModel.removeCheckedIndexs(values);
    }

    public void removeCheckedIndexs(int index0, int index1) {
	selectModel.removeCheckedIndexs(index0, index1);
    }

    public int[] getCheckedIndexs() {
	return selectModel.getCheckedIndexs();
    }

    public Object[] getCheckedValues() {
	int[] indexs = selectModel.getCheckedIndexs();

	List<Object> list = new ArrayList<Object>();
	ListModel model = getModel();
	for (int i = 0; i < indexs.length; i++) {
	    int index = indexs[i];
	    if (model.getSize() > index) {
		list.add(model.getElementAt(index));
	    }

	}

	return list.toArray();
    }

    public boolean isCheckedIndex(int index) {
	return super.isSelectedIndex(index);
    }

    public CheckboxListSelectionModel getSelectModel() {
	return selectModel;
    }

    public void setSelectModel(CheckboxListSelectionModel selectModel) {
	this.selectModel = selectModel;
    }

    protected ListCellRenderer createDefaultCellRenderer() {
	return new CheckListRenderer();
    }

    protected CheckboxListSelectionModel createDefaultSelectionModel() {
	return new CheckboxListSelectionModel();
    }

    static class CheckListRenderer extends JCheckBox implements
	    ListCellRenderer {
	private static final long serialVersionUID = 6011489891472438541L;

	public CheckListRenderer() {
	    setBackground(UIManager.getColor("List.textBackground"));
	    setForeground(UIManager.getColor("List.textForeground"));
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean hasFocus) {
	    setEnabled(list.isEnabled());
	    CheckboxListSelectionModel model = (CheckboxListSelectionModel) list
		    .getSelectionModel();
	    setSelected(model.containIndex(index));
	    setText(value.toString());
	    if (hasFocus) {
		setBackground(list.getSelectionBackground());
		setForeground(list.getSelectionForeground());
	    } else {
		setBackground(list.getBackground());
		setForeground(list.getForeground());
	    }

	    return this;
	}
    }

}
