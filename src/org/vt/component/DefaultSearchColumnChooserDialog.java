package org.vt.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

/**
 * define default columns to search
 * 
 * @author Zerg Law
 * 
 */
public class DefaultSearchColumnChooserDialog extends JDialog {

    private JVisualTable table;
    private CheckboxList list = new CheckboxList(new DefaultListModel());
    private JButton okButton = new JButton("Ok");
    private JButton cancelButton = new JButton("Cancel");
    private ResourceBundle bundle = ResourceManager
	    .getBundle(VTResourceBundle.class);

    public DefaultSearchColumnChooserDialog(JVisualTable table) {
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
	this.setTitle(bundle.getString("pls_select_search"));
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
	int index = 0;
	for (int i = 0; i < columnBeans.length; i++) {
	    ColumnBean columnBean = columnBeans[i];
	    if (columnBean.isShowInTable() && !columnBean.isHidden()) {
		listModel.addElement(columnBean.getColumnName());
		if (columnBean.isDefaultSearch()) {
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
			    DefaultSearchColumnChooserDialog.this, bundle
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
			columnBean.setDefaultSearch(exist(indexs, index));
			index++;
		    }
		}

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
