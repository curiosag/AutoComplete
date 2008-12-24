/*
 * 12/21/2008
 *
 * AutoCompletion.java - Handles auto-completion for a text component.
 * Copyright (C) 2008 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 */
package org.fife.ui.autocomplete;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;


/**
 * Adds autocompletion to a text component.  Provides a popup window with a
 * list of autocomplete choices on a given keystroke, such as Crtrl+Space.<p>
 *
 * @author Robert Futrell
 * @version 1.0
 */
/*
 * This class handles intercepting window and hierarchy events from the text
 * component, so the popup window is only visible when it should be visible.
 * It also handles communication between the CompletionProvider and the actual
 * popup Window.
 */
public class AutoCompletion implements HierarchyListener, ComponentListener {

	/**
	 * The text component we're providing completion for.
	 */
	private JTextComponent textComponent;

	/**
	 * The parent window of {@link #textComponent}.
	 */
	private Window parentWindow;

	/**
	 * The popup window containing completion choices.
	 */
	private AutoCompletePopupWindow popupWindow;

	/**
	 * Provides the completion options relevant to the current caret position.
	 */
	private CompletionProvider provider;

	/**
	 * The handler to use when an external URL is clicked in the help
	 * documentation.
	 */
	private ExternalURLHandler externalURLHandler;

	/**
	 * Whether the description window should be displayed along with the
	 * completion choice window.
	 */
	private boolean showDescWindow;

	/**
	 * Whether autocomplete is enabled.
	 */
	private boolean autoCompleteEnabled;

	/**
	 * Whether or not, when there is only a single auto-complete option
	 * that maches the text at the current text position, that text should
	 * be auto-inserted, instead of the completion window displaying.
	 */
	private boolean autoCompleteSingleChoices;

	/**
	 * The keystroke that triggers the completion window.
	 */
	private KeyStroke trigger;

	/**
	 * The action previously assigned to {@link #trigger}, so we can reset it
	 * if the user disables auto-completion.
	 */
	private Action oldTriggerAction;


	/**
	 * Constructor.
	 *
	 * @param provider The completion provider.  This cannot be
	 *        <code>null</code>.
	 */
	public AutoCompletion(CompletionProvider provider) {
		setCompletionProvider(provider);
		setTriggerKey(getDefaultTriggerKey());
		setAutoCompleteEnabled(true);
		setAutoCompleteSingleChoices(true);
		setShowDescWindow(false);
	}


	public void componentHidden(ComponentEvent e) {
		hidePopupWindow();
	}


	public void componentMoved(ComponentEvent e) {
		hidePopupWindow();
	}


	public void componentResized(ComponentEvent e) {
		hidePopupWindow();
	}


	public void componentShown(ComponentEvent e) {
	}


	public void doCompletion() {
		Completion comp = popupWindow.getSelection();
		doCompletionImpl(comp);
	}


	private void doCompletionImpl(Completion c) {

		JTextComponent textComp = getTextComponent();
		String alreadyEntered = c.getAlreadyEntered(textComp);
		hidePopupWindow();
		Caret caret = textComp.getCaret();

		int dot = caret.getDot();
		caret.setDot(dot - alreadyEntered.length());
		caret.moveDot(dot);
		textComp.replaceSelection(c.getReplacementText());
/*
		Document doc = textComp.getDocument();
		int end = caret.getDot();
		int start = end - alreadyEntered.length();
try {
		if (doc instanceof AbstractDocument) {
			((AbstractDocument)doc).replace(start, end-start, c.getReplacementText(), null);
		}
		else {
			doc.remove(start, end-start);
			doc.insertString(start, c.getReplacementText(), null);
		}
} catch (javax.swing.text.BadLocationException ble) { ble.printStackTrace(); }
*/
	}


	/**
	 * Returns whether, if a single autocomplete choice is available, it should
	 * be automatically inserted, without displaying the popup menu.
	 *
	 * @return Whether to autocomplete single choices.
	 * @see #setAutoCompleteSingleChoices(boolean)
	 */
	public boolean getAutoCompleteSingleChoices() {
		return autoCompleteSingleChoices;
	}


	/**
	 * Returns the default autocomplete "trigger key" for this OS.  For
	 * Windows, for example, it is Ctrl+Space.
	 *
	 * @return The default autocomplete trigger key.
	 */
	public static KeyStroke getDefaultTriggerKey() {
		// Default to CTRL, even on Mac, since Ctrl+Space activates Spotlight
		int mask = Event.CTRL_MASK;
		return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, mask);
	}


	/**
	 * Returns the handler to use when an external URL is clicked in the
	 * description window.
	 *
	 * @return The handler.
	 * @see #setExternalURLHandler(ExternalURLHandler)
	 */
	public ExternalURLHandler getExternalURLHandler() {
		return externalURLHandler;
	}


	int getLineOfCaret() {
		Document doc = textComponent.getDocument();
		Element root = doc.getDefaultRootElement();
		return root.getElementIndex(textComponent.getCaretPosition());
	}


	CompletionProvider getProviderAtCaretPosition() {
		// TODO: Delegate to provider in case the provider itself delegates.
		//return provider.getProviderAtCaretPosition(textComponent);
		return provider;
	}


	/**
	 * Returns whether the "description window" should be shown alongside
	 * the completion window.
	 *
	 * @return Whether the description window should be shown.
	 * @see #setShowDescWindow(boolean)
	 */
	public boolean getShowDescWindow() {
		return showDescWindow;
	}


	/**
	 * Returns the text component for which autocompletion is enabled.
	 *
	 * @return The text component, or <code>null</code> if this
	 *         {@link AutoCompletion} is not installed on any text component.
	 * @see #install(JTextComponent)
	 */
	public JTextComponent getTextComponent() {
		return textComponent;
	}


	/**
	 * Returns the "trigger key" used for autocomplete.
	 *
	 * @return The trigger key.
	 * @see #setTriggerKey(KeyStroke)
	 */
	public KeyStroke getTriggerKey() {
		return trigger;
	}


	/**
	 * Called when the component hierarchy for our text component changes.
	 * When the text component is added to a new {@link Window}, this method
	 * registers listeners on that <code>Window</code>.
	 *
	 * @param e The event.
	 */
	public void hierarchyChanged(HierarchyEvent e) {

		// NOTE: e many be null as we call this method at other times.
		//System.out.println("Hierarchy changed! " + e);

		Window oldParentWindow = parentWindow;
		parentWindow = SwingUtilities.getWindowAncestor(textComponent);
		if (parentWindow!=oldParentWindow) {
			if (oldParentWindow!=null) {
				oldParentWindow.removeComponentListener(this);
			}
			if (parentWindow!=null) {
				parentWindow.addComponentListener(this);
			}
		}

	}


	private void hidePopupWindow() {
		if (popupWindow!=null) {
			if (popupWindow.isVisible()) {
				popupWindow.setVisible(false);
			}
		}
	}


	/**
	 * Installs this autocompletion on a text component.  If this
	 * {@link AutoCompletion} is already installed on another text component,
	 * it is uninstalled first.
	 *
	 * @param c The text component.
	 * @see #uninstall()
	 */
	public void install(JTextComponent c) {

		if (textComponent!=null) {
			uninstall();
		}

		this.textComponent = c;
		installTriggerKey(getTriggerKey());

		this.textComponent.addHierarchyListener(this);
		hierarchyChanged(null); // In case textComponent is already in a window

	}


	/**
	 * Installs a "trigger key" action onto the current text component.
	 *
	 * @param ks The keystroke that should trigger the action.
	 * @see #uninstallTriggerKey() 
	 */
	private void installTriggerKey(KeyStroke ks) {
		Keymap km = textComponent.getKeymap();
		oldTriggerAction = km.getAction(ks);
		km.addActionForKeyStroke(ks, new AutoCompleteAction());
	}


	/**
	 * Returns whether autocompletion is enabled.
	 *
	 * @return Whether autocompletion is enabled.
	 * @see #setAutoCompleteEnabled(boolean)
	 */
	public boolean isAutoCompleteEnabled() {
		return autoCompleteEnabled;
	}


	private boolean isPopupVisible() {
		return popupWindow!=null && popupWindow.isVisible();
	}


	/**
	 * Refreshes the popup window.  First, this method gets the possible
	 * completions for the current caret position.  If there are none, and the
	 * popup is visible, it is hidden.  If there are some completions and the
	 * popup is hidden, it is made visible and made to display the completions.
	 * If there are some completions and the popup is visible, its list is
	 * updated to the current set of completions.
	 *
	 * @return The current line number of the caret.
	 */
	protected int refreshPopupWindow() {

		final List completions = provider.getCompletions(textComponent);
		int count = completions.size();

		if (count>1 || (count==1 && isPopupVisible()) ||
				(count==1 && !getAutoCompleteSingleChoices())) {

			if (popupWindow==null) {
				popupWindow = new AutoCompletePopupWindow(parentWindow, this);
			}

			popupWindow.setCompletions(completions);
//			popupWindow.clear();
//			for (int i=0; i<completions.size(); i++) {
//				
//				popupWindow.addItem((Completion)completions.get(i));
//			}
			popupWindow.selectFirstItem();

			if (!popupWindow.isVisible()) {
				Rectangle r = null;
				try {
					r = textComponent.modelToView(textComponent.
														getCaretPosition());
				} catch (BadLocationException ble) {
					ble.printStackTrace();
					return -1;
				}
				Point p = new Point(r.x, r.y);
				SwingUtilities.convertPointToScreen(p, textComponent);
				r.x = p.x;
				r.y = p.y;
				popupWindow.setLocationRelativeTo(r);
				popupWindow.setVisible(true);
			}

		}

		else if (count==1) { // !isPopupVisible && autoCompleteSingleChoices
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					doCompletionImpl((Completion)completions.get(0));
				}
			});
		}

		else {
			hidePopupWindow();
		}

		return getLineOfCaret();

	}


	/**
	 * Sets whether autocompletion is enabled.
	 *
	 * @param enabled Whether autocompletion is enabled.
	 * @see #isAutoCompleteEnabled()
	 */
	public void setAutoCompleteEnabled(boolean enabled) {
		if (enabled!=autoCompleteEnabled) {
			autoCompleteEnabled = enabled;
			hidePopupWindow();
		}
	}


	/**
	 * Sets whether, if a single autocomplete choice is available, it should
	 * be automatically inserted, without displaying the popup menu.
	 *
	 * @param autoComplete Whether to autocomplete single choices.
	 * @see #getAutoCompleteSingleChoices()
	 */
	public void setAutoCompleteSingleChoices(boolean autoComplete) {
		autoCompleteSingleChoices = autoComplete;
	}


	/**
	 * Sets the completion provider being used.
	 *
	 * @param provider The new completion provider.  This cannot be
	 *        <code>null</code>.
	 * @throws IllegalArgumentException If <code>provider</code> is
	 *         <code>null</code>.
	 */
	public void setCompletionProvider(CompletionProvider provider) {
		if (provider==null) {
			throw new IllegalArgumentException("provider cannot be null");
		}
		this.provider = provider;
		hidePopupWindow(); // In case new choices should be displayed.
	}


	/**
	 * Sets the handler to use when an external URL is clicked in the
	 * description window.  This handler can perform some action, such as
	 * open the URL in a web browser.  The default implementation will open
	 * the URL in a browser, but only if running in Java 6.  If you want
	 * browser support for Java 5 and below, you will have to install your own
	 * handler to do so.
	 *
	 * @param handler The new handler.
	 * @see #getExternalURLHandler()
	 */
	public void setExternalURLHandler(ExternalURLHandler handler) {
		this.externalURLHandler = handler;
	}


	/**
	 * Sets whether the "description window" should be shown beside the
	 * completion window.
	 *
	 * @param show Whether to show the description window.
	 * @see #getShowDescWindow()
	 */
	public void setShowDescWindow(boolean show) {
		showDescWindow = show;
	}


	/**
	 * Sets the keystroke that should be used to trigger the autocomplete
	 * popup window.
	 *
	 * @param ks The keystroke.
	 * @throws IllegalArgumentException If <code>ks</code> is <code>null</code>.
	 * @see #getTriggerKey()
	 */
	public void setTriggerKey(KeyStroke ks) {
		if (ks==null) {
			throw new IllegalArgumentException("trigger key cannot be null");
		}
		if (!ks.equals(trigger)) {
			if (textComponent!=null) {
				// Put old trigger action back.
				uninstallTriggerKey();
				// Grab current action for new trigger and replace it.
				installTriggerKey(ks);
			}
			trigger = ks;
		}
	}


	/**
	 * Uninstalls this autocompletion from its text component.  If it is not
	 * installed on any text component, nothing happens.
	 *
	 * @see #install(JTextComponent)
	 */
	public void uninstall() {
		if (textComponent!=null) {
			hidePopupWindow(); // Unregisters listeners, actions, etc.
			uninstallTriggerKey();
			textComponent.removeHierarchyListener(this);
			if (parentWindow!=null) {
				parentWindow.removeComponentListener(this);
			}
			textComponent = null;
		}
	}


	/**
	 * Replaces the "trigger key" action with the one that was there
	 * before autocompletion was installed.
	 *
	 * @see #installTriggerKey(KeyStroke)
	 */
	private void uninstallTriggerKey() {
		Keymap km = textComponent.getKeymap();
		if (oldTriggerAction!=null) {
			km.addActionForKeyStroke(trigger, oldTriggerAction);
		}
		else {
			km.removeKeyStrokeBinding(trigger);
		}
	}


	/**
	 * Updates the LookAndFeel of the popup window.  Applications can call
	 * this method as appropriate if they support changing the LookAndFeel
	 * at runtime.
	 */
	public void updateUI() {
		if (popupWindow!=null) {
			popupWindow.updateUI();
		}
	}


	/**
	 * The <code>Action</code> that displays the popup window if autocompletion
	 * is enabled.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	class AutoCompleteAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isAutoCompleteEnabled()) {
				refreshPopupWindow();
			}
			else if (oldTriggerAction!=null) {
				oldTriggerAction.actionPerformed(e);
			}
		}

	}


}