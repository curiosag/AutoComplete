
package org.fife.ui.autocomplete;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.CaretEvent;
import javax.swing.text.JTextComponent;

class AutoCompletePopupSubWindow extends AutoCompletePopupWindow  {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            The parent window (hosting the text component).
	 * @param ac
	 *            The auto-completion instance.
	 */
	public AutoCompletePopupSubWindow(Window parent, final AutoCompletion ac) {

		super(parent, ac);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
	}

	private KeyStroke getSelectKey() {
		int mask = InputEvent.CTRL_MASK;
		return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, mask);
	}

	private Action oldSelectionAction;
	private Object oldTriggerKey;

	public void uninstallSelectionKey() {
		JTextComponent comp = ac.getTextComponent();
		InputMap im = comp.getInputMap();
		ActionMap am = comp.getActionMap();

		im.put(getSelectKey(), oldTriggerKey);
		am.put(AutoCompletion.PARAM_TRIGGER_KEY, oldSelectionAction);
	}

	private void installSelectionKey() {
		JTextComponent comp = ac.getTextComponent();
		InputMap im = comp.getInputMap();
		ActionMap am = comp.getActionMap();

		KeyStroke ks = getSelectKey();
		oldTriggerKey = im.get(ks);
		im.put(ks, AutoCompletion.PARAM_TRIGGER_KEY);
		oldSelectionAction = am.get(AutoCompletion.PARAM_TRIGGER_KEY);
		am.put(AutoCompletion.PARAM_TRIGGER_KEY, createSelectionAction());
	}

	private Action createSelectionAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int[] sel = list.getSelectedIndices();
				int focused = list.getLeadSelectionIndex();
				//list.setSelectedIndices(invertSelection(sel, focused));
				String s = "";
				for (int i = 0; i < sel.length; i++) 
					s = s + String.valueOf(sel[i] + ";");
				System.out.println(s);
				
			}
		};
	}

	private int[] invertSelection(int[] current, int value) {
		if (true)
			return current;
		
		int[] result;
		int foundAt = Arrays.binarySearch(current, value);
		if (foundAt < 0) {
			result = Arrays.copyOf(current, current.length + 1);
			result[current.length + 1] = value;
			Arrays.sort(result);
		} else {
			current[foundAt] = Integer.MAX_VALUE;
			Arrays.sort(current);
			result = Arrays.copyOf(current, current.length - 1);
		}

		return result;
	}

	@Override
	public void caretUpdate(CaretEvent e) {
	}

	@Override

	protected void internalCreateKeyActionPairs(EnterAction enterAction) {
		super.internalCreateKeyActionPairs(enterAction);
		leftKap = new KeyActionPair("Left", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isVisible())
					setVisible(false);
			}
		});
		
		
	}

	/**
	 * Inserts the currently selected completion.
	 *
	 * @see #getSelection()
	 */
	@Override
	protected void insertSelectedCompletion() {
		Completion comp = getSelection();
		ac.insertCompletion(comp);
	}

	private Completion getCombinedSelections() {
		String selectedValue = "";

		if (!list.isSelectionEmpty()) {
			StringBuilder sb = new StringBuilder();

			int minIndex = list.getMinSelectionIndex();
			int maxIndex = list.getMaxSelectionIndex();
			for (int i = minIndex; i <= maxIndex; i++) {
				if (sb.length() > 0)
					sb.append(", ");
				if (list.isSelectedIndex(i))
					sb.append(((Completion) list.getModel().getElementAt(i)).getReplacementText());

			}
			selectedValue = sb.toString();
		}
		DefaultCompletionProvider pro = new DefaultCompletionProvider();
		BasicCompletion c = new BasicCompletion(pro , selectedValue);
		pro.addCompletion(c);
		return c;
	}

}