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
package com.github.weisj.darklaf.ui.slider;

import com.github.weisj.darklaf.decorators.MouseClickListener;
import com.github.weisj.darklaf.util.DarkUIUtil;
import com.github.weisj.darklaf.util.GraphicsContext;
import com.github.weisj.darklaf.util.GraphicsUtil;
import com.github.weisj.darklaf.util.PropertyKey;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Jannis Weis
 */
public class DarkSliderUI extends BasicSliderUI implements PropertyChangeListener {

    protected static final String KEY_PREFIX = "JSlider.";
    public static final String KEY_THUMB_ARROW_SHAPE = KEY_PREFIX + "paintThumbArrowShape";
    public static final String KEY_SHOW_VOLUME_ICON = KEY_PREFIX + "volume.showIcon";
    public static final String KEY_VARIANT = KEY_PREFIX + "variant";
    public static final String KEY_INSTANT_SCROLL = KEY_PREFIX + "instantScrollEnabled";
    public static final String KEY_SHOW_FOCUS_GLOW = KEY_PREFIX + "paintFocusGlow";
    public static final String VARIANT_VOLUME = "volume";

    private static final int ICON_BAR_EXT = 5;
    private static final int ICON_PAD = 10;
    private final Rectangle iconRect = new Rectangle(0, 0, 0, 0);
    private final MouseListener mouseListener = new MouseClickListener() {
        private boolean muted = false;
        private int oldValue;

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (slider.isEnabled() && showVolumeIcon(slider) && iconRect.contains(e.getPoint())) {
                if (muted && slider.getValue() == slider.getMinimum()) {
                    slider.setValue(oldValue);
                    muted = false;
                } else {
                    oldValue = slider.getValue();
                    slider.setValue(slider.getMinimum());
                    muted = true;
                }
            }
        }
    };
    protected int plainThumbRadius;
    protected int arcSize;
    protected int trackSize;
    protected Dimension thumbSize;
    protected Color inactiveTickForeground;
    protected Color trackBackground;
    protected Color selectedTrackBackground;
    protected Color selectedTrackInactiveBackground;
    protected Color selectedVolumeTrackBackground;
    protected Color selectedVolumeTrackInactiveBackground;
    protected Color thumbBackground;
    protected Color thumbInactiveBackground;
    protected Color volumeThumbBackground;
    protected Color volumeThumbInactiveBackground;
    protected Color thumbBorderColor;
    protected Color thumbInactiveBorderColor;

    protected Icon volume0;
    protected Icon volume1;
    protected Icon volume2;
    protected Icon volume3;
    protected Icon volume4;
    protected Icon volume0Inactive;
    protected Icon volume1Inactive;
    protected Icon volume2Inactive;
    protected Icon volume3Inactive;
    protected Icon volume4Inactive;

    public DarkSliderUI(final JSlider b) {
        super(b);
    }


    public static ComponentUI createUI(final JComponent c) {
        return new DarkSliderUI((JSlider) c);
    }

    private static boolean showVolumeIcon(final JComponent c) {
        return isVolumeSlider(c)
               && Boolean.TRUE.equals(c.getClientProperty(KEY_SHOW_VOLUME_ICON));
    }

    private static boolean isVolumeSlider(final JComponent c) {
        return VARIANT_VOLUME.equals(c.getClientProperty(KEY_VARIANT));
    }

    @Override
    protected TrackListener createTrackListener(final JSlider slider) {
        return new SnapTrackListener();
    }

    @Override
    protected void installListeners(final JSlider slider) {
        super.installListeners(slider);
        slider.addMouseListener(mouseListener);
        slider.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallListeners(final JSlider slider) {
        super.uninstallListeners(slider);
        slider.removeMouseListener(mouseListener);
        slider.removePropertyChangeListener(this);
    }

    @Override
    protected void calculateGeometry() {
        super.calculateGeometry();
        if (showVolumeIcon(slider)) {
            calculateIconRect();
        } else {
            iconRect.setBounds(-1, -1, 0, 0);
        }
    }

    @Override
    protected void calculateContentRect() {
        super.calculateContentRect();
        if (showVolumeIcon(slider)) {
            if (isHorizontal()) {
                contentRect.width -= getVolumeIcon().getIconWidth() + ICON_PAD;
                if (!slider.getComponentOrientation().isLeftToRight()) {
                    contentRect.x += getVolumeIcon().getIconWidth() + ICON_PAD;
                }
            } else {
                contentRect.height -= getVolumeIcon().getIconHeight() + ICON_PAD;
                if (!slider.getComponentOrientation().isLeftToRight()) {
                    contentRect.y += getVolumeIcon().getIconHeight() + ICON_PAD;
                }
            }
        }
    }

    @Override
    protected Dimension getThumbSize() {
        if (isPlainThumb()) {
            return new Dimension(plainThumbRadius + 6, plainThumbRadius + 6);
        }
        return isHorizontal()
               ? new Dimension(thumbSize.width, thumbSize.height)
               : new Dimension(thumbSize.height, thumbSize.width);
    }

    @Override
    public void paint(final Graphics g2, final JComponent c) {
        super.paint(g2, c);
        if (showVolumeIcon(c)) {
            getVolumeIcon().paintIcon(c, g2, iconRect.x, iconRect.y);
        }
    }

    @Override
    public void paintFocus(final Graphics g2) {
        //Do nothing
    }

    @Override
    public void paintTrack(final Graphics g2d) {
        Graphics2D g = (Graphics2D) g2d;
        GraphicsContext config = GraphicsUtil.setupStrokePainting(g);

        Color bgColor = getTrackBackground();
        Color selectionColor = getSelectedTrackColor();

        if (isHorizontal()) {
            Area track = getHorizontalTrackShape();
            g.setColor(bgColor);
            g.fill(track);
            Shape selection = getHorizontalSliderShape(track);
            g.setColor(selectionColor);
            g.fill(selection);
        } else {
            Area track = getVerticalTrackShape();
            g.setColor(bgColor);
            g.fill(track);
            Shape selection = getVerticalSliderShape(track);
            g.setColor(selectionColor);
            g.fill(selection);
        }
        config.restore();
    }

    @Override
    protected void paintMinorTickForHorizSlider(final Graphics g, final Rectangle tickBounds, final int x) {
        checkDisabled(g);
        super.paintMinorTickForHorizSlider(g, tickBounds, x);
    }

    @Override
    protected void paintMajorTickForHorizSlider(final Graphics g, final Rectangle tickBounds, final int x) {
        checkDisabled(g);
        super.paintMajorTickForHorizSlider(g, tickBounds, x);
    }

    @Override
    protected void paintMinorTickForVertSlider(final Graphics g, final Rectangle tickBounds, final int y) {
        checkDisabled(g);
        super.paintMinorTickForVertSlider(g, tickBounds, y);
    }

    @Override
    protected void paintMajorTickForVertSlider(final Graphics g, final Rectangle tickBounds, final int y) {
        checkDisabled(g);
        super.paintMajorTickForVertSlider(g, tickBounds, y);
    }

    @Override
    public void paintLabels(final Graphics g) {
        checkDisabled(g);
        GraphicsContext config = GraphicsUtil.setupAntialiasing(g);
        super.paintLabels(g);
        config.restore();
    }

    @Override
    protected void paintHorizontalLabel(final Graphics g, final int value, final Component label) {
        checkDisabled(g);
        super.paintHorizontalLabel(g, value, label);
    }

    @Override
    protected void paintVerticalLabel(final Graphics g, final int value, final Component label) {
        checkDisabled(g);
        super.paintVerticalLabel(g, value, label);
    }


    @Override
    public void paintThumb(final Graphics g2) {
        Graphics2D g = (Graphics2D) g2;
        GraphicsContext context = GraphicsUtil.setupStrokePainting(g);
        g.translate(thumbRect.x, thumbRect.y);

        if (isPlainThumb()) {
            paintPlainSliderThumb(g);
        } else {
            paintSliderThumb(g);
        }

        g.translate(-thumbRect.x, -thumbRect.y);
        context.restore();
    }

    @Override
    protected void scrollDueToClickInTrack(final int dir) {
        Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, slider);
        Area area = isHorizontal() ? getHorizontalTrackShape() : getVerticalTrackShape();
        if (!area.getBounds().contains(p)) {
            return;
        }
        if (instantScrollEnabled(slider)) {
            int value = isHorizontal() ? valueForXPosition(p.x) : valueForYPosition(p.y);
            slider.setValue(value);
        } else {
            super.scrollDueToClickInTrack(dir);
        }
    }

    private void checkDisabled(final Graphics g) {
        if (!slider.isEnabled()) {
            g.setColor(getDisabledTickColor());
        }
    }


    protected Color getDisabledTickColor() {
        return inactiveTickForeground;
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        slider.putClientProperty(KEY_SHOW_FOCUS_GLOW, UIManager.getBoolean("Slider.paintFocusGlow"));
    }

    @Override
    protected void installDefaults(final JSlider slider) {
        super.installDefaults(slider);
        LookAndFeel.installProperty(slider, PropertyKey.OPAQUE, false);
        arcSize = UIManager.getInt("Slider.arc");
        trackSize = UIManager.getInt("Slider.trackThickness");
        plainThumbRadius = UIManager.getInt("Slider.plainThumbRadius");
        thumbSize = UIManager.getDimension("Slider.thumbSize");
        inactiveTickForeground = UIManager.getColor("Slider.disabledTickColor");
        trackBackground = UIManager.getColor("Slider.trackBackground");
        selectedTrackBackground = UIManager.getColor("Slider.selectedTrackColor");
        selectedTrackInactiveBackground = UIManager.getColor("Slider.disabledTrackColor");
        selectedVolumeTrackBackground = UIManager.getColor("Slider.volume.selectedTrackColor");
        selectedVolumeTrackInactiveBackground = UIManager.getColor("Slider.volume.disabledTrackColor");
        thumbBackground = UIManager.getColor("Slider.activeThumbFill");
        thumbInactiveBackground = UIManager.getColor("Slider.inactiveThumbFill");
        volumeThumbBackground = UIManager.getColor("Slider.volume.activeThumbFill");
        volumeThumbInactiveBackground = UIManager.getColor("Slider.volume.inactiveThumbFill");
        thumbBorderColor = UIManager.getColor("Slider.thumbBorderColor");
        thumbInactiveBorderColor = UIManager.getColor("Slider.thumbBorderColorDisabled");

        volume0 = UIManager.getIcon("Slider.volume.enabled_level_0.icon");
        volume1 = UIManager.getIcon("Slider.volume.enabled_level_1.icon");
        volume2 = UIManager.getIcon("Slider.volume.enabled_level_2.icon");
        volume3 = UIManager.getIcon("Slider.volume.enabled_level_3.icon");
        volume4 = UIManager.getIcon("Slider.volume.enabled_level_4.icon");
        volume0Inactive = UIManager.getIcon("Slider.volume.disabled_level_0.icon");
        volume1Inactive = UIManager.getIcon("Slider.volume.disabled_level_1.icon");
        volume2Inactive = UIManager.getIcon("Slider.volume.disabled_level_2.icon");
        volume3Inactive = UIManager.getIcon("Slider.volume.disabled_level_3.icon");
        volume4Inactive = UIManager.getIcon("Slider.volume.disabled_level_4.icon");
    }

    protected void calculateIconRect() {
        iconRect.width = getVolumeIcon().getIconWidth();
        iconRect.height = getVolumeIcon().getIconHeight();
        if (isHorizontal()) {
            if (slider.getComponentOrientation().isLeftToRight()) {
                iconRect.x = trackRect.x + trackRect.width + ICON_PAD;
                iconRect.y = trackRect.y + (trackRect.height - iconRect.height) / 2;
            } else {
                iconRect.x = trackRect.x - iconRect.width - ICON_PAD;
                iconRect.y = trackRect.y + (trackRect.height - iconRect.height) / 2;
            }
        } else {
            if (slider.getComponentOrientation().isLeftToRight()) {
                iconRect.x = trackRect.x + (trackRect.width - iconRect.width) / 2;
                iconRect.y = trackRect.y + trackRect.height + ICON_PAD;
            } else {
                iconRect.x = trackRect.x + (trackRect.width - iconRect.width) / 2;
                iconRect.y = trackRect.y - iconRect.height - ICON_PAD;
            }
        }
    }

    private Area getHorizontalSliderShape(final Area track) {
        double x = thumbRect.x + thumbRect.width / 2.0;
        Area leftArea = new Area(new Rectangle2D.Double(0, 0, x, slider.getHeight()));
        Area rightArea = new Area(new Rectangle2D.Double(x, 0, slider.getWidth() - x, slider.getHeight()));
        if (slider.getComponentOrientation().isLeftToRight()) {
            if (slider.getInverted()) {
                track.intersect(rightArea);
            } else {
                track.intersect(leftArea);
            }
        } else {
            if (!slider.getInverted()) {
                track.intersect(rightArea);
            } else {
                track.intersect(leftArea);
            }
        }
        return track;
    }

    protected Icon getVolumeIcon() {
        int range = slider.getMaximum() - slider.getMinimum();
        int value = slider.getValue() - slider.getMinimum();
        double percentage = value / (double) range;
        boolean enabled = slider.isEnabled();
        if (Math.abs(percentage) < 1E-6) {
            return enabled ? volume0 : volume0Inactive;
        } else if (percentage < 0.25) {
            return enabled ? volume1 : volume1Inactive;
        } else if (percentage < 0.5) {
            return enabled ? volume2 : volume2Inactive;
        } else if (percentage < 0.75) {
            return enabled ? volume3 : volume3Inactive;
        } else {
            return enabled ? volume4 : volume4Inactive;
        }
    }

    private boolean isHorizontal() {
        return slider.getOrientation() == JSlider.HORIZONTAL;
    }


    private Area getHorizontalTrackShape() {
        int arc = arcSize;
        int yOff = (trackRect.height / 2) - trackSize / 2;
        int w = showVolumeIcon(slider) ? trackRect.width + getIconBarExt() : trackRect.width;
        if (slider.getComponentOrientation().isLeftToRight()) {
            return new Area(new RoundRectangle2D.Double(
                trackRect.x, trackRect.y + yOff, w, trackSize, arc, arc));
        } else {
            return new Area(new RoundRectangle2D.Double(
                trackRect.x - getIconBarExt(), trackRect.y + yOff, w, trackSize, arc, arc));
        }
    }

    private Area getVerticalSliderShape(final Area track) {
        int y = thumbRect.y + thumbRect.height / 2;
        if (slider.getInverted()) {
            track.intersect(new Area(new Rectangle2D.Double(0, 0, slider.getWidth(), y)));
        } else {
            track.intersect(new Area(new Rectangle2D.Double(0, y, slider.getWidth(), slider.getHeight() - y)));
        }
        return track;
    }


    private Area getVerticalTrackShape() {
        int arc = arcSize;
        int xOff = (trackRect.width / 2) - trackSize / 2;
        int h = showVolumeIcon(slider) ? trackRect.height + getIconBarExt() : trackRect.height;
        if (slider.getComponentOrientation().isLeftToRight()) {
            return new Area(new RoundRectangle2D.Double(
                trackRect.x + xOff, trackRect.y, trackSize, h, arc, arc));
        } else {
            return new Area(new RoundRectangle2D.Double(
                trackRect.x + xOff, trackRect.y - getIconBarExt(), trackSize, h, arc, arc));

        }
    }

    private int getIconBarExt() {
        return isPlainThumb() && showVolumeIcon(slider) ? ICON_BAR_EXT : 0;
    }

    private void paintPlainSliderThumb(final Graphics2D g) {
        int r = plainThumbRadius;
        int x = isHorizontal() ? 4 : (thumbRect.width - r) / 2;
        int y = isHorizontal() ? (thumbRect.height - r) / 2 : 4;
        g.translate(x, y);
        Ellipse2D.Double thumb = new Ellipse2D.Double(0, 0, r, r);
        Ellipse2D.Double innerThumb = new Ellipse2D.Double(1, 1, r - 2, r - 2);
        if (paintFocus()) {
            DarkUIUtil.paintFocusOval(g, 1, 1, r - 2, r - 2);
        }
        if (isVolumeSlider(slider)) {
            g.setColor(getThumbColor());
            g.fill(thumb);
        } else {
            g.setColor(getThumbBorderColor());
            g.fill(thumb);
            g.setColor(getThumbColor());
            g.fill(innerThumb);
        }
        g.translate(-x, -y);
    }

    protected boolean isPlainThumb() {
        Boolean paintThumbArrowShape = (Boolean) slider.getClientProperty(KEY_THUMB_ARROW_SHAPE);
        return (!slider.getPaintTicks() && paintThumbArrowShape == null) ||
               paintThumbArrowShape == Boolean.FALSE;
    }

    private void paintSliderThumb(final Graphics2D g) {
        Path2D thumb = getThumbShape();
        if (paintFocus()) {
            GraphicsContext config = new GraphicsContext(g);
            g.setComposite(DarkUIUtil.GLOW_ALPHA);
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 8));
            DarkUIUtil.Outline.focus.setGraphicsColor(g, true);
            g.draw(thumb);
            config.restore();
        }
        g.setColor(getThumbColor());
        g.fill(thumb);
        g.setColor(getThumbBorderColor());
        g.draw(thumb);
    }

    private boolean paintFocus() {
        return slider.hasFocus() && Boolean.TRUE.equals(slider.getClientProperty(KEY_SHOW_FOCUS_GLOW));
    }

    private Path2D getThumbShape() {
        if (isHorizontal()) {
            return getHorizontalThumbShape();
        } else if (slider.getComponentOrientation().isLeftToRight()) {
            return getVerticalThumbShapeLR();
        } else {
            return getVerticalThumbShapeRL();
        }
    }


    private Path2D getHorizontalThumbShape() {
        int w = thumbRect.width;
        int h = thumbRect.height;
        int cw = w / 2;
        Path2D shape = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        shape.moveTo(1, 1);
        shape.lineTo(w - 2, 1);
        shape.lineTo(w - 2, h - cw - 1);
        shape.lineTo(cw, h - 2);
        shape.lineTo(1, h - cw - 1);
        shape.closePath();
        return shape;
    }


    private Path2D getVerticalThumbShapeLR() {
        int w = thumbRect.width;
        int h = thumbRect.height;
        int cw = h / 2;
        Path2D shape = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        shape.moveTo(2, 1);
        shape.lineTo(w - cw - 1, 1);
        shape.lineTo(w - 1, h - cw);
        shape.lineTo(w - cw - 1, h - 2);
        shape.lineTo(2, h - 2);
        shape.closePath();
        return shape;
    }


    private Path2D getVerticalThumbShapeRL() {
        int w = thumbRect.width;
        int h = thumbRect.height;
        int cw = h / 2;
        Path2D shape = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        shape.moveTo(w - 2, 1);
        shape.lineTo(cw + 1, 1);
        shape.lineTo(1, h - cw);
        shape.lineTo(cw + 1, h - 2);
        shape.lineTo(w - 2, h - 2);
        shape.closePath();
        return shape;
    }


    protected Color getThumbColor() {
        if (isVolumeSlider(slider)) {
            return slider.isEnabled() ? volumeThumbBackground : volumeThumbInactiveBackground;
        } else {
            return slider.isEnabled() ? thumbBackground : thumbInactiveBackground;
        }
    }


    protected Color getThumbBorderColor() {
        return slider.isEnabled() ? thumbBorderColor : thumbInactiveBorderColor;
    }


    protected Color getTrackBackground() {
        return trackBackground;
    }


    protected Color getSelectedTrackColor() {
        if (isVolumeSlider(slider)) {
            return slider.isEnabled() ? selectedVolumeTrackBackground : selectedVolumeTrackInactiveBackground;
        } else {
            return slider.isEnabled() ? selectedTrackBackground : selectedTrackInactiveBackground;
        }
    }

    private boolean instantScrollEnabled(final JComponent c) {
        return Boolean.TRUE.equals(c.getClientProperty(KEY_INSTANT_SCROLL));
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String key = evt.getPropertyName();
        if (KEY_VARIANT.equals(key)) {
            slider.repaint();
        } else if (DarkSliderUI.KEY_SHOW_VOLUME_ICON.equals(key)) {
            calculateGeometry();
            slider.repaint();
        }
    }

    public class SnapTrackListener extends TrackListener {
        private int offset;

        public void mousePressed(final MouseEvent evt) {
            int pos = isHorizontal() ? evt.getX() : evt.getY();
            int loc = getLocationForValue(getSnappedValue(evt));
            offset = (loc < 0) ? 0 : pos - loc;
            if (iconRect.contains(evt.getPoint())) return;
            super.mousePressed(evt);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            if (slider.getSnapToTicks()) {
                int pos = getLocationForValue(getSnappedValue(e));
                if (isHorizontal()) {
                    e.translatePoint(pos - e.getX() + offset, 0);
                } else {
                    e.translatePoint(0, pos - e.getY() + offset);
                }
            }
            super.mouseDragged(e);
            slider.repaint();
        }

        private int getLocationForValue(final int value) {
            return isHorizontal() ? xPositionForValue(value) : yPositionForValue(value);
        }

        private int getSnappedValue(final MouseEvent e) {
            int value = isHorizontal() ? valueForXPosition(e.getX())
                                       : valueForYPosition(e.getY());
            // Now calculate if we should adjust the value
            int snappedValue = value;
            int tickSpacing = 0;
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }
            // If it's not on a tick, change the value
            if (tickSpacing != 0) {
                if ((value - slider.getMinimum()) % tickSpacing != 0) {
                    float temp = (float) (value - slider.getMinimum())
                                 / (float) tickSpacing;
                    snappedValue = slider.getMinimum() +
                                   (Math.round(temp) * tickSpacing);
                }
            }
            return snappedValue;
        }
    }
}
