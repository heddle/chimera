package cnuphys.chimera.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class VerticalPanel extends JPanel {

	public VerticalPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(Component.CENTER_ALIGNMENT);
	}

	/**
	 * Add a component to the panel using the component's preferred height
	 *
	 * @param component the component to add
	 */
	public void addItem(JComponent component) {
		addItem(component, -1);
	}


	/**
	 * Add a component to the panel
	 *
	 * @param component the component to add
	 * @param height    the height of the component
	 */
	   public void addItem(JComponent component, int height) {

		if (height < 1) {
			height = component.getPreferredSize().height + 2;
		}
		// Create a component (e.g., JLabel or JButton)
		Dimension d = component.getPreferredSize();
		component.setPreferredSize(new Dimension(d.width, height)); // Set varying heights
		component.setMaximumSize(new Dimension(d.width, height)); // Allow resizing in width

		// Align the component to the center
		component.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Add padding around each item
		add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing
		add(component);
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 4, def.left + 4, def.bottom + 4, def.right + 4);
	}

}
