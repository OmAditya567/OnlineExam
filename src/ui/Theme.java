package ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

/**
 * Orange / red border theme. Call {@link #install()} once, before any window is created.
 */
public final class Theme {
    public static final Color ORANGE = new Color(0xF57C00);
    public static final Color RED = new Color(0xD32F2F);
    public static final Color ORANGE_LIGHT = new Color(0xFFB74D);

    private Theme() {}

    /** Sets the look and feel and the default borders for buttons, fields, tables, tabs and titled panels. */
    public static void install() {
        // The native macOS/Windows look ignores custom borders on buttons and fields,
        // so the cross-platform look is used to make the borders show up everywhere.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        Border fieldBorder = pad(gradient(2, 0), 3, 5, 3, 5);
        UIManager.put("Button.border", new BorderUIResource(pad(gradient(2, 0), 4, 14, 4, 14)));
        UIManager.put("TextField.border", new BorderUIResource(fieldBorder));
        UIManager.put("PasswordField.border", new BorderUIResource(fieldBorder));
        UIManager.put("ScrollPane.border", new BorderUIResource(gradient(2, 0)));
        UIManager.put("TitledBorder.border", new BorderUIResource(gradient(2, 0)));
        UIManager.put("TitledBorder.titleColor", RED);
        UIManager.put("TableHeader.cellBorder",
                new BorderUIResource(BorderFactory.createMatteBorder(0, 0, 2, 1, ORANGE)));
        UIManager.put("Table.gridColor", ORANGE_LIGHT);

        UIManager.put("TabbedPane.darkShadow", ORANGE);
        UIManager.put("TabbedPane.selectHighlight", RED);
        UIManager.put("TabbedPane.highlight", ORANGE_LIGHT);
        UIManager.put("TabbedPane.light", ORANGE_LIGHT);
        UIManager.put("TabbedPane.shadow", ORANGE_LIGHT);
        UIManager.put("TabbedPane.focus", RED);
        // Metal's Ocean theme reads the selected-tab outline from this key (the misspelling is the JDK's own).
        UIManager.put("TabbedPane.borderHightlightColor", RED);
        UIManager.put("TabbedPane.borderHighlightColor", RED);
        UIManager.put("Button.focus", RED);
        UIManager.put("RadioButton.focus", RED);
    }

    /** Thick gradient border around a window's content pane. */
    public static void frame(JFrame f) {
        ((JComponent) f.getContentPane()).setBorder(gradient(4, 0));
    }

    /** Gradient rectangle border, orange at top-left to red at bottom-right. */
    public static Border gradient(int thickness, int arc) {
        return new GradientBorder(thickness, arc, false);
    }

    /** Gradient line along the bottom edge only, with padding inside it. */
    public static Border underlined(int top, int left, int bottom, int right) {
        return pad(new GradientBorder(3, 0, true), top, left, bottom, right);
    }

    /** Boxed content area: gradient outline with padding, plus a margin outside it. */
    public static Border card(int margin, int padding) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(margin, margin, margin, margin),
                pad(gradient(2, 0), padding, padding, padding, padding));
    }

    /** Small rounded outline for a label, in the given colour. */
    public static Border pill(Color color) {
        return pad(BorderFactory.createLineBorder(color, 2, true), 3, 10, 3, 10);
    }

    private static Border pad(Border outer, int top, int left, int bottom, int right) {
        return BorderFactory.createCompoundBorder(outer, BorderFactory.createEmptyBorder(top, left, bottom, right));
    }

    private static final class GradientBorder extends AbstractBorder {
        private final int thickness;
        private final int arc;
        private final boolean bottomOnly;

        GradientBorder(int thickness, int arc, boolean bottomOnly) {
            this.thickness = thickness;
            this.arc = arc;
            this.bottomOnly = bottomOnly;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (bottomOnly) {
                g2.setPaint(new GradientPaint(x, 0, ORANGE, x + w, 0, RED));
                g2.fillRect(x, y + h - thickness, w, thickness);
            } else {
                g2.setPaint(new GradientPaint(x, y, ORANGE, x + w, y + h, RED));
                Area ring = new Area(new RoundRectangle2D.Float(x, y, w, h, arc, arc));
                int innerArc = Math.max(0, arc - 2 * thickness);
                ring.subtract(new Area(new RoundRectangle2D.Float(
                        x + thickness, y + thickness, w - 2f * thickness, h - 2f * thickness, innerArc, innerArc)));
                g2.fill(ring);
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return bottomOnly ? new Insets(0, 0, thickness, 0) : new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets i = getBorderInsets(c);
            insets.set(i.top, i.left, i.bottom, i.right);
            return insets;
        }
    }
}
