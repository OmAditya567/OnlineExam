package ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Rounded red-orange look. Call Modern.apply(getContentPane()) at the end of a frame's constructor. */
public final class Modern {
    private Modern() {}

    public static final Color RED    = new Color(0xE63946);
    public static final Color ORANGE = new Color(0xFF8C42);
    public static final Color BG     = new Color(0xFFF6EF);
    public static final Color PEACH  = new Color(0xFFE6D5);
    public static final Color LINE   = new Color(0xFFCBA8);
    public static final Color TEXT   = new Color(0x3D2B25);

    private static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private static Color mix(Color a, Color b, float t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                         (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                         (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** Walks the component tree and restyles everything it recognises. */
    public static void apply(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof GradientPanel) {
                // header: leave its own painting alone
            } else if (c instanceof JTabbedPane) {
                JTabbedPane t = (JTabbedPane) c;
                t.setUI(new PillTabsUI());
                t.setFont(t.getFont().deriveFont(Font.BOLD, 13f));
                t.setBackground(BG);
                t.setOpaque(true);
            } else if (c instanceof JButton) {
                JButton b = (JButton) c;
                b.setUI(new RoundButtonUI());
            } else if (c instanceof JTable) {
                styleTable((JTable) c);
            } else if (c instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) c;
                sp.setBorder(new RoundedBorder(LINE, 16, 1, 6));
                sp.getViewport().setBackground(Color.WHITE);
                sp.setOpaque(false);
            } else if (c instanceof JTextField) {
                ((JTextField) c).setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(LINE, 12, 1, 2), new EmptyBorder(4, 8, 4, 8)));
            } else if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                p.setBackground(BG);
                if (p.getBorder() instanceof TitledBorder) {
                    TitledBorder tb = (TitledBorder) p.getBorder();
                    tb.setBorder(new RoundedBorder(LINE, 16, 1, 4));
                    tb.setTitleColor(RED);
                    tb.setTitleFont(tb.getTitleFont() == null ? p.getFont().deriveFont(Font.BOLD)
                                                              : tb.getTitleFont().deriveFont(Font.BOLD));
                }
            }
            if (c instanceof Container) apply((Container) c);
        }
    }

    private static void styleTable(JTable t) {
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(PEACH);
        t.setSelectionForeground(TEXT);
        t.setForeground(TEXT);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        if (h != null) {
            h.setReorderingAllowed(false);
            h.setPreferredSize(new Dimension(h.getPreferredSize().width, 36));
            h.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int col) {
                    JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, col);
                    l.setOpaque(true);
                    l.setBackground(PEACH);
                    l.setForeground(RED);
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                    l.setBorder(new EmptyBorder(0, 2, 0, 2));
                    return l;
                }
            });
        }
    }

    /** Header bar with a red -> orange gradient and rounded bottom corners. */
    public static class GradientPanel extends JPanel {
        public GradientPanel(LayoutManager lm) { super(lm); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setPaint(new GradientPaint(0, 0, RED, getWidth(), 0, ORANGE));
            g2.fillRoundRect(0, -30, getWidth(), getHeight() + 30, 30, 30);
            g2.dispose();
        }
    }

    public static class RoundedBorder extends AbstractBorder {
        private final Color color; private final int radius, thick, pad;
        public RoundedBorder(Color color, int radius, int thick, int pad) {
            this.color = color; this.radius = radius; this.thick = thick; this.pad = pad;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = smooth(g);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thick));
            g2.drawRoundRect(x + thick / 2, y + thick / 2, w - thick - 1, h - thick - 1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) {
            int i = thick + pad + radius / 4;
            return new Insets(i, i, i, i);
        }
        @Override public Insets getBorderInsets(Component c, Insets in) {
            in.top = in.left = in.bottom = in.right = thick + pad + radius / 4;
            return in;
        }
    }

    /** Pill button. Set b.putClientProperty("variant", "ghost") for the white-outline version (use on the gradient header). */
    public static class RoundButtonUI extends BasicButtonUI {
        @Override public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setRolloverEnabled(true);
            b.setForeground(Color.WHITE);
            b.setFont(b.getFont().deriveFont(Font.BOLD));
            b.setBorder(new EmptyBorder(8, 20, 8, 20));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel m = b.getModel();
            Graphics2D g2 = smooth(g);
            int w = c.getWidth(), h = c.getHeight();
            if ("ghost".equals(c.getClientProperty("variant"))) {
                g2.setColor(new Color(255, 255, 255, m.isRollover() ? 90 : 50));
                g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            } else {
                Color a = RED, o = ORANGE;
                if (!c.isEnabled()) { a = new Color(0xCCCCCC); o = new Color(0xDDDDDD); }
                else if (m.isPressed()) { a = a.darker(); o = o.darker(); }
                else if (m.isRollover()) { a = mix(a, Color.WHITE, 0.15f); o = mix(o, Color.WHITE, 0.15f); }
                g2.setPaint(new GradientPaint(0, 0, a, w, 0, o));
                g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            }
            g2.dispose();
            super.paint(g, c);
        }
    }

    /** Rounded pill tabs: selected = red-orange gradient, others = soft peach. */
    public static class PillTabsUI extends BasicTabbedPaneUI {
        @Override protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(8, 16, 8, 16);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(10, 12, 8, 12);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }
        @Override protected int calculateTabWidth(int p, int i, FontMetrics m) { return super.calculateTabWidth(p, i, m) + 10; }
        @Override protected int calculateTabHeight(int p, int i, int fh) { return super.calculateTabHeight(p, i, fh) + 4; }
        @Override protected void paintTabBackground(Graphics g, int p, int i, int x, int y, int w, int h, boolean sel) {
            Graphics2D g2 = smooth(g);
            int rx = x + 5, ry = y + 2, rw = w - 10, rh = h - 4;
            if (sel) g2.setPaint(new GradientPaint(rx, 0, RED, rx + rw, 0, ORANGE));
            else g2.setColor(PEACH);
            g2.fillRoundRect(rx, ry, rw, rh, rh, rh);
            g2.dispose();
        }
        @Override protected int getTabLabelShiftY(int p, int i, boolean sel) { return 0; }
        @Override protected int getTabLabelShiftX(int p, int i, boolean sel) { return 0; }
        @Override protected void paintTabBorder(Graphics g, int p, int i, int x, int y, int w, int h, boolean sel) {}
        @Override protected void paintFocusIndicator(Graphics g, int p, Rectangle[] r, int i, Rectangle ir, Rectangle tr, boolean sel) {}
        @Override protected void paintContentBorder(Graphics g, int p, int sel) {}
        @Override protected void paintText(Graphics g, int p, Font font, FontMetrics fm, int i, String title, Rectangle tr, boolean sel) {
            Graphics2D g2 = smooth(g);
            g2.setFont(font);
            g2.setColor(sel ? Color.WHITE : TEXT);
            g2.drawString(title, tr.x, tr.y + fm.getAscent());
            g2.dispose();
        }
    }
}