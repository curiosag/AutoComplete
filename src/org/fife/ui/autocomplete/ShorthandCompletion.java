/*
 * 12/22/2008
 *
 * ShorhandCompletion.java - A completion that is shorthand for some other
 * text.
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


/**
 * A completion where the input text is shorthand for (really, just different
 * than) the actual text to be inserted.  For example, the input text
 * "<code>sysout</code>" could be associated with the completion
 * "<code>System.out.println(</code>" in Java.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ShorthandCompletion extends WordCompletion {

	/**
	 * The text the user can start typing that will match this completion.
	 */
	private String inputText;


	/**
	 * Constructor.
	 *
	 * @param provider The provider that returns this completion.
	 * @param inputText The text the user inputs to get this completion.
	 * @param replacementText The replacement text of the completion.
	 */
	public ShorthandCompletion(CompletionProvider provider, String inputText,
								String replacementText) {
		super(provider, replacementText);
		this.inputText = inputText;
	}


	/**
	 * Returns the replacement text.  Subclasses can override this method to
	 * return a more detailed description.
	 *
	 * @return A description of this completion (the text that will be
	 *         inserted).
	 * @see #getReplacementText()
	 */
	public String getSummary() {
		return "<html><body><tt>" + getReplacementText();
	}


	/**
	 * Returns the text the user must start typing to get this completion.
	 *
	 * @return The text the user must start to input.
	 */
	public String getInputText() {
		return inputText;
	}


}