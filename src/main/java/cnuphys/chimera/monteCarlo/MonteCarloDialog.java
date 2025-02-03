package cnuphys.chimera.monteCarlo;

import java.awt.Frame;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.chimera.dialog.LabeledTextField;
import cnuphys.chimera.dialog.VerticalPanel;
import cnuphys.chimera.grid.ChimeraGrid;

public class MonteCarloDialog extends SimpleDialog {

	private static final String RUN = "Run";

    //the overall grid
    private ChimeraGrid grid;

    //the clear check box
    private JCheckBox _clearCheckBox;

    //the number of points to generate
    private LabeledTextField _nPointsField;

    //the progress bar
    private JProgressBar _progressBar;


    public MonteCarloDialog(Frame owner, ChimeraGrid grid) {
        super("Monte Carlo", false, RUN, "Cancel"); // Modeless dialog
        this.grid = grid;

        pack();
        setLocationRelativeTo(null);
    }


    @Override
    protected JComponent createCenterComponent() {
    	VerticalPanel vp = new VerticalPanel();
    	vp.addItem(new JLabel("Monte Carlo Parameters"));

    	//the clear data checkbox
    	_clearCheckBox = new JCheckBox("Clear existing data");
    	_clearCheckBox.setSelected(true);
    	vp.addItem(_clearCheckBox);

    	//the number of points to generate
    	_nPointsField = new LabeledTextField("Number of points", 2000000, null, true, 6, 0, 0);
    	vp.addItem(_nPointsField);

		_progressBar = new JProgressBar(0, 100);
		_progressBar.setStringPainted(true);
		vp.addItem(_progressBar);

    	return vp;
    }

	@Override
	public void handleCommand(String command) {
		reason = command;
		if (command.equals(RUN)) {
	    	MonteCarlo.runMonteCarlo(getNPoints(), clearExistingData(), _progressBar);
		}
		else {
			System.err.println("Hit Cancel");
			setVisible(false);
		}
	}


    /**
     * Get the clear existing data flag
     * @return <code>true</code> if existing data should be cleared.
     */
	public boolean clearExistingData() {
		return _clearCheckBox.isSelected();
	}

	/**
	 * Get the number of points to generate
	 *
	 * @return the number of points to generate
	 */
	public int getNPoints() {
		String text = _nPointsField.getText();
		if (text == null) {
			return 0;
		}
		text = text.trim();
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			System.err.println("Monte Carlo dialog: bad number format: " + text);
			return 0;
		}
	}

}
