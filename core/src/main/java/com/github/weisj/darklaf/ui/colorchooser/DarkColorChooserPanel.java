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

package com.github.weisj.darklaf.ui.colorchooser;

import com.github.weisj.darklaf.color.DarkColorModel;
import com.github.weisj.darklaf.components.DefaultColorPipette;
import com.github.weisj.darklaf.components.uiresource.JButtonUIResource;
import com.github.weisj.darklaf.decorators.AncestorAdapter;
import com.github.weisj.darklaf.util.ColorUtil;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author pegov
 * @author Konstantin Bulenkov
 * @author Jannis Weis
 */
public class DarkColorChooserPanel extends AbstractColorChooserPanel implements ColorListener {

    public static final String TRANSPARENCY_ENABLED_PROPERTY
            = "TransparencyEnabled";

    private final ColorPipette pipette;
    private final ColorWheelPanel colorWheelPanel;
    private final JFormattedTextField textHex;
    private final ColorValueFormatter hexFormatter;
    private final JFormattedTextField[] valueFields;
    private final ColorValueFormatter[] formatters;

    private final JComboBox<DarkColorModel> formatBox;

    private final ColorPreviewComponent previewComponent;
    private final JLabel[] descriptors;
    private final JLabel[] descriptorsAfter;
    private final boolean doneInit;
    private Color currentColor;
    private boolean isChanging;
    private Icon pipetteIcon;
    private Icon pipetteHoverIcon;


    public DarkColorChooserPanel(final DarkColorModel... colorModels) {
        if (colorModels == null || colorModels.length == 0) {
            throw new IllegalArgumentException("Must pass at least one valid colorModel");
        }

        previewComponent = new ColorPreviewComponent();
        colorWheelPanel = new ColorWheelPanel(this, true, true);
        pipette = new DefaultColorPipette(this, colorWheelPanel::setColor);
        pipetteIcon = UIManager.getIcon("ColorChooser.pipette.icon");
        pipetteHoverIcon = UIManager.getIcon("ColorChooser.pipetteRollover.icon");

        formatBox = new JComboBox<>(colorModels);
        formatBox.addActionListener(e -> {
            updateDescriptors();
            updateValueFields();
            updateFormatters();
            applyColorToFields(getColorFromModel());
            doLayout();
        });
        int record = 0;
        DarkColorModel prototype = null;
        for (DarkColorModel model : colorModels) {
            record = Math.max(model.getValueCount(), record);
            String name = model.toString();
            if (prototype == null || prototype.toString().length() < name.length()) {
                prototype = model;
            }
        }

        formatBox.setPrototypeDisplayValue(prototype);
        descriptors = new JLabel[record];
        descriptorsAfter = new JLabel[record];

        textHex = createColorField(true);
        textHex.addAncestorListener(new AncestorAdapter() {
            @Override
            public void ancestorAdded(final AncestorEvent event) {
                textHex.requestFocus();
                textHex.removeAncestorListener(this);
            }
        });

        hexFormatter = ColorValueFormatter.init(getDarkColorModel(), 0, true, textHex);
        hexFormatter.setTransparencyEnabled(isColorTransparencySelectionEnabled());

        valueFields = new JFormattedTextField[record];
        formatters = new ColorValueFormatter[record];

        for (int i = 0; i < record; i++) {
            descriptors[i] = new JLabel();
            descriptorsAfter[i] = new JLabel();
            valueFields[i] = createColorField(false);
            formatters[i] = ColorValueFormatter.init(getDarkColorModel(), i, false, valueFields[i]);
        }

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        add(buildTopPanel(UIManager.getBoolean("ColorChooser.pipetteEnabled")), BorderLayout.NORTH);
        add(colorWheelPanel, BorderLayout.CENTER);
        add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        updateValueFields();
        updateDescriptors();
        doneInit = true;
    }

    private void updateDescriptors() {
        char[] desc = getDarkColorModel().getLabelDescriptorsBefore();
        char[] descAfter = getDarkColorModel().getLabelDescriptorsAfter();
        for (int i = 0; i < descriptors.length; i++) {
            if (i < desc.length) {
                descriptors[i].setText(desc[i] + ":");
            } else {
                descriptors[i].setText("");
            }
            if (i < descAfter.length) {
                descriptorsAfter[i].setText(String.valueOf(descAfter[i]));
            } else {
                descriptorsAfter[i].setText("");
            }
        }
    }

    private void updateValueFields() {
        DarkColorModel model = getDarkColorModel();
        int count = model.getValueCount();
        for (int i = 0; i < valueFields.length; i++) {
            valueFields[i].setEnabled(i < count);
            valueFields[i].setVisible(i < count);
        }
    }

    private void updateFormatters() {
        for (ColorValueFormatter formatter : formatters) {
            formatter.setModel(getDarkColorModel());
        }
    }

    private void applyColorToFields(final Color color) {
        DarkColorModel model = getDarkColorModel();
        isChanging = true;
        int[] values = model.getValuesFromColor(color);
        for (int i = 0; i < values.length; i++) {
            valueFields[i].setValue(values[i]);
        }
        isChanging = false;
    }

    @Override
    protected Color getColorFromModel() {
        Color c = super.getColorFromModel();
        return c == null ? currentColor : c;
    }

    protected DarkColorModel getDarkColorModel() {
        return (DarkColorModel) formatBox.getSelectedItem();
    }


    private JComponent buildTopPanel(final boolean enablePipette) {
        final JPanel result = new JPanel(new BorderLayout());

        final JPanel previewPanel = new JPanel(new BorderLayout());
        if (enablePipette && pipette != null) {
            JButton pipetteButton = new JButtonUIResource();
            pipetteButton.putClientProperty("JButton.variant", "onlyLabel");
            pipetteButton.putClientProperty("JButton.thin", Boolean.TRUE);
            pipetteButton.setRolloverEnabled(true);
            pipetteButton.setIcon(getPipetteIcon());
            pipetteButton.setRolloverIcon(getPipetteRolloverIcon());
            pipetteButton.setDisabledIcon(getPipetteRolloverIcon());
            pipetteButton.setPressedIcon(getPipetteRolloverIcon());
            pipetteButton.setFocusable(false);
            pipetteButton.addActionListener(e -> {
                pipetteButton.setEnabled(false);
                pipette.setInitialColor(getColorFromModel());
                pipette.show();
            });
            ((DefaultColorPipette) pipette).setCloseAction(() -> pipetteButton.setEnabled(true));
            previewPanel.add(pipetteButton, BorderLayout.WEST);
        }
        previewPanel.add(previewComponent, BorderLayout.CENTER);
        result.add(previewPanel, BorderLayout.NORTH);

        final JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.X_AXIS));
        valuePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        for (int i = 0; i < descriptorsAfter.length; i++) {
            descriptorsAfter[i].setPreferredSize(new Dimension(14, -1));
            descriptors[i].setPreferredSize(new Dimension(14, -1));
            valuePanel.add(descriptors[i]);
            valuePanel.add(valueFields[i]);
            valuePanel.add(descriptorsAfter[i]);
            if (i < descriptorsAfter.length - 1) {
                valuePanel.add(Box.createHorizontalStrut(2));
            }
        }
        result.add(valuePanel, BorderLayout.WEST);

        final JPanel hexPanel = new JPanel();
        hexPanel.setLayout(new BoxLayout(hexPanel, BoxLayout.X_AXIS));
        hexPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        hexPanel.add(formatBox);
        hexPanel.add(Box.createHorizontalStrut(2));
        hexPanel.add(new JLabel("#"));
        hexPanel.add(textHex);

        result.add(hexPanel, BorderLayout.EAST);
        return result;
    }


    private JFormattedTextField createColorField(final boolean hex) {
        JFormattedTextField field = new JFormattedTextField(0);
        field.setColumns(hex ? 8 : 4);
        if (!hex) {
            field.addPropertyChangeListener(e -> {
                if ("value".equals(e.getPropertyName())) {
                    updatePreviewFromTextFields();
                }
            });
        } else {
            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(final DocumentEvent e) {
                    update();
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    update();
                }

                @Override
                public void changedUpdate(final DocumentEvent e) {
                }

                protected void update() {
                    try {
                        if (isChanging) return;
                        String hexStr = String.format("%1$-" + 8 + "s", field.getText()).replaceAll(" ", "F");
                        int alpha = isColorTransparencySelectionEnabled()
                                    ? Integer.valueOf(hexStr.substring(6, 8), 16) : 255;
                        Color c = new Color(
                                Integer.valueOf(hexStr.substring(0, 2), 16),
                                Integer.valueOf(hexStr.substring(2, 4), 16),
                                Integer.valueOf(hexStr.substring(4, 6), 16),
                                alpha);
                        colorWheelPanel.setColor(c, textHex);
                    } catch (NumberFormatException | IndexOutOfBoundsException ignore) {}
                }
            });
        }
        field.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        return field;
    }

    protected Icon getPipetteIcon() {
        return pipetteIcon;
    }

    protected Icon getPipetteRolloverIcon() {
        return pipetteHoverIcon;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(520, 420);
    }

    @Override
    public void updateChooser() {
        if (isChanging) return;
        Color color = getColorFromModel();
        if (color != null) {
            colorWheelPanel.setColor(color, this);
        }
    }

    @Override
    protected void buildChooser() {
    }

    @Override
    public String getDisplayName() {
        return "Color Wheel";
    }

    @Override
    public int getMnemonic() {
        return KeyEvent.VK_W;
    }

    @Override
    public int getDisplayedMnemonicIndex() {
        return 6;
    }

    @Override
    public Icon getSmallDisplayIcon() {
        return null;
    }

    public boolean isColorTransparencySelectionEnabled() {
        return colorWheelPanel.isColorTransparencySelectionEnabled();
    }

    @Override
    public Icon getLargeDisplayIcon() {
        return null;
    }

    private void updatePreviewFromTextFields() {
        if (!doneInit || isChanging) return;
        isChanging = true;
        int[] values = new int[valueFields.length];
        for (int i = 0; i < valueFields.length; i++) {
            values[i] = (((Integer) valueFields[i].getValue()));
        }
        Color color = getDarkColorModel().getColorFromValues(values);

        if (isColorTransparencySelectionEnabled()) {
            color = ColorUtil.toAlpha(color, getColorFromModel().getAlpha());
        }
        colorWheelPanel.setColor(color, valueFields[0]);
        isChanging = false;
    }

    public void setColorTransparencySelectionEnabled(final boolean b) {
        boolean oldValue = isColorTransparencySelectionEnabled();
        if (b != oldValue) {
            Color color = getColorFromModel();
            color = new Color(color.getRed(), color.getBlue(), color.getGreen());
            ColorSelectionModel model = getColorSelectionModel();
            if (model != null) {
                model.setSelectedColor(color);
            }
            currentColor = color;
            hexFormatter.setTransparencyEnabled(b);
            colorWheelPanel.setColorTransparencySelectionEnabled(b);
            applyColorToHEX(getColorFromModel());
            firePropertyChange(TRANSPARENCY_ENABLED_PROPERTY,
                               oldValue, b);
        }
    }

    @Override
    public void colorChanged(final Color color, final Object source) {
        isChanging = true;
        if (color != null && !color.equals(currentColor)) {
            Color newColor = !isColorTransparencySelectionEnabled()
                             ? new Color(color.getRed(), color.getGreen(), color.getBlue()) : color;
            ColorSelectionModel model = getColorSelectionModel();
            if (model != null) {
                model.setSelectedColor(newColor);
            }
            currentColor = newColor;
            previewComponent.setColor(newColor);
            if (!(source instanceof JFormattedTextField)) {
                applyColorToFields(newColor);
            }
            if (source != textHex) {
                applyColorToHEX(newColor);
            }
        }
        isChanging = false;
    }


    private void applyColorToHEX(final Color c) {
        boolean changingOld = isChanging;
        isChanging = true;
        boolean transparencyEnabled = isColorTransparencySelectionEnabled();
        if (transparencyEnabled) {
            textHex.setValue(c);
        } else {
            textHex.setValue(ColorUtil.removeAlpha(c));
        }
        isChanging = changingOld;
    }


}
