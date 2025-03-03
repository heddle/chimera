package cnuphys.mosaic.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class OptionPanel extends JPanel {

    private JCheckBox[] checkBoxes;
    private OptionPanelCallback callback;

    public OptionPanel(String[] labels, boolean[] selected) {
        // Set the layout to vertical BoxLayout.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        checkBoxes = new JCheckBox[labels.length];

        // Create and add each checkbox.
        for (int i = 0; i < labels.length; i++) {
            checkBoxes[i] = new JCheckBox(labels[i], selected[i]);
            // Left-align the checkbox.
            checkBoxes[i].setAlignmentX(LEFT_ALIGNMENT);
            // Add an action listener to call the callback when toggled.
            checkBoxes[i].addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent e) {
                    if (callback != null) {
                        callback.onToggle();
                    }
                }
            });
            add(checkBoxes[i]);
        }
    }

    // Returns true if the nth checkbox is selected.
    protected boolean isSelected(int n) {
        if (n >= 0 && n < checkBoxes.length) {
            return checkBoxes[n].isSelected();
        }
        throw new IndexOutOfBoundsException("Checkbox index out of range: " + n);
    }

    // Set the public callback to be invoked when any checkbox is toggled.
    public void setCallback(OptionPanelCallback callback) {
        this.callback = callback;
    }

    // Define the callback interface.
    public interface OptionPanelCallback {
        void onToggle();
    }
}
