package org.fife.ui.autocomplete;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;

public class NestedPopup extends JWindow {

	private JList list;

	public NestedPopup(Window parent, final AutoCompletion ac) {
		super(parent);

		String[] listData = { "one", "two", "three", "four", "five", "six", "seven" };
		list = new JList(listData);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane listPane = new JScrollPane(list);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(listPane);
		
		getRootPane().setContentPane(contentPane);
		
		pack();


	}

}
