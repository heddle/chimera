package cnuphys.chimera.dialog.gridparams;

import javax.swing.table.DefaultTableModel;

import cnuphys.chimera.frame.Chimera;
import cnuphys.chimera.grid.CartesianGrid;
import cnuphys.chimera.grid.ChimeraGrid;
import cnuphys.chimera.grid.SphericalGrid;
import cnuphys.chimera.util.ThetaPhi;


public class GridTableModel extends DefaultTableModel {

	// column indices
	public static final int MIN_INDEX = 1;
	public static final int MAX_INDEX = 2;
	public static final int NUM_INDEX = 3;
	public static final int DEL_INDEX = 4;

	// row indices
	public static final int NAME_INDEX = 0;
	public static final int X_INDEX = 1;
	public static final int Y_INDEX = 2;
	public static final int Z_INDEX = 3;
	public static final int THETA_INDEX = 4;
	public static final int PHI_INDEX = 5;

	// the names of the columns
	protected static final String colNames[] = { " ", "  Min", "  Max", "  Num", "  Del" };

	private static final String emptyNames[] = { " ", " ", " ", " ", " " };

	// names of the rows
	protected static final String rowNames[] = { " ", "  x", "  y", "  z", "  " + ThetaPhi.SMALL_THETA,
			"  " + ThetaPhi.SMALL_PHI };

	// the column widths
	protected static final int columnWidths[] = { 30, // name
			60, // min
			100, // max
			80, // num
			100 // del
	};

	/**
	 * Constructor
	 */
	public GridTableModel() {
		super(emptyNames, 6);
	}

	/**
	 * Get the number of columns
	 *
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	/**
	 * Get the number of rows
	 *
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		return 6;
	}

	@Override
	public Object getValueAt(int row, int col) {
	    ChimeraGrid grid = Chimera.getInstance().getGridCopy();
	    CartesianGrid cgrid = grid.getCartesianGrid();
	    SphericalGrid sgrid = grid.getSphericalGrid();

	    // Handle header rows and columns
	    if (col == 0) {
	        return rowNames[row];
	    }
	    if (row == 0) {
	        return colNames[col];
	    }

	    // Determine the value dynamically based on the row and column
	    switch (row) {
	        case X_INDEX:
	            return getCartesianGridValue(cgrid, col, "X");

	        case Y_INDEX:
	            return getCartesianGridValue(cgrid, col, "Y");

	        case Z_INDEX:
	            return getCartesianGridValue(cgrid, col, "Z");

	        case THETA_INDEX:
	            return getSphericalGridValue(sgrid, col, true);

	        case PHI_INDEX:
	            return getSphericalGridValue(sgrid, col, false);

	        default:
	            return "?"; // Unsupported row
	    }
	}

	/**
	 * Gets a value from the CartesianGrid based on the column.
	 *
	 * @param cgrid The CartesianGrid instance.
	 * @param col   The column index.
	 * @param axis  The axis ("X", "Y", or "Z").
	 * @return The value at the specified column for the CartesianGrid.
	 */
	private Object getCartesianGridValue(CartesianGrid cgrid, int col, String axis) {
	    switch (col) {
	        case MIN_INDEX:
	            return getCartesianMin(cgrid, axis);
	        case MAX_INDEX:
	            return getCartesianMax(cgrid, axis);
	        case NUM_INDEX:
	            return getCartesianNum(cgrid, axis);
	        case DEL_INDEX:
	            return formatValue(getCartesianDel(cgrid, axis));
	        default:
	            return "?"; // Unsupported column
	    }
	}

	/**
	 * Gets a value from the SphericalGrid based on the column.
	 *
	 * @param sgrid       The SphericalGrid instance.
	 * @param col         The column index.
	 * @param isTheta     True if row is for Theta; false for Phi.
	 * @return The value at the specified column for the SphericalGrid.
	 */
	private Object getSphericalGridValue(SphericalGrid sgrid, int col, boolean isTheta) {
	    switch (col) {
	        case MIN_INDEX:
	            return isTheta ? 0 : -180;
	        case MAX_INDEX:
	            return isTheta ? 180 : 180;
	        case NUM_INDEX:
	            return isTheta ? sgrid.getNumTheta() : sgrid.getNumPhi();
	        case DEL_INDEX:
	            double delta = isTheta ? sgrid.getThetaDel() : sgrid.getPhiDel();
	            return formatValue(Math.toDegrees(delta));
	        default:
	            return "?"; // Unsupported column
	    }
	}

	/**
	 * Retrieves the minimum value for the specified Cartesian axis.
	 */
	private double getCartesianMin(CartesianGrid cgrid, String axis) {
	    switch (axis) {
	        case "X":
	            return cgrid.getXMin();
	        case "Y":
	            return cgrid.getYMin();
	        case "Z":
	            return cgrid.getZMin();
	        default:
	            throw new IllegalArgumentException("Unsupported axis: " + axis);
	    }
	}

	/**
	 * Retrieves the maximum value for the specified Cartesian axis.
	 */
	private double getCartesianMax(CartesianGrid cgrid, String axis) {
	    switch (axis) {
	        case "X":
	            return cgrid.getXMax();
	        case "Y":
	            return cgrid.getYMax();
	        case "Z":
	            return cgrid.getZMax();
	        default:
	            throw new IllegalArgumentException("Unsupported axis: " + axis);
	    }
	}

	/**
	 * Retrieves the number of divisions for the specified Cartesian axis.
	 */
	private int getCartesianNum(CartesianGrid cgrid, String axis) {
	    switch (axis) {
	        case "X":
	            return cgrid.getNumX();
	        case "Y":
	            return cgrid.getNumY();
	        case "Z":
	            return cgrid.getNumZ();
	        default:
	            throw new IllegalArgumentException("Unsupported axis: " + axis);
	    }
	}

	/**
	 * Retrieves the delta value for the specified Cartesian axis.
	 */
	private double getCartesianDel(CartesianGrid cgrid, String axis) {
	    switch (axis) {
	        case "X":
	            return cgrid.getXDel();
	        case "Y":
	            return cgrid.getYDel();
	        case "Z":
	            return cgrid.getZDel();
	        default:
	            throw new IllegalArgumentException("Unsupported axis: " + axis);
	    }
	}

	/**
	 * Formats a double value with fixed width and precision.
	 *
	 * @param value The value to format.
	 * @return The formatted string.
	 */
	private String formatValue(double value) {
	    return String.format("%-11.5f", value);
	}


	@Override
	public void setValueAt(Object value, int row, int column) {
	    // Get the copy that has stuffed the editor
	    ChimeraGrid grid = Chimera.getInstance().getGridCopy();
	    CartesianGrid cgrid = grid.getCartesianGrid();
	    SphericalGrid sgrid = grid.getSphericalGrid();

	    try {
	        switch (row) {
	            case GridTableModel.X_INDEX:
	                updateCartesianGrid(cgrid, column, value, "X");
	                break;

	            case GridTableModel.Y_INDEX:
	                updateCartesianGrid(cgrid, column, value, "Y");
	                break;

	            case GridTableModel.Z_INDEX:
	                updateCartesianGrid(cgrid, column, value, "Z");
	                break;

	            case GridTableModel.THETA_INDEX:
	                if (column == GridTableModel.NUM_INDEX) {
	                    sgrid.setNumTheta(parseInteger(value));
	                }
	                break;

	            case GridTableModel.PHI_INDEX:
	                if (column == GridTableModel.NUM_INDEX) {
	                    sgrid.setNumPhi(parseInteger(value));
	                }
	                break;

	            default:
	                throw new IllegalArgumentException("Unsupported row index: " + row);
	        }

	        // Notify the table of the update
	        fireTableCellUpdated(row, column);

	    } catch (NumberFormatException e) {
	        System.err.printf("Invalid value '%s' for row %d, column %d: %s%n", value, row, column, e.getMessage());
	    }
	}

	/**
	 * Updates the CartesianGrid based on the specified column and value.
	 *
	 * @param cgrid The CartesianGrid to update.
	 * @param column The column being updated.
	 * @param value The new value to set.
	 * @param axis The axis name (e.g., "X", "Y", or "Z").
	 */
	private void updateCartesianGrid(CartesianGrid cgrid, int column, Object value, String axis) {
	    double parsedValue;

	    switch (column) {
	        case GridTableModel.MIN_INDEX:
	            parsedValue = parseDouble(value);
	            setCartesianMin(cgrid, axis, parsedValue);
	            break;

	        case GridTableModel.MAX_INDEX:
	            parsedValue = parseDouble(value);
	            setCartesianMax(cgrid, axis, parsedValue);
	            break;

	        case GridTableModel.NUM_INDEX:
	            int parsedNum = parseInteger(value);
	            setCartesianNum(cgrid, axis, parsedNum);
	            break;

	        default:
	            throw new IllegalArgumentException("Unsupported column index: " + column);
	    }
	}

	/**
	 * Parses a value into a double.
	 *
	 * @param value The value to parse.
	 * @return The parsed double value.
	 * @throws NumberFormatException If the value is not a valid double.
	 */
	private double parseDouble(Object value) {
	    return Double.parseDouble(value.toString());
	}

	/**
	 * Parses a value into an integer.
	 *
	 * @param value The value to parse.
	 * @return The parsed integer value.
	 * @throws NumberFormatException If the value is not a valid integer.
	 */
	private int parseInteger(Object value) {
	    return Integer.parseInt(value.toString());
	}

	/**
	 * Sets the minimum value for the specified axis in the CartesianGrid.
	 *
	 * @param cgrid The CartesianGrid to update.
	 * @param axis The axis name (e.g., "X", "Y", or "Z").
	 * @param value The value to set.
	 */
	private void setCartesianMin(CartesianGrid cgrid, String axis, double value) {
	    switch (axis) {
	        case "X":
	            cgrid.setXMin(value);
	            break;
	        case "Y":
	            cgrid.setYMin(value);
	            break;
	        case "Z":
	            cgrid.setZMin(value);
	            break;
	        default:
	            throw new IllegalArgumentException("Unsupported axis: " + axis);
	    }
	}

	/**
	 * Sets the maximum value for the specified axis in the CartesianGrid.
	 *
	 * @param cgrid The CartesianGrid to update.
	 * @param axis The axis name (e.g., "X", "Y", or "Z").
	 * @param value The value to set.
	 */
	private void setCartesianMax(CartesianGrid cgrid, String axis, double value) {
	    switch (axis) {
	        case "X":
	            cgrid.setXMax(value);
	            break;
	        case "Y":
	            cgrid.setYMax(value);
	            break;
	        case "Z":
	            cgrid.setZMax(value);
	            break;
	        default:
	            throw new IllegalArgumentException("Unsupported axis: " + axis);
	    }
	}

	/**
	 * Sets the number of divisions for the specified axis in the CartesianGrid.
	 *
	 * @param cgrid The CartesianGrid to update.
	 * @param axis The axis name (e.g., "X", "Y", or "Z").
	 * @param value The value to set.
	 */
	private void setCartesianNum(CartesianGrid cgrid, String axis, int value) {
	    switch (axis) {
	        case "X":
	            cgrid.setNumX(value);
	            break;
	        case "Y":
	            cgrid.setNumY(value);
	            break;
	        case "Z":
	            cgrid.setNumZ(value);
	            break;
	        default:
	            throw new IllegalArgumentException("Unsupported axis: " + axis);
	    }
	}


	@Override
	public boolean isCellEditable(int row, int column) {

		switch (row) {
		case X_INDEX:
			return (column > NAME_INDEX) && (column < DEL_INDEX);
		case Y_INDEX:
			return (column > NAME_INDEX) && (column < DEL_INDEX);
		case Z_INDEX:
			return (column > NAME_INDEX) && (column < DEL_INDEX);
		case THETA_INDEX:
			return column == NUM_INDEX;
		case PHI_INDEX:
			return column == NUM_INDEX;

		}

		return false;
	}

}
