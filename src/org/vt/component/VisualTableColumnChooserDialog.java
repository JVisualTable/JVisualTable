package org.vt.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vt.JVisualTable;
import org.vt.cache.orm.ColumnBean;
import org.vt.locale.ResourceManager;
import org.vt.locale.VTResourceBundle;
import org.vt.util.CheckboxList;
import org.vt.util.DBGeneratorHandle;

/**
 * choose default column shown dialog
 * 
 * @author Zerg Law
 * 
 */
public class VisualTableColumnChooserDialog extends JDialog {
    private JVisualTable table;
    private CheckboxList list = new CheckboxList(new DefaultListModel());
    private JButton okButton = new JButton("Ok");
    private JButton cancelButton = new JButton("Cancel");
    private ResourceBundle bundle = ResourceManager
	    .getBundle(VTResourceBundle.class);

    public VisualTableColumnChooserDialog(JVisualTable table) {
	this.table = table;
	init();
	initComponent();
	initListeners();
    }

    private void init() {
	this.setSize(250, 350);
	this.setModal(true);
	this.setLocationRelativeTo(table.getRootPane());
	this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	this.setTitle(bundle.getString("pls_select_show"));
    }

    private void initComponent() {
	this.getContentPane().add(
		new JLabel(bundle.getString("column_select")),
		BorderLayout.NORTH);
	this.getContentPane().add(new JScrollPane(list));
	JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	panel.add(okButton);
	panel.add(cancelButton);
	this.getContentPane().add(panel, BorderLayout.SOUTH);

	initList();
    }

    private void initList() {
	VisualTableModel visualTableModel = (VisualTableModel) this.table
		.getModel();
	DefaultListModel listModel = (DefaultListModel) list.getModel();
	ColumnBean[] columnBeans = visualTableModel.getColumnBeans();
	List<Integer> indexs = new ArrayList<Integer>();
	int index = 0;
	for (int i = 0; i < columnBeans.length; i++) {
	    ColumnBean columnBean = columnBeans[i];
	    if (columnBean.isShowInTable()) {
		listModel.addElement(columnBean.getColumnName());
		if (!columnBean.isHidden()) {
		    list.addCheckedIndex(index);
		}
		index++;
	    }
	}
    }

    private boolean exist(int[] indexs, int index) {
	for (int i : indexs) {
	    if (i == index) {
		return true;
	    }
	}
	return false;
    }

    private void initListeners() {
	okButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		int[] indexs = list.getCheckedIndexs();
		if (indexs == null || indexs.length == 0) {
		    JOptionPane.showMessageDialog(
			    VisualTableColumnChooserDialog.this, bundle
				    .getString("pls_keep_1"), bundle
				    .getString("warning"),
			    JOptionPane.ERROR_MESSAGE);
		    return;
		}
		VisualTableModel visualTableModel = (VisualTableModel) table
			.getModel();
		ColumnBean[] columnBeans = visualTableModel.getColumnBeans();
		int index = 0;
		for (int i = 0; i < columnBeans.length; i++) {
		    ColumnBean columnBean = columnBeans[i];
		    if (columnBean.isShowInTable()) {
			columnBean.setHidden(!exist(indexs, index));
			index++;
		    }
		}

		DBGeneratorHandle.fireChangeShowTableIndexs(columnBeans);
		table.createDefaultColumnsFromModel();
		visualTableModel.firePropertyChange(new PropertyChangeEvent(
			visualTableModel,
			VisualTableModel.PROPERTY_COLUMN_CHANGE, 0, indexs));
		dispose();
	    }
	});
	cancelButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispose();
	    }
	});
    }

}
