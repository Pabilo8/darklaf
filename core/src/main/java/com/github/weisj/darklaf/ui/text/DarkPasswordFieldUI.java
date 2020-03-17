/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.weisj.darklaf.ui.text;

import com.github.weisj.darklaf.decorators.MouseMovementListener;
import com.github.weisj.darklaf.util.DarkUIUtil;
import com.github.weisj.darklaf.util.GraphicsContext;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;

/**
 * @author Konstantin Bulenkov
 * @author Jannis Weis
 */
public class DarkPasswordFieldUI extends DarkPasswordFieldUIBridge {

    protected static final String KEY_PREFIX = "JPasswordField.";
    public static final String KEY_SHOW_VIEW_BUTTON = "JPasswordField.showViewIcon";
    protected Icon show;
    protected Icon showPressed;
    private final FocusListener focusListener = new FocusAdapter() {
        public void focusGained(final FocusEvent e) {
            getComponent().repaint();
        }

        public void focusLost(final FocusEvent e) {
            getComponent().repaint();
        }
    };
    private final MouseMotionListener mouseMotionListener = (MouseMovementListener) e -> updateCursor(e.getPoint());
    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyTyped(final KeyEvent e) {
            SwingUtilities.invokeLater(() -> {
                Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, getComponent());
                updateCursor(p);
            });
        }
    };
    protected int borderSize;
    protected int arc;
    private char echo_dot = '*';
    private boolean showTriggered = false;
    private final MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mousePressed(final MouseEvent e) {
            if (hasShowIcon(editor) && isOverEye(e.getPoint())) {
                ((JPasswordField) getComponent()).setEchoChar((char) 0);
                showTriggered = true;
                getComponent().repaint();
            }
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            if (showTriggered) {
                ((JPasswordField) getComponent()).setEchoChar(echo_dot);
                showTriggered = false;
                getComponent().repaint();
            }
        }
    };


    public static ComponentUI createUI(final JComponent c) {
        return new DarkPasswordFieldUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        borderSize = UIManager.getInt("PasswordField.borderThickness");
        arc = UIManager.getInt("PasswordField.arc");
        show = UIManager.getIcon("PasswordField.show.icon");
        showPressed = UIManager.getIcon("PasswordField.showPressed.icon");
    }

    protected void updateCursor(final Point p) {
        boolean useShow = hasShowIcon(editor);
        Rectangle textRect = DarkTextFieldUI.getTextRect(getComponent());
        int rightMargin = useShow ? getShowIconCoord().x : textRect.x + textRect.width + 1;
        boolean insideTextArea = getDrawingRect(getComponent()).contains(p)
                && p.x >= textRect.x && p.x < rightMargin;
        if (insideTextArea) {
            getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        } else if (useShow && isOverEye(p)) {
            getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            getComponent().setCursor(Cursor.getDefaultCursor());
        }
    }

    protected void installListeners() {
        super.installListeners();
        Component c = getComponent();
        c.addFocusListener(focusListener);
        c.addMouseListener(mouseListener);
        c.addMouseMotionListener(mouseMotionListener);
        c.addKeyListener(keyListener);
    }

    protected void uninstallListeners() {
        super.uninstallListeners();
        Component c = getComponent();
        c.removeFocusListener(focusListener);
        c.removeMouseListener(mouseListener);
        c.removeMouseMotionListener(mouseMotionListener);
        c.removeKeyListener(keyListener);
    }

    protected void paintBackground(final Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        JTextComponent c = getComponent();

        Container parent = c.getParent();
        if (parent != null) {
            g.setColor(parent.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }

        Border border = c.getBorder();
        GraphicsContext config = new GraphicsContext(g);
        if (border instanceof DarkTextBorder) {
            if (c.isEnabled() && c.isEditable()) {
                g.setColor(c.getBackground());
            }
            int width = c.getWidth();
            int height = c.getHeight();
            int w = borderSize;
            DarkUIUtil.fillRoundRect(g, w, w, width - 2 * w, height - 2 * w, arc);
            if (hasShowIcon(c) && showShowIcon()) {
                paintShowIcon(g);
            }
        } else {
            super.paintBackground(g);
        }
        config.restore();
    }

    private void paintShowIcon(final Graphics2D g) {
        Point p = getShowIconCoord();
        if (showTriggered) {
            getShowTriggeredIcon().paintIcon(getComponent(), g, p.x, p.y);
        } else {
            getShowIcon().paintIcon(getComponent(), g, p.x, p.y);
        }
    }

    protected Icon getShowTriggeredIcon() {
        return showPressed;
    }

    public static boolean hasShowIcon(final Component c) {
        return c instanceof JPasswordField
            && Boolean.TRUE.equals(((JComponent) c).getClientProperty(KEY_SHOW_VIEW_BUTTON));
    }


    private Point getShowIconCoord() {
        Rectangle r = getDrawingRect(getComponent());
        int w = getShowIcon().getIconWidth();
        return DarkUIUtil.adjustForOrientation(new Point(r.x + r.width - w - borderSize, r.y + (r.height - w) / 2),
                                               w, editor);
    }

    private boolean isOverEye(final Point p) {
        return showShowIcon() && DarkTextFieldUI.isOver(getShowIconCoord(), getShowIcon(), p);
    }

    protected Icon getShowIcon() {
        return show;
    }

    private boolean showShowIcon() {
        JPasswordField c = (JPasswordField) getComponent();
        char[] pw = c.getPassword();
        boolean show = pw.length > 0;
        Arrays.fill(pw, (char) 0);
        return show;
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        if (c instanceof JPasswordField) {
            echo_dot = ((JPasswordField) c).getEchoChar();
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String key = evt.getPropertyName();
        if (KEY_SHOW_VIEW_BUTTON.equals(key)) {
            editor.doLayout();
            Component parent = editor.getParent();
            if (parent instanceof JComponent) {
                parent.doLayout();
            }
            editor.repaint();
        }
    }
}
