package cnuphys.chimera.dialog.gridparams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class GridTableExample {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Grid Table Example");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Create the model and table
            GridTableModel model = new GridTableModel();
            JTable table = new JTable(model);

            // Customize appearance for row 0 and column 0
            TableCellRenderer customRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (row == 0 || column == 0) {
                        c.setBackground(Color.LIGHT_GRAY);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                    return c;
                }
            };

            // Apply custom renderer
            for (int i = 0; i < model.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
            }

            // Add key listener to detect Enter key in editable fields
            table.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        int row = table.getEditingRow();
                        int column = table.getEditingColumn();

                        // Only respond if an editable field triggered the Enter key
                        if (model.isCellEditable(row, column)) {
                            // Stop editing and fetch the value
                            table.getCellEditor().stopCellEditing();
                            Object newValue = table.getValueAt(row, column);

                            // Callback logic (replace with actual callback implementation)
                            System.out.printf("Enter pressed at row %d, column %d. New value: %s%n", row, column, newValue);
                        }
                    }
                }
            });

            // Configure table properties
            table.setRowHeight(25);
            table.setPreferredScrollableViewportSize(new Dimension(600, 150));

            // Add the table to a scroll pane and frame
            frame.add(new JScrollPane(table), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

