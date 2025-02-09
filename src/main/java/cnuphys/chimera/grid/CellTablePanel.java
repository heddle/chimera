package cnuphys.chimera.grid;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * A panel that displays a list of Cell objects in a JTable with the following features:
 * <ul>
 *   <li>A row header (outside the table) displays a fixed ordinal index (1, 2, 3, …) that does not
 *       change when the table is sorted.</li>
 *   <li>The table has four columns: Nx, Ny, Nz, and Type. Clicking on any of the first three headers
 *       will sort using Nx as primary, Ny as secondary, and Nz as tertiary key (all ascending). Clicking
 *       on the Type header sorts by the type string.</li>
 *   <li>Single row selection only.</li>
 *   <li>Mouse click events (single and double clicks) on a row call registered callback methods,
 *       with the corresponding Cell passed to the callback.</li>
 *   <li>All cell content (including the row header) is centered, and the header text is bold.</li>
 * </ul>
 */
public class CellTablePanel extends JPanel {

    // The header labels for the table (excluding the row header).
    private static final String[] COLUMN_NAMES = {"   Nx", "   Ny", "   Nz", "   Type"};

    // The table model.
    private CellTableModel tableModel;

    // The table.
    private JTable table;

    // The row sorter.
    private TableRowSorter<CellTableModel> rowSorter;

    // Callback interfaces.
    public interface CellClickListener extends EventListener {
        void cellClicked(Cell cell);
    }

    public interface CellDoubleClickListener extends EventListener {
        void cellDoubleClicked(Cell cell);
    }

    private CellClickListener clickListener;
    private CellDoubleClickListener doubleClickListener;

    /**
     * Create the panel with the given list of cells.
     *
     * @param cells the list of Cell objects to display
     */
    public CellTablePanel(List<Cell> cells) {
        super(new BorderLayout());
        tableModel = new CellTableModel(cells);
        table = new JTable(tableModel);

        // Create the row sorter and disable sorting on the row header column (which is not part of the model).
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Set up comparators for the sorting columns.
        // For Nx, Ny, Nz: sort using natural integer order.
        Comparator<Integer> xyzComparator = Integer::compare;
        rowSorter.setComparator(0, xyzComparator); // Nx
        rowSorter.setComparator(1, xyzComparator); // Ny
        rowSorter.setComparator(2, xyzComparator); // Nz

        // For the Type column, compare the type strings.
        rowSorter.setComparator(3, (s1, s2) -> {
            String str1 = (String) s1;
            String str2 = (String) s2;
            return str1.compareTo(str2);
        });

        // Intercept header clicks to set our custom sort keys.
        JTableHeader header = table.getTableHeader();
        // Make header font bold.
        Font headerFont = header.getFont().deriveFont(Font.BOLD);
        header.setFont(headerFont);
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewColumn = header.columnAtPoint(e.getPoint());
                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                // For columns Nx, Ny, or Nz (model columns 0,1,2), sort using all three keys.
                if (modelColumn >= 0 && modelColumn <= 2) {
                    List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                    sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING)); // Nx
                    sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING)); // Ny
                    sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING)); // Nz
                    rowSorter.setSortKeys(sortKeys);
                }
                // If the Type column (model column 3) is clicked, sort by type string.
                else if (modelColumn == 3) {
                    List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                    sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
                    rowSorter.setSortKeys(sortKeys);
                }
            }
        });

        // Only allow single row selection.
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add mouse listener for row click events.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.getSelectedRow();
                if (viewRow < 0) {
                    return;
                }
                int modelRow = table.convertRowIndexToModel(viewRow);
                Cell cell = tableModel.getCellAt(modelRow);
                if (e.getClickCount() == 2) {
                    if (doubleClickListener != null) {
                        doubleClickListener.cellDoubleClicked(cell);
                    }
                } else if (e.getClickCount() == 1) {
                    if (clickListener != null) {
                        clickListener.cellClicked(cell);
                    }
                }
            }
        });

        // Center cell content in the table.
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Put the table in a scroll pane.
        JScrollPane scrollPane = new JScrollPane(table);

        // Create a row header (a JList) for the fixed ordinal numbers.
        JList<String> rowHeader = new JList<>(new RowHeaderListModel(tableModel));
        rowHeader.setFixedCellWidth(40);
        rowHeader.setFixedCellHeight(table.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));
        scrollPane.setRowHeaderView(rowHeader);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Set the single-click callback.
     *
     * @param listener the listener to be called on single click
     */
    public void setCellClickListener(CellClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Set the double-click callback.
     *
     * @param listener the listener to be called on double click
     */
    public void setCellDoubleClickListener(CellDoubleClickListener listener) {
        this.doubleClickListener = listener;
    }

    /**
     * Update the table data.
     *
     * @param cells the new list of cells
     */
    public void updateCells(List<Cell> cells) {
        tableModel.setCells(cells);
    }

    /**
     * The table model for Cells (without the ordinal column).
     */
    private static class CellTableModel extends AbstractTableModel {

        private List<Cell> cells;

        public CellTableModel(List<Cell> cells) {
            this.cells = new ArrayList<>(cells);
        }

        public void setCells(List<Cell> cells) {
            this.cells = new ArrayList<>(cells);
            fireTableDataChanged();
        }

        public Cell getCellAt(int rowIndex) {
            return cells.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return cells.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Cell cell = cells.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return cell.nx;
                case 1:
                    return cell.ny;
                case 2:
                    return cell.nz;
                case 3:
                    int type = cell.getIntersectionType();
                    if (type < 0 || type >= Cell.intersectionTypes.length) {
                        return "unknown";
                    }
                    return Cell.intersectionTypes[type];
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                case 1:
                case 2:
                    return Integer.class;
                case 3:
                    return String.class;
                default:
                    return Object.class;
            }
        }
    }

    /**
     * A ListModel for the row header, which simply returns the row numbers as Strings.
     */
    private static class RowHeaderListModel extends AbstractListModel<String> {

        private final TableModel tableModel;

        public RowHeaderListModel(TableModel tableModel) {
            this.tableModel = tableModel;
            // Listen for changes in the table model.
            tableModel.addTableModelListener((TableModelEvent e) -> {
                fireContentsChanged(this, 0, getSize());
            });
        }

        @Override
        public int getSize() {
            return tableModel.getRowCount();
        }

        @Override
        public String getElementAt(int index) {
            return String.valueOf(index + 1);
        }
    }

    /**
     * A cell renderer for the row header (JList) that mimics the table header style.
     */
    private static class RowHeaderRenderer extends DefaultListCellRenderer {
        public RowHeaderRenderer(JTable table) {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 5, 0, 5));
            setFont(table.getTableHeader().getFont());
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            // Simply display the row number.
            return super.getListCellRendererComponent(list, value, index, false, false);
        }
    }

    /**
     * Create and display the modeless dialog with the CellTablePanel.
     *
     * @param cells the list of Cell objects to display
     */
    public static void showDialog(List<Cell> cells) {
        // Create the panel.
        CellTablePanel cellTablePanel = new CellTablePanel(cells);

        // Optionally, set up callbacks.
        cellTablePanel.setCellClickListener(cell -> {
            System.out.println("Single-clicked on: " + cell);
        });
        cellTablePanel.setCellDoubleClickListener(cell -> {
            System.out.println("Double-clicked on: " + cell);
        });

        // Create a modeless dialog.
        JDialog dialog = new JDialog((JFrame) null, "Cell Table", false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(cellTablePanel, BorderLayout.CENTER);

        // Add a Close button.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener((ActionEvent e) -> dialog.dispose());
        buttonPanel.add(closeButton);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // For testing/demo purposes.
    public static void main(String[] args) {
        // Create some demo cells.
        List<Cell> cellList = new ArrayList<>();
        // NOTE: Replace 'null' for the grid and a dummy radius.
        cellList.add(new Cell(null, 5, 2, 7, 0b00001111, 1.0));
        cellList.add(new Cell(null, 3, 6, 2, 0b00101010, 1.0));
        cellList.add(new Cell(null, 5, 2, 3, 0b11110000, 1.0));
        cellList.add(new Cell(null, 3, 2, 9, 0b10101010, 1.0));
        cellList.add(new Cell(null, 1, 8, 5, 0b01010101, 1.0));

        SwingUtilities.invokeLater(() -> showDialog(cellList));
    }
}
