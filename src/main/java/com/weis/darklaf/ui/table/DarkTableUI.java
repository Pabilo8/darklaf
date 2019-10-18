package com.weis.darklaf.ui.table;

import com.weis.darklaf.util.DarkUIUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;

/**
 * @author Jannis Weis
 */
public class DarkTableUI extends DarkTableUIBridge {

    private static final int ROW_HEIGHT = 22;
    protected Color selectionBackground;
    protected Color selectionFocusBackground;
    private final FocusListener focusListener = new FocusListener() {
        @Override
        public void focusGained(final FocusEvent e) {
            var bg = table.getSelectionBackground();
            if (bg instanceof UIResource) {
                table.setSelectionBackground(selectionFocusBackground);
            }
            table.repaint();
        }

        @Override
        public void focusLost(final FocusEvent e) {
            var bg = table.getSelectionBackground();
            if (bg instanceof UIResource) {
                if (table.isEditing()) {
                    table.setSelectionBackground(table.getBackground());
                } else {
                    table.setSelectionBackground(selectionBackground);
                }
            }
            table.repaint();
        }
    };
    private final PropertyChangeListener propertyChangeListener = e -> {
        var key = e.getPropertyName();
        if ("showHorizontalLines".equals(key)) {
            var b = (boolean) e.getNewValue();
            table.setRowMargin(b ? 1 : 0);
        } else if ("showVerticalLines".equals(key)) {
            var b = (boolean) e.getNewValue();
            table.getColumnModel().setColumnMargin(b ? 1 : 0);
        } else if ("ancestor".equals(key)) {
            var oldVal = e.getOldValue();
            var newVal = e.getNewValue();
            if (oldVal instanceof Component) {
                var oldUnwrapped = SwingUtilities.getUnwrappedParent((Component) oldVal);
                if ((oldUnwrapped instanceof JScrollPane)
                        && ((JComponent) oldUnwrapped).getBorder() instanceof UIResource) {
                    LookAndFeel.uninstallBorder((JComponent) oldUnwrapped);
                }
            }
            if (newVal instanceof Component) {
                var newUnwrapped = SwingUtilities.getUnwrappedParent((Component) newVal);
                if ((newUnwrapped instanceof JScrollPane)) {
                    LookAndFeel.installBorder((JComponent) newUnwrapped, "Table.scrollPaneBorder");
                }
            }
        }
    };
    protected Color borderColor;

    @NotNull
    @Contract("_ -> new")
    public static ComponentUI createUI(final JComponent c) {
        return new DarkTableUI();
    }


    @Override
    protected void paintGrid(@NotNull final Graphics g,
                             final int rMin, final int rMax, final int cMin, final int cMax) {
        g.setColor(table.getGridColor());

        Rectangle minCell = table.getCellRect(rMin, cMin, true);
        Rectangle maxCell = table.getCellRect(rMax, cMax, true);
        Rectangle damagedArea = minCell.union(maxCell);

        int draggedIndex = viewIndexForColumn(table.getTableHeader().getDraggedColumn());
        if (table.getShowHorizontalLines()) {
            int tableWidth = damagedArea.x + damagedArea.width;
            if (table.getComponentOrientation().isLeftToRight()) {
                if (draggedIndex == cMax) {
                    tableWidth -= 1;
                }
            } else {
                if (draggedIndex == cMin) {
                    tableWidth -= 1;
                }
            }
            int y = damagedArea.y;
            for (int row = rMin; row <= rMax; row++) {
                y += table.getRowHeight(row);
                SwingUtilities2.drawHLine(g, damagedArea.x, tableWidth - 1, y - 1);
            }
        }

        boolean scrollPaneRtl = isScrollPaneRtl();
        boolean scrollVisible = scrollBarVisible();
        if (table.getShowVerticalLines()) {
            TableColumnModel cm = table.getColumnModel();
            int tableHeight = getPreferredSize(table).height;
            int x;
            boolean ltr = table.getComponentOrientation().isLeftToRight();
            if (ltr) {
                x = damagedArea.x;
                if (scrollPaneRtl && scrollBarVisible()) {
                    SwingUtilities2.drawVLine(g, x, 0, tableHeight - 1);
                }
                for (int column = cMin; column <= cMax; column++) {
                    int w = cm.getColumn(column).getWidth();
                    x += w;
                    if (showVerticalLine(true, scrollVisible, !scrollPaneRtl, column, draggedIndex, cMin, cMax)) {
                        SwingUtilities2.drawVLine(g, x - 1, 0, tableHeight - 1);
                    }
                }
            } else {
                x = damagedArea.x;
                if (scrollPaneRtl && scrollBarVisible()) {
                    SwingUtilities2.drawVLine(g, x, 0, tableHeight - 1);
                }
                for (int column = cMax; column >= cMin; column--) {
                    int w = cm.getColumn(column).getWidth();
                    x += w;
                    if (showVerticalLine(false, scrollVisible, !scrollPaneRtl, column, draggedIndex, cMin, cMax)) {
                        SwingUtilities2.drawVLine(g, x - 1, 0, tableHeight - 1);
                    }
                }
            }
        }
    }


    protected static void setupRendererComponents(@NotNull final JTable table) {
        var cellRenderer = new DarkTableCellRenderer();
        var cellEditor = new DarkTableCellEditor();
        var colorRendererEditor = new DarkColorTableCellRendererEditor();

        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setDefaultRenderer(String.class, cellRenderer);
        table.setDefaultRenderer(Integer.class, cellRenderer);
        table.setDefaultRenderer(Double.class, cellRenderer);
        table.setDefaultRenderer(Float.class, cellRenderer);
        table.setDefaultRenderer(Boolean.class, cellRenderer);
        table.setDefaultRenderer(Color.class, colorRendererEditor);

        table.setDefaultEditor(Object.class, cellEditor);
        table.setDefaultEditor(String.class, cellEditor);
        table.setDefaultEditor(Integer.class, cellEditor);
        table.setDefaultEditor(Double.class, cellEditor);
        table.setDefaultEditor(Float.class, cellEditor);
        table.setDefaultEditor(Boolean.class, cellEditor);
        table.setDefaultEditor(Color.class, colorRendererEditor);
    }

    @Override
    protected void paintDraggedArea(@NotNull final Graphics g, final int rMin, final int rMax,
                                    final int cMin, final int cMax,
                                    final TableColumn draggedColumn, final int distance) {
        int draggedColumnIndex = viewIndexForColumn(draggedColumn);

        Rectangle minCell = table.getCellRect(rMin, draggedColumnIndex, true);
        Rectangle maxCell = table.getCellRect(rMax, draggedColumnIndex, true);

        Rectangle vacatedColumnRect = minCell.union(maxCell);

        int dist = adjustDistance(distance, vacatedColumnRect, table);

        // Paint a gray well in place of the moving column.
        g.setColor(table.getParent().getBackground());
        g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
                   vacatedColumnRect.width - 1, vacatedColumnRect.height);


        // Move to the where the cell has been dragged.
        vacatedColumnRect.x += dist;

        boolean scrollVisible = scrollBarVisible();
        boolean drawBottomBorder = !table.getShowHorizontalLines() && !scrollVisible && table.getShowVerticalLines();
        boolean ltr = table.getComponentOrientation().isLeftToRight();

        // Fill the background.
        g.setColor(table.getBackground());
        g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
                   vacatedColumnRect.width, vacatedColumnRect.height);


        // Paint the vertical grid lines if necessary.
        if (table.getShowVerticalLines()) {
            g.setColor(table.getGridColor());
            int x1 = vacatedColumnRect.x;
            int y1 = vacatedColumnRect.y;
            int x2 = x1 + vacatedColumnRect.width - 1;
            int y2 = y1 + vacatedColumnRect.height - 1;

            boolean onLeftEdge = ltr ? draggedColumnIndex == cMin : draggedColumnIndex == cMax;
            boolean onRightEdge = ltr ? draggedColumnIndex == cMax : draggedColumnIndex == cMin;
            if (scrollBarVisible()) {
                if (isScrollPaneRtl()) {
                    onLeftEdge = false;
                } else {
                    onRightEdge = false;
                }
            }
            // Left
            if (dist != 0 || !onLeftEdge) {
                if (draggedColumnIndex == cMin && scrollBarVisible() && isScrollPaneRtl()) x1++;
                g.fillRect(x1 - 1, y1, 1, y2 - y1);
            }
            // Right
            if (dist != 0 || !onRightEdge) {
                g.fillRect(x2, y1, 1, y2 - y1);
            }
        }

        for (int row = rMin; row <= rMax; row++) {
            // Render the cell value
            Rectangle r = table.getCellRect(row, draggedColumnIndex, false);
            r.x += dist;
            paintCell(g, r, row, draggedColumnIndex);

            // Paint the (lower) horizontal grid line if necessary.
            if (table.getShowHorizontalLines() || (!scrollVisible && row == rMax)) {
                g.setColor(table.getGridColor());
                Rectangle rcr = table.getCellRect(row, draggedColumnIndex, true);
                rcr.x += dist;
                int x1 = rcr.x - 1;
                int y1 = rcr.y;
                int x2 = x1 + rcr.width + 1;
                int y2 = y1 + rcr.height - 1;
                g.fillRect(x1, y2, x2 - x1, 1);
            }
        }
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        table.addFocusListener(focusListener);
        table.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        table.removeFocusListener(focusListener);
        table.removePropertyChangeListener(propertyChangeListener);
    }

    protected boolean pointOutsidePrefSize(final int row, final int column, final Point p) {
        return false;
    }

    protected boolean isScrollPaneRtl() {
        if (!isInScrollPane()) return false;
        Container comp = SwingUtilities.getUnwrappedParent(table).getParent();
        return !comp.getComponentOrientation().isLeftToRight();
    }

    protected boolean scrollBarVisible() {
        Container comp = SwingUtilities.getUnwrappedParent(table);
        if (comp != null) {
            comp = comp.getParent();
        }
        return comp instanceof JScrollPane && ((JScrollPane) comp).getVerticalScrollBar().isVisible();
    }

    protected boolean showVerticalLine(final boolean ltr, final boolean scrollVisible,
                                       final boolean scrollLtR, final int column, final int draggedIndex,
                                       final int cMin, final int cMax) {
        int dist = adjustDistance(table.getTableHeader().getDraggedDistance(),
                                  table.getCellRect(0, draggedIndex, true),
                                  table);
        boolean isDragged = column == draggedIndex && dist != 0;
        if (!scrollVisible) {
            if (ltr) {
                return column != cMax;
            } else {
                return column != cMin;
            }
        } else {
            if (ltr) {
                if (scrollLtR) {
                    return column != cMax || !isDragged;
                } else {
                    return column != cMax;
                }
            } else {
                if (scrollLtR) {
                    return column != cMin || !isDragged;
                } else {
                    return column != cMin;
                }
            }
        }
    }

    protected Color getBorderColor() {
        return borderColor;
    }

    protected boolean isInScrollPane() {
        Container comp = SwingUtilities.getUnwrappedParent(table);
        if (comp != null) {
            comp = comp.getParent();
        }
        return comp instanceof JScrollPane;
    }

    protected static int adjustDistance(final int distance, @NotNull final Rectangle rect,
                                        @NotNull final JTable comp) {
        int dist = distance;
        int min = 0;
        int max = comp.getX() + comp.getWidth();
        if (rect.x + dist <= min) {
            dist = min - rect.x;
        }
        if (rect.x + rect.width + dist >= max) {
            dist = max - rect.x - rect.width;
        }
        return dist;
    }

    @Override
    protected void paintCell(final Graphics g, final Rectangle cellRect, final int row, final int column) {
        var bounds = table.getVisibleRect();
        Point upperLeft = bounds.getLocation();
        Point lowerRight = new Point(upperLeft.x + bounds.width - 1, upperLeft.y + bounds.height - 1);
        int cMin = table.columnAtPoint(upperLeft);
        int cMax = table.columnAtPoint(lowerRight);

        boolean scrollLtR = !isScrollPaneRtl();
        boolean ltr = table.getComponentOrientation().isLeftToRight();
        int draggedIndex = viewIndexForColumn(table.getTableHeader().getDraggedColumn());
        int dist = adjustDistance(table.getTableHeader().getDraggedDistance(),
                                  table.getCellRect(row, draggedIndex, true),
                                  table);
        boolean isDragged = column == draggedIndex && dist != 0;
        var r = new Rectangle(cellRect);
        if (!scrollBarVisible()) {
            if (ltr) {
                if (column == cMax && !isDragged) r.width += 1;
            } else {
                if (column == cMin && !isDragged) r.width += 1;
            }
        } else if (!scrollLtR) {
            if (ltr) {
                if (column == cMax && !isDragged) r.width += 1;
                if (column == cMin && !isDragged) {
                    r.width -= 1;
                    r.x += 1;
                }
            } else {
                if (column == cMin && !isDragged) r.width += 1;
                if (column == cMax && !isDragged) {
                    r.width -= 1;
                    r.x += 1;
                }
            }
        }
        if (table.isEditing() && table.getEditingRow() == row &&
                table.getEditingColumn() == column) {
            Component component = table.getEditorComponent();
            component.setBounds(cellRect);
            component.validate();
        } else {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component component = table.prepareRenderer(renderer, row, column);
            rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y,
                                        cellRect.width, cellRect.height, true);
        }
    }

    @Override
    protected Handler getHandler() {
        if (handler == null) {
            handler = new DarkHandler();
        }
        return handler;
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        int rowHeight = UIManager.getInt("Table.rowHeight");
        if (rowHeight > 0) {
            table.setRowHeight(ROW_HEIGHT);
        }
        table.setDefaultEditor(Object.class, new DarkTableCellEditor());
        table.putClientProperty("JTable.renderBooleanAsCheckBox",
                                UIManager.getBoolean("Table.renderBooleanAsCheckBox"));
        table.putClientProperty("JTable.booleanRenderType", UIManager.getString("Table.booleanRenderType"));
        table.putClientProperty("JTable.alternateRowColor", UIManager.getBoolean("Table.alternateRowColor"));
        setupRendererComponents(table);
        borderColor = UIManager.getColor("TableHeader.borderColor");
        selectionFocusBackground = UIManager.getColor("Table.focusSelectionBackground");
        selectionBackground = UIManager.getColor("Table.selectionNoFocusBackground");
    }

    @Override
    public Dimension getPreferredSize(final JComponent c) {
        var prefSize = super.getPreferredSize(c);
        if (!isInScrollPane()) {
            return prefSize;
        } else {
            var dim = SwingUtilities.getUnwrappedParent(table).getSize();
            if (dim.width < prefSize.width || dim.height < prefSize.height) {
                return prefSize;
            } else {
                return dim;
            }
        }
    }

    protected class DarkHandler extends Handler {

        protected int lastIndex = -1;

        @Override
        public void mouseClicked(final MouseEvent e) {
            super.mouseClicked(e);
            if (isFileList) {
                int row = table.rowAtPoint(e.getPoint());
                JFileChooser fc = getFileChooser();
                if (row < 0 || fc == null) return;
                int column = getFileNameColumnIndex();
                boolean isSelected = table.getSelectionModel().getLeadSelectionIndex() == row
                        && table.getColumnModel().getSelectionModel().getLeadSelectionIndex() == column;
                if ((!fc.isMultiSelectionEnabled() || fc.getSelectedFiles().length <= 1)
                        && isSelected && lastIndex == row
                        && DarkUIUtil.isOverText(e, row, column, table)) {
                    startEditing(row, column);
                } else {
                    lastIndex = row;
                }
            }
        }

        protected JFileChooser getFileChooser() {
            var obj = table.getClientProperty("JTable.fileChooserParent");
            if (obj instanceof Supplier) {
                var supplied = ((Supplier) obj).get();
                return supplied instanceof JFileChooser ? (JFileChooser) supplied : null;
            }
            return null;
        }

        protected Integer getFileNameColumnIndex() {
            var obj = table.getClientProperty("JTable.fileNameColumnIndex");
            return obj instanceof Integer ? (Integer) obj : 0;
        }

        protected void startEditing(final int row, final int column) {
            table.editCellAt(row, column, null);
            Component editorComponent = table.getEditorComponent();
            if (editorComponent != null && !editorComponent.hasFocus()) {
                SwingUtilities2.compositeRequestFocus(editorComponent);
            }
        }

        @Override
        protected void maybeStartTimer() {
        }

        @Override
        public void actionPerformed(final ActionEvent ae) {
        }
    }
}
