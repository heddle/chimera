package cnuphys.chimera.monteCarlo;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import cnuphys.chimera.frame.Chimera;
import cnuphys.chimera.grid.Fiveplet;

public class MonteCarlo {

	/**
	 * Run a Monte Carlo simulation with the given number of points
	 *
	 * @param numPoints the number of points to generate
	 * @param clear     if true, clear the existing points
	 * @param progressBar the progress bar to update
	 */

	public static void runMonteCarlo(int numPoints, boolean clear, JProgressBar progressBar) {
		// Disable the button while running
		progressBar.setValue(0);
		List<MonteCarloPoint> points = Chimera.getInstance().getMonteCarloPoints();
		HashSet<Fiveplet> seenTuples = Chimera.getInstance().getMonteCarloSeenSet();
		if (clear) {
			points.clear();
		}

		// SwingWorker to handle background processing
		SwingWorker<List<MonteCarloPoint>, Integer> worker = new SwingWorker<>() {
			@Override
			protected List<MonteCarloPoint> doInBackground() {

				for (int i = 0; i < numPoints; i++) {
					MonteCarloPoint point = new MonteCarloPoint();
					points.add(point);

                    // Add the tuple to the seen set
					seenTuples.add(point.fiveplet);

					// Publish progress
					if (i % (numPoints / 100) == 0) { // Update progress every 1%
						publish((i * 100) / numPoints);
					}
				}
				return points;
			}

			@Override
			protected void process(List<Integer> chunks) {
				// Update progress bar on EDT
				for (int progress : chunks) {
					progressBar.setValue(progress);
				}
			}

			@Override
			protected void done() {
					try {
					List<MonteCarloPoint> points = get(); // Get the result
					JOptionPane.showMessageDialog(Chimera.getInstance(), "Simulation complete with " + points.size() + " points.",
							"Done", JOptionPane.INFORMATION_MESSAGE);
					Chimera.refresh();
				} catch (InterruptedException | ExecutionException e) {
					JOptionPane.showMessageDialog(Chimera.getInstance(), "Error: " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		// Start the SwingWorker
		worker.execute();
	}

}
