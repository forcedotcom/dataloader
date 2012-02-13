/*
 * Copyright (c) 2012, salesforce.com, inc.
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

package com.salesforce.dataloader.ui;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;


/**
 * A hyperlink text label.
 * <p>
 * This control displays a line of text (with an optional underline) which can
 * be clicked to send a Selection event. Colors for the text and underline in
 * their normal, mouse hover and active state can be set independently. The text
 * can contain a mnemonic character for triggering the link via keyboard. Unless
 * the control is created with the NO_FOCUS style, it accepts keyboard focus and
 * can be triggered with RETURN and SPACE while focused.
 * </p><p>
 * Note: This control should not be resized beyond its minimum / preferred size.
 * </p><p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>NO_FOCUS</dd>
 * <dt><b>Events:</b>
 * <dd>Selection</dd>
 * </dl>
 * </p>
 *
 * @author Stefan Zeiger (szeiger@novocode.com)
 * @since Mar 2, 2004
 * @version $Id: Hyperlink.java,v 1.5 2004/03/10 20:39:19 szeiger Exp $
 */

public final class Hyperlink extends Canvas
{
    private String text = "";
    private Cursor handCursor, arrowCursor;
    private Color normalForeground, activeForeground, hoverForeground;
    private Color normalUnderline, activeUndeline, hoverUnderline;
    private boolean isActive;
    private boolean cursorInControl;
    private Rectangle cachedClientArea;
    private Listener shellListener;
    private Shell shell;
    private int mnemonic = -1;


    /**
     * Constructs a new instance of this class given its parent
     * and a style value describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in
     * class <code>SWT</code> which is applicable to instances of this
     * class, or must be built by <em>bitwise OR</em>'ing together
     * (that is, using the <code>int</code> "|" operator) two or more
     * of those <code>SWT</code> style constants. The class description
     * lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     */

    public Hyperlink(Composite parent, int style)
    {
        super(parent, checkStyle(style));

        handCursor = new Cursor(getDisplay(), SWT.CURSOR_HAND);
        arrowCursor = new Cursor(getDisplay(), SWT.CURSOR_ARROW);
        setCursor(handCursor);

        normalForeground = getDisplay().getSystemColor(SWT.COLOR_BLUE);
        hoverForeground  = normalForeground;
        activeForeground = getDisplay().getSystemColor(SWT.COLOR_RED);

        normalUnderline  = null;
        hoverUnderline   = normalForeground;
        activeUndeline   = activeForeground;

        super.setForeground(normalForeground);

        addPaintListener(new PaintListener()
        {
            @Override
            public void paintControl(PaintEvent event)
            {
                onPaint(event);
            }
        });

        addDisposeListener(new DisposeListener()
        {
            @Override
            public void widgetDisposed(DisposeEvent e)
            {
                if(handCursor != null)
                {
                    handCursor.dispose();
                    handCursor = null;
                }
                if(arrowCursor != null)
                {
                    arrowCursor.dispose();
                    arrowCursor = null;
                }
                if(shellListener != null)
                {
                    shell.removeListener(SWT.Activate, shellListener);
                    shell.removeListener(SWT.Deactivate, shellListener);
                    shellListener = null;
                }
                text = null;
            }
        });

        addListener(SWT.MouseDown, new Listener()
        {
            @Override
            public void handleEvent(Event event)
            {
                isActive = true;
                cursorInControl = true;
                redraw();
            }
        });

        addListener(SWT.MouseUp, new Listener()
        {
            @Override
            public void handleEvent(Event event)
            {
                isActive = false;
                redraw();
                if(cursorInControl) linkActivated();
            }
        });

        addListener(SWT.Resize, new Listener()
        {
            @Override
            public void handleEvent(Event event)
            {
                cachedClientArea = getClientArea();
            }
        });

        Listener mouseListener = new Listener()
        {
            @Override
            public void handleEvent(Event event)
            {
                boolean newCursorInControl = isInClientArea(event);
                if(cursorInControl != newCursorInControl)
                {
                    cursorInControl = newCursorInControl;
                    if(cursorInControl) setCursor(handCursor);
                    else if(isActive) setCursor(arrowCursor);
                    if(isActive || (normalForeground != hoverForeground) || (normalUnderline != hoverUnderline)) redraw();
                }
            }
        };
        addListener(SWT.MouseMove, mouseListener);
        addListener(SWT.MouseEnter, mouseListener);
        addListener(SWT.MouseExit, mouseListener);

        cachedClientArea = getClientArea();

        if((style & SWT.NO_FOCUS) == 0) // Take focus
        {
            addListener(SWT.KeyDown, new Listener()
            {
                @Override
                public void handleEvent(Event event)
                {
                    if(event.character == ' ') linkActivated();
                }
            });

            addListener(SWT.Traverse, new Listener()
            {
                @Override
                public void handleEvent(Event event)
                {
                    if(event.detail == SWT.TRAVERSE_RETURN)
                    {
                        linkActivated();
                        event.doit = false;
                    }
                    else if(event.detail == SWT.TRAVERSE_MNEMONIC)
                    {
                        if(mnemonic != -1 && Character.toLowerCase(event.character) == mnemonic)
                        {
                            setFocus();
                            linkActivated();
                            event.doit = false;
                        }
                        else event.doit = true;
                    }
                    else event.doit = true; // Accept all other traversal keys
                }
            });

            addListener(SWT.FocusIn, new Listener()
            {
                @Override
                public void handleEvent(Event event)
                {
                    //System.out.println("FocusIn");
                    redraw();
                }
            });

            addListener(SWT.FocusOut, new Listener()
            {
                @Override
                public void handleEvent(Event event)
                {
                    //System.out.println("FocusOut");
                    redraw();
                }
            });
        }
        else // Don't take focus but still support mnemonics
        {
            addListener(SWT.Traverse, new Listener()
            {
                @Override
                public void handleEvent(Event event)
                {
                    if(event.detail == SWT.TRAVERSE_MNEMONIC && mnemonic != -1 && Character.toLowerCase(event.character) == mnemonic)
                    {
                        linkActivated();
                        event.doit = false;
                    }
                }
            });
        }

        Composite shellComp = getParent();
        while(shellComp != null && (!(shellComp instanceof Shell))) shellComp = shellComp.getParent();
        shell = (Shell)shellComp;

        if(shell != null)
        {
            shellListener = new Listener() // Remove stale mouse hover on shell activation / deactivation
            {
                @Override
                public void handleEvent(Event event)
                {
                    boolean newCursorInControl = getDisplay().getCursorControl() == Hyperlink.this;
                    //System.out.println("Shell (de)activated. Cursor over control: "+newCursorInControl);
                    if(cursorInControl != newCursorInControl)
                    {
                        cursorInControl = newCursorInControl;
                        if(cursorInControl) setCursor(handCursor);
                        else if(isActive) setCursor(arrowCursor);
                        if(isActive || (normalForeground != hoverForeground) || (normalUnderline != hoverUnderline)) redraw();
                    }
                }
            };

            shell.addListener(SWT.Activate, shellListener);
            shell.addListener(SWT.Deactivate, shellListener);
        }
    }


    private void linkActivated()
    {
        //System.out.println("Link clicked!");
        Event e = new Event();
        e.widget = this;
        e.type = SWT.Selection;
        notifyListeners(SWT.Selection, e);
    }


    private boolean isInClientArea(Event event)
    {
        return event.x >= cachedClientArea.x && event.x < cachedClientArea.x+cachedClientArea.width &&
                event.y >= cachedClientArea.y && event.y < cachedClientArea.y+cachedClientArea.height;
    }


    @Override
    public boolean isReparentable ()
    {
        checkWidget ();
        return false;
    }


    /**
     * Check the style bits to ensure that no invalid styles are applied.
     */

    private static int checkStyle(int style)
    {
        style = style & SWT.NO_FOCUS;

        // [NOTE] The following transparency workaround was taken from CLabel
        //TEMPORARY CODE
        /*
         * The default background on carbon and some GTK themes is not a solid color
         * but a texture.  To show the correct default background, we must allow
         * the operating system to draw it and therefore, we can not use the
         * NO_BACKGROUND style.  The NO_BACKGROUND style is not required on platforms
         * that use double buffering which is true in both of these cases.
         */
        String platform = SWT.getPlatform();
        if ("carbon".equals(platform) || "gtk".equals(platform)) return style;
        return style | SWT.NO_BACKGROUND;
    }


    @Override
    public Point computeSize(int wHint, int hHint, boolean changed)
    {
        checkWidget();
        Point e = getTotalSize(text);
        if (wHint != SWT.DEFAULT) e.x = wHint;
        if (hHint != SWT.DEFAULT) e.y = hHint;
        return e;
    }


    /**
     * Compute the minimum size.
     */

    private Point getTotalSize(String text)
    {
        Point size = new Point(0, 0);
        GC gc = new GC(this);

        if (text != null && text.length() > 0)
        {
            Point e = gc.textExtent(text, SWT.DRAW_MNEMONIC);
            size.x += e.x;
            size.y = Math.max(size.y, e.y);
        }
        else size.y = Math.max(size.y, gc.getFontMetrics().getHeight());

        gc.dispose();
        return size;
    }


    /**
     * Return the Hyperlink's displayed text.
     *
     * @return the text of the hyperlink or null
     */

    public String getText()
    {
        return text;
    }


    private void onPaint(PaintEvent event)
    {
        Rectangle rect = cachedClientArea; // getClientArea();
        if (rect.width == 0 || rect.height == 0) return;

        Point extent = getTotalSize(text);

        GC gc = event.gc;

        if ((getStyle() & SWT.NO_BACKGROUND) != 0)
        {
            gc.setBackground(getBackground());
            gc.fillRectangle(rect);
        }

        if(isFocusControl()) gc.drawFocus(rect.x, rect.y, rect.width, rect.height);

        Color textFG, lineFG;

        if(cursorInControl)
        {
            textFG = isActive ? activeForeground : hoverForeground;
            lineFG = isActive ? activeUndeline : hoverUnderline;
        }
        else
        {
            textFG = /* isActive ? mouseOverForeground : */ normalForeground;
            lineFG = /* isActive ? mouseOverUnderline : */ normalUnderline;
        }

        if(textFG == null) textFG = normalForeground;
        if(textFG == null) textFG = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

        int textHeight = gc.getFontMetrics().getHeight();

        gc.setForeground(textFG);
        gc.drawText(text, rect.x, rect.y + (rect.height - textHeight) / 2, SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC);

        int uy = (rect.y + (rect.height - textHeight) / 2) + gc.getFontMetrics().getAscent() + gc.getFontMetrics().getLeading() + 1;
        int lineWidth = extent.x > rect.width ? rect.width : extent.x;

        if(lineFG != null)
        {
            if(lineFG != textFG) gc.setForeground(lineFG);
            gc.drawLine(rect.x, uy, rect.x + lineWidth, uy);
        }
    }


    @Override
    public void setForeground(Color color)
    {
        super.setForeground(color);
        this.normalForeground = color;
        redraw();
    }


    public void setHoverForeground(Color color)
    {
        this.hoverForeground = color;
        redraw();
    }


    public void setActiveForeground(Color color)
    {
        this.activeForeground = color;
        redraw();
    }


    public void setUnderline(Color color)
    {
        this.normalUnderline = color;
        redraw();
    }


    public void setHoverUnderline(Color color)
    {
        this.hoverUnderline = color;
        redraw();
    }


    public void setActiveUnderline(Color color)
    {
        this.activeUndeline = color;
        redraw();
    }


    public Color getHoverForeground()
    {
        return this.hoverForeground;
    }


    public Color getActiveForeground()
    {
        return this.activeForeground;
    }


    public Color getUnderline()
    {
        return this.normalUnderline;
    }


    public Color getHoverUnderline()
    {
        return this.hoverUnderline;
    }


    public Color getActiveUnderline()
    {
        return this.activeUndeline;
    }


    @Override
    public void setBackground(Color color)
    {
        super.setBackground(color);
        redraw();
    }


    @Override
    public void setFont(Font font)
    {
        super.setFont(font);
        redraw();
    }


    /**
     * Set the Hyperlink's displayed text.
     * The value <code>null</code> clears it.
     * <p>
     * Mnemonics are indicated by an '&amp' that causes the next
     * character to be the mnemonic.  When the user presses a
     * key sequence that matches the mnemonic, a selection
     * event occurs. On most platforms, the mnemonic appears
     * underlined but may be emphasised in a platform specific
     * manner.  The mnemonic indicator character '&amp' can be
     * escaped by doubling it in the string, causing a single
     * '&amp' to be displayed.
     * </p>
     *
     * @param text the text to be displayed in the hyperlink or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */

    public void setText(String text)
    {
        checkWidget();
        if (text == null) text = "";
        if (!text.equals(this.text))
        {
            this.text = text;
            int i = text.indexOf('&');
            if(i == -1 || i == text.length()-1) mnemonic = -1;
            else mnemonic = Character.toLowerCase(text.charAt(i+1));
            redraw();
        }
    }


    /**
     * Adds the listener to receive events.
     *
     * @param listener the listener
     *
     * @exception SWTError(ERROR_THREAD_INVALID_ACCESS)
     * when called from the wrong thread
     * @exception SWTError(ERROR_WIDGET_DISPOSED)
     * when the widget has been disposed
     * @exception SWTError(ERROR_NULL_ARGUMENT)
     * when listener is null
     */

    public void addSelectionListener(SelectionListener listener)
    {
        checkWidget();
        if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        TypedListener typedListener = new TypedListener(listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection, typedListener);
    }


    /**
     * Removes the listener.
     *
     * @param listener the listener
     *
     * @exception SWTError(ERROR_THREAD_INVALID_ACCESS)
     * when called from the wrong thread
     * @exception SWTError(ERROR_WIDGET_DISPOSED)
     * when the widget has been disposed
     * @exception SWTError(ERROR_NULL_ARGUMENT)
     * when listener is null
     */

    public void removeSelectionListener(SelectionListener listener)
    {
        checkWidget();
        if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        removeListener(SWT.Selection, listener);
        removeListener(SWT.DefaultSelection, listener);
    }
}
