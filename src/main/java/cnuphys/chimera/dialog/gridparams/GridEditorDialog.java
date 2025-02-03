package cnuphys.chimera.dialog.gridparams;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

import cnuphys.chimera.dialog.LabeledTextField;
import cnuphys.chimera.dialog.VerticalPanel;
import cnuphys.chimera.frame.Chimera;
import cnuphys.chimera.grid.CartesianGrid;
import cnuphys.chimera.grid.ChimeraGrid;
import cnuphys.chimera.grid.IGridChangeListener;
import cnuphys.chimera.grid.SphericalGrid;
import cnuphys.bCNU.dialog.SimpleDialog;

public class GridEditorDialog extends SimpleDialog {
	/** The small theta character */
	public static final String SMALL_ALPHA = "\u03B1";

	/** The small phi character */
    public static final String SMALL_BETA = "\u03B2";


    private CartesianGrid cartesianGridCopy;
    private SphericalGrid sphericalGridCopy;

	// List of grid change listeners
	private EventListenerList _listenerList;

    //the overall grid
    private ChimeraGrid grid;

    //the grid copy
    private ChimeraGrid gridCopy;

    //the grid param table
    private GridTable _table;

	private LabeledTextField _xotf;
	private LabeledTextField _yotf;
	private LabeledTextField _zotf;
	private LabeledTextField _alphatf;
	private LabeledTextField _betatf;



    /**
     * A dialog for editing grid parameters
     * @param owner the owner frame
     * @param grid the grid to edit
     */
    public GridEditorDialog(Frame owner, ChimeraGrid grid) {
        super("Grid Parameters", false, "OK", "Cancel"); // Modeless dialog
        this.grid = grid;
        reset();

        pack();
        setLocationRelativeTo(null);
    }

    //makes a fresh copy of the grid for editing
    private void reset() {
        //edit copies
        this.cartesianGridCopy = new CartesianGrid(grid.getCartesianGrid()); // Make a copy
        this.sphericalGridCopy = new SphericalGrid(grid.getSphericalGrid()); // Make a copy);
        gridCopy = new ChimeraGrid(cartesianGridCopy, sphericalGridCopy);

    }

    @Override
	protected JComponent createNorthComponent() {
		return new JLabel(" Distances are in radii, angles in degrees.");
	}

	@Override
	protected JComponent createCenterComponent() {

		JPanel panel = new JPanel() {
			@Override
			public Insets getInsets() {
				Insets def = super.getInsets();
				return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
			}

		};

		panel.setLayout(new BorderLayout(6, 6));

		_table = new GridTable();
		panel.add(new JScrollPane(_table), BorderLayout.CENTER);

		JPanel vPanel = new JPanel();
		vPanel.setLayout(new BorderLayout(4, 4));
		vPanel.add(panel, BorderLayout.CENTER);

		vPanel.add(createParameterPanel(), BorderLayout.SOUTH);
		return vPanel;
	}

	private VerticalPanel createParameterPanel() {

		ChimeraGrid grid = Chimera.getInstance().getChimeraGrid();
		CartesianGrid cg = grid.getCartesianGrid();
		SphericalGrid sg = grid.getSphericalGrid();

		VerticalPanel panel = new VerticalPanel();
		_xotf = new LabeledTextField("Cartesian xo", cg.getXOffset(), "(units of sphere radii)", true, 8, -1, 2);
		panel.addItem(_xotf);
		_yotf = new LabeledTextField("Cartesian yo", cg.getYOffset(), "(units of sphere radii)", true, 8, -1, 2);
		panel.addItem(_yotf);
		_zotf = new LabeledTextField("Cartesian zo", cg.getZOffset(), "(units of sphere radii)", true, 8, -1, 2);
		panel.addItem(_zotf);
		_alphatf = new LabeledTextField(SMALL_ALPHA + " (polar rotation)", Math.toDegrees(sg.getAlpha()), "degrees", true, 8, -1, 2);
		panel.addItem(_alphatf);
		_betatf = new LabeledTextField(SMALL_BETA + " (azimuthal rotation)", Math.toDegrees(sg.getBeta()), "degrees", true, 8, -1, 2);
		panel.addItem(_betatf);
		return panel;

	}

    /**
     * Get the grid copy (being edited)
     * @return the grid copy
     */
	public ChimeraGrid getGridCopy() {
		return gridCopy;
	}

	//user hit OK
    private void handleOK() {
		System.err.println("Hit OK");
		// Update the grid


		cartesianGridCopy.setXOffset(_xotf.getDoubleValue());
		cartesianGridCopy.setYOffset(_yotf.getDoubleValue());
		cartesianGridCopy.setZOffset(_zotf.getDoubleValue());
		sphericalGridCopy.setAlpha(Math.toRadians(_alphatf.getDoubleValue()));
		sphericalGridCopy.setBeta(Math.toRadians(_betatf.getDoubleValue()));

		grid.setCartesianGrid(cartesianGridCopy);
		grid.setSphericalGrid(sphericalGridCopy);
		notifyListeners();
	}

	@Override
	public void handleCommand(String command) {
		reason = command;
		if (command.equals("OK")) {
			handleOK();
		}
		else {
			System.err.println("Hit Cancel");
			reset();
		}
		setVisible(false);
	}

	/**
	 * Add a grid change listener
	 *
	 * @param gridChangeListener the listener to add
	 */
	public void addGridChangeListener(IGridChangeListener gridChangeListener) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(IGridChangeListener.class, gridChangeListener);
		_listenerList.add(IGridChangeListener.class, gridChangeListener);

	}


	/**
	 * Notify all listeners that a change has occurred in the grid
	 */
	protected void notifyListeners() {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == IGridChangeListener.class) {
				IGridChangeListener listener = (IGridChangeListener) listeners[i + 1];
				listener.gridChanged();
			}

		}
	}


}

