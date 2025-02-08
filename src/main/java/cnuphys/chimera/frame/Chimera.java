package cnuphys.chimera.frame;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.chimera.grid.ChimeraGrid;
import cnuphys.chimera.grid.Fiveplet;
import cnuphys.chimera.grid.TestGrid;
import cnuphys.chimera.monteCarlo.MonteCarloDialog;
import cnuphys.chimera.monteCarlo.MonteCarloPoint;

@SuppressWarnings("serial")
public class Chimera extends BaseMDIApplication {


	//the singleton
	private static Chimera _instance;


	// chimera release
	private static final String RELEASE = "0.1";

	private MonteCarloDialog _monteCarloDialog;

	// the grid
	private ChimeraGrid _chimeraGrid;

	//current Montecarlo points
	private List<MonteCarloPoint> _points = new ArrayList<>();

	 // HashSet to store unique 5-plets
    private final HashSet<Fiveplet> _seenTuples = new HashSet<>();

	//2D MC view
	private MonteCarloView2D _mc2DView;


	/**
	 * Constructor (private--used to create singleton)
	 *
	 * @param keyVals an optional variable length list of attributes in type-value
	 *                pairs. For example, PropertySupport.NAME, "my application",
	 *                PropertySupport.CENTER, true, etc.
	 */
	private Chimera(Object... keyVals) {
		super(keyVals);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};
		addWindowListener(windowAdapter);

		createInitialGrid();
	}

	//create the initial (default) grid
	private void createInitialGrid() {
		_chimeraGrid = TestGrid.primaryTestGrid();
	}

	public double getRadius() {
		return _chimeraGrid.getSphericalGrid().getRadius();
	}

	/**
	 * Get the singleton instance of the Chimera class
	 *
	 * @return the singleton instance
	 */
	public static Chimera getInstance() {
		if (_instance == null) {
			_instance = new Chimera(PropertySupport.TITLE, "chimera " + RELEASE, PropertySupport.BACKGROUNDIMAGE,
					"images/cnu.png", PropertySupport.FRACTION, 0.9);

			_instance.addInitialViews();
			_instance.createMenus();

       }
        return _instance;
    }

	/**
	 * Get the Chimera grid
	 *
	 * @return the Chimera grid
	 */
	public ChimeraGrid getChimeraGrid() {
		return _chimeraGrid;
	}

	/**
	 * Add the initial views to the desktop.
	 */
	private void addInitialViews() {
		_mc2DView = new MonteCarloView2D();
	}

	/**
	 * Get the current monte carlo points
	 * @return the current monte carlo points
	 */
	public List<MonteCarloPoint> getMonteCarloPoints() {
        return _points;
    }

	/**
	 * Get the current monte carlo patch counts
	 * @return the current monte carlo patch counts
	 */
	public HashSet<Fiveplet> getMonteCarloSeenSet() {
		return _seenTuples;
	}

	/**
	 * Add items to existing menus and/or create new menus NOTE: Swim menu is
	 * created by the SwimManager
	 */
	private void createMenus() {
		MenuManager mmgr = MenuManager.getInstance();
		// the options menu
		addToOptionMenu(mmgr.getOptionMenu());

		JMenu menu = new JMenu("Intersections");

		JMenuItem item = new JMenuItem("Find Intersecting Cells");
		item.addActionListener(e -> handleIntersectingCells());
		menu.add(item);
		getJMenuBar().add(menu);

	}

	//handle intersecting cells
	private void handleIntersectingCells() {
		_chimeraGrid.findIntersectingCells();

	}

	// add to the options menu
	private void addToOptionMenu(JMenu omenu) {

	    JMenuItem monteCarloItem = new JMenuItem("Monte Carlo...");
	    monteCarloItem.addActionListener(e -> handleMonteCarloDialog());

	    omenu.add(monteCarloItem);
	}

	//handle selection of the monte carlo dialog
	private void handleMonteCarloDialog() {
		if (_monteCarloDialog == null) {
			_monteCarloDialog = new MonteCarloDialog(this, _chimeraGrid);
		}
		_monteCarloDialog.setVisible(true);
		_monteCarloDialog.toFront();
	}


	/**
	 * Refresh all views (with containers)
	 */
	public static void refresh() {

		if (SwingUtilities.isEventDispatchThread()) {
			refreshAllViews();
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						refreshAllViews();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	private static void refreshAllViews() {
	    for (JInternalFrame frame : Desktop.getInstance().getAllFrames()) {
	        frame.repaint();
	    }
	}


	/**
	 * The main program
	 *
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		Environment.setLookAndFeel();
		FileUtilities.setDefaultDir("data");

		// now make the frame visible, in the AWT thread
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				getInstance();
				getInstance().setVisible(true);
				System.out.println("chimera %s is ready.");
			}

		});
	}

}
