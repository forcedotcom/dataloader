/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dataloader.ui.extraction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
/**
 * A PopupList is a list of selectable items that appears in its own shell positioned above
 * its parent shell.  It it used for selecting items when editing a Table cell (similar to the
 * list that appears when you open a Combo box).
 *
 * The list will be positioned so that does not run off the screen and the largest number of items
 * are visible.  It may appear above the current cursor location or below it depending how close you
 * are to the edge of the screen.
 */
public class ExtrPopupList {
    Shell  shell;
    List   list;
    int    minimumWidth;
    /**
     * Creates a PopupList above the specified shell.
     * 
     * @param parent a Shell control which will be the parent of the new instance (cannot be null)
     */
    public ExtrPopupList(Shell parent) {
        this (parent, 0);
    }
    /**
     * Creates a PopupList above the specified shell.
     * 
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     * 
     * @since 3.0
     */
    public ExtrPopupList(Shell parent, int style) {
        shell = new Shell(parent, checkStyle(style));

        list = new List(shell, SWT.SINGLE | SWT.V_SCROLL);

        // close dialog if user selects outside of the shell
        shell.addListener(SWT.Deactivate, new Listener() {
            @Override
            public void handleEvent(Event e){
                shell.setVisible (false);
            }
        });

        // resize shell when list resizes
        shell.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e){}
            @Override
            public void controlResized(ControlEvent e){
                Rectangle shellSize = shell.getClientArea();
                list.setSize(shellSize.width, shellSize.height);
            }
        });

        // return list selection on Mouse Up or Carriage Return
        list.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e){}
            @Override
            public void mouseDown(MouseEvent e){}
            @Override
            public void mouseUp(MouseEvent e){
                shell.setVisible (false);
            }
        });
        list.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e){}
            @Override
            public void keyPressed(KeyEvent e){
                if (e.character == '\r'){
                    shell.setVisible (false);
                }
            }
        });

    }
    private static int checkStyle (int style) {
        int mask = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
        return style & mask;
    }
    /**
     * Gets the widget font.
     * <p>
     * @return the widget font
     *
     * @exception SWTError <ul>
     *       <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *       <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     *   </ul>
     */
    public Font getFont () {
        return list.getFont();
    }
    /**
     * Gets the items.
     * <p>
     * This operation will fail if the items cannot
     * be queried from the OS.
     *
     * @return the items in the widget
     *
     * @exception SWTError <ul>
     *       <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *       <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     *       <li>ERROR_CANNOT_GET_ITEM when the operation fails</li>
     *   </ul>
     */
    public String[] getItems () {
        return list.getItems();
    }
    /**
     * Gets the minimum width of the list.
     *
     * @return the minimum width of the list
     */
    public int getMinimumWidth () {
        return minimumWidth;
    }
    /**
     * Launches the Popup List, waits for an item to be selected and then closes PopupList.
     *
     * @param rect the initial size and location of the PopupList; the dialog will be
     *        positioned so that it does not run off the screen and the largest number of items are visible
     *
     * @return the text of the selected item or null if no item is selected
     */
    public String open (Rectangle rect) {

        Point listSize = list.computeSize (rect.width, SWT.DEFAULT);
        Rectangle screenSize = shell.getDisplay().getBounds();

        // Position the dialog so that it does not run off the screen and the largest number of items are visible
        int spaceBelow = screenSize.height - (rect.y + rect.height) - 30;
        int spaceAbove = rect.y - 30;

        int y = 0;
        if (spaceAbove > spaceBelow && listSize.y > spaceBelow) {
            // place popup list above table cell
            if (listSize.y > spaceAbove){
                listSize.y = spaceAbove;
            } else {
                listSize.y += 2;
            }
            y = rect.y - listSize.y;

        } else {
            // place popup list below table cell
            if (listSize.y > spaceBelow){
                listSize.y = spaceBelow;
            } else {
                listSize.y += 2;
            }
            y = rect.y + rect.height + 40;
        }

        // Make dialog as wide as the cell
        listSize.x = rect.width;
        // dialog width should not be les than minimumwidth
        if (listSize.x < minimumWidth)
            listSize.x = minimumWidth;

        // Align right side of dialog with right side of cell
        int x = rect.x + rect.width - listSize.x;

        shell.setBounds(x, y, listSize.x, listSize.y);

        shell.open();
        list.setFocus();

        Display display = shell.getDisplay();
        while (!shell.isDisposed () && shell.isVisible ()) {
            if (!display.readAndDispatch()) display.sleep();
        }

        String result = null;
        if (!shell.isDisposed ()) {
            String [] strings = list.getSelection ();
            shell.dispose();
            if (strings.length != 0) result = strings [0];
        }
        return result;
    }
    /**
     * Selects an item with text that starts with specified String.
     * <p>
     * If the item is not currently selected, it is selected.
     * If the item at an index is selected, it remains selected.
     * If the string is not matched, it is ignored.
     *
     * @param string the text of the item
     *
     * @exception SWTError <ul>
     *       <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *       <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     *   </ul>
     */
    public void select(String string) {
        String[] items = list.getItems();

        // find the first entry in the list that starts with the
        // specified string
        if (string != null){
            for (int i = 0; i < items.length; i++) {
                if (items[i].startsWith(string)){
                    int index = list.indexOf(items[i]);
                    list.select(index);
                    break;
                }
            }
        }
    }
    /**
     * Sets the widget font.
     * <p>
     * When new font is null, the font reverts
     * to the default system font for the widget.
     *
     * @param font the new font (or null)
     * 
     * @exception SWTError <ul>
     *       <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *       <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     *   </ul>
     */
    public void setFont (Font font) {
        list.setFont(font);
    }
    /**
     * Sets all items.
     * <p>
     * The previous selection is cleared.
     * The previous items are deleted.
     * The new items are added.
     * The top index is set to 0.
     *
     * @param strings the array of items
     *
     * This operation will fail when an item is null
     * or could not be added in the OS.
     *
     * @exception SWTError <ul>
     *       <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *       <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     *       <li>ERROR_NULL_ARGUMENT when items is null</li>
     *       <li>ERROR_ITEM_NOT_ADDED when the operation fails</li>
     *   </ul>
     */
    public void setItems (String[] strings) {
        list.setItems(strings);
    }
    /**
     * Sets the minimum width of the list.
     *
     * @param width the minimum width of the list
     */
    public void setMinimumWidth (int width) {
        if (width < 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);

        minimumWidth = width;
    }
}
