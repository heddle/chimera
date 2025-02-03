package cnuphys.chimera.dialog.gridparams;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class GridTable extends JTable {

	public GridTable() {
		super(new GridTableModel());

		// Customize appearance for row 0 and column 0
		TableCellRenderer customRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				boolean isEditable = getModel().isCellEditable(row, column);

				if (row == 0 || column == 0) {
					c.setBackground(Color.LIGHT_GRAY);
					c.setForeground(Color.BLACK);
					c.setFont(c.getFont().deriveFont(Font.BOLD));
				} else {
					c.setBackground(Color.WHITE);
					if (!isEditable) {
						c.setForeground(Color.GRAY);
					} else {
						if (hasFocus) {
							c.setForeground(Color.RED);
						} else {
							c.setForeground(Color.BLUE);
						}
					}
				}

				return c;
			}
		};

		// Apply custom renderer
		for (int i = 0; i < getModel().getColumnCount(); i++) {
			this.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
		}

		// Add key listener to detect Enter key in editable fields
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleEnterKey();
				}
			}
		});

		// Configure table properties
		this.setRowHeight(25);
		this.setPreferredScrollableViewportSize(new Dimension(600, 150));
	}

	@Override
	public Component prepareEditor(javax.swing.table.TableCellEditor editor, int row, int column) {
		Component c = super.prepareEditor(editor, row, column);

		// Add key listener to the editor component
		c.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleEnterKey();
				}
			}
		});

		c.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				JTextField tf = (JTextField) c;
				tf.selectAll();
			}

			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				handleEnterKey();
			}
		});

		return c;
	}

	private void handleEnterKey() {
		int row = getEditingRow();
		int column = getEditingColumn();

		if (row == -1 || column == -1) {
			return; // No cell is being edited
		}

		// Only respond if an editable field triggered the Enter key
		if (getModel().isCellEditable(row, column)) {
			Object oldValue = getModel().getValueAt(row, column);

			try {
				handleCellEdit(row, column);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.err.println("Restoring previous value");
				getModel().setValueAt(oldValue, row, column);
				e.printStackTrace();
			}
		}
	}

	//
	private void handleCellEdit(int row, int column) {
		// Stop editing and fetch the value

		Component editorComponent = getEditorComponent();
		getCellEditor().stopCellEditing();



	    if (editorComponent instanceof JTextField) {
	        String newValue = ((JTextField) editorComponent).getText().trim();

	        // Validate and update the model based on row and column
	        try {
	            Object parsedValue = parseValue(row, column, newValue);
	            if (parsedValue != null) {
	                getModel().setValueAt(parsedValue, row, column);
	                System.out.printf("Updated value at row %d, column %d: %s%n", row, column, parsedValue);
	            }
	        } catch (NumberFormatException e) {
	            System.err.printf("Invalid input '%s' at row %d, column %d. Error: %s%n", newValue, row, column, e.getMessage());
	            // Optionally, show an error dialog or reset the editor to the previous value
	            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
	        }
	    }



		revalidate();
		repaint();
	}

	/**
	 * Parses the new value based on the row and column.
	 *
	 * @param row       The row being edited.
	 * @param column    The column being edited.
	 * @param newValue  The new value entered by the user.
	 * @return The parsed value to be set in the model, or null if no update is needed.
	 * @throws NumberFormatException If the input is invalid.
	 */
	private Object parseValue(int row, int column, String newValue) throws NumberFormatException {
	    switch (row) {
	        case GridTableModel.X_INDEX:
	        case GridTableModel.Y_INDEX:
	        case GridTableModel.Z_INDEX:
	            return parseCartesianValue(column, newValue);

	        case GridTableModel.THETA_INDEX:
	        case GridTableModel.PHI_INDEX:
	            if (column == GridTableModel.NUM_INDEX) {
	                return Integer.parseInt(newValue);
	            }
	            break;
	    }
	    return null; // No update required for this cell
	}

	/**
	 * Parses a Cartesian coordinate value (MIN, MAX, or NUM).
	 *
	 * @param column    The column being edited.
	 * @param newValue  The new value entered by the user.
	 * @return The parsed value to be set in the model.
	 * @throws NumberFormatException If the input is invalid.
	 */
	private Object parseCartesianValue(int column, String newValue) throws NumberFormatException {
	    switch (column) {
	        case GridTableModel.MIN_INDEX:
	        case GridTableModel.MAX_INDEX:
	            return Double.parseDouble(newValue);

	        case GridTableModel.NUM_INDEX:
	            return Integer.parseInt(newValue);

	        default:
	            throw new IllegalArgumentException("Unsupported column for Cartesian values: " + column);
	    }
	}

}
