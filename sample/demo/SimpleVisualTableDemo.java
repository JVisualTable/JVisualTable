package demo;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vt.JVisualTable;
import org.vt.component.VisualTableModel;
import org.vt.locale.ResourceManager;
import org.vt.util.ScrollBarShowTipsUtil;
import org.vt.util.VisualTableUtil;

/**
 * simple demo
 * 
 * @author Zerg Law
 * 
 */
public class SimpleVisualTableDemo {
    private JVisualTable table = new JVisualTable(new VisualTableModel(
	    SimpleBean.class));

    private JFrame createFrame() {
	JFrame frame = new JFrame("Simple Visual Table Demo");
	frame.setSize(800, 600);
	frame.setLocationRelativeTo(null);
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	return frame;
    }

    private JPanel createPanel() {
	JPanel panel = new JPanel(new BorderLayout());

	JScrollPane jsp = new JScrollPane(table);

	ScrollBarShowTipsUtil.bindVerticalScrollBarTips(table, jsp);
	VisualTableUtil.registerTableHeadPopupMenu(table);
	panel.add(jsp, BorderLayout.CENTER);
	VisualTableModel model = (VisualTableModel) table.getModel();
	model.setResourceBundle(ResourceManager.getBundle(getClass()));
	initData(model);
	return panel;
    }

    private void initData(VisualTableModel model) {
	List<SimpleBean> beans = new ArrayList<SimpleBean>();
	for (int i = 0; i < 100; i++) {
	    SimpleBean bean = new SimpleBean();
	    bean.setId(i);
	    bean.setBooleanValue(new Random().nextBoolean() + "");

	    bean.setShortValue((short) (i));

	    bean.setLongValue(Math.abs(new Random().nextLong()));

	    bean.setDoubleValue(new Random().nextDouble());

	    bean.setStringValue("Cell Value:" + i);

	    bean.setDateValue(new Date());

	    bean.setUserObject(new UserObject(bean.getId(), System
		    .currentTimeMillis()
		    + ""));
	    beans.add(bean);
	}
	model.addBeans(beans, true);
    }

    private void showDemo() {
	JFrame frame = createFrame();
	frame.getContentPane().add(createPanel());
	frame.getContentPane().add(
		VisualTableUtil.createStatusBar(null, table),
		BorderLayout.SOUTH);
	frame.setVisible(true);
    }

    public static void main(String[] args) {
	new SimpleVisualTableDemo().showDemo();

    }
}
