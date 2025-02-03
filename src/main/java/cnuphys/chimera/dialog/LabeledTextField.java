package cnuphys.chimera.dialog;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LabeledTextField extends JPanel {
    private final JLabel label;
    private final JTextField textField;
    private final JLabel unitsLabel;
    private final int option;

    public interface Callback {
        void callback(String text);
    }

    private Callback focusLostCallback;
    private Callback enterKeyCallback;

    /**
     * Create a labeled text field with the given label and units
     * @param label the label for the text field
     * @param initVal initial value for the text field can be null
     * @param units units label for the text field can be null
     * @param editable usual flag for editable text field
     * @param width width of the text field in characters
     * @param fontSize font size for label and text field
     * @param option 0 for integer, 1 for double, 2 for string
     */
    public LabeledTextField(String label, Object initVal, String units, boolean editable, int width, int fontSize, int option) {
        this.option = option;

        // Set layout
        setLayout(new FlowLayout(FlowLayout.LEFT));


        // Initialize components
        this.label = new JLabel(label);

        Font font = (fontSize < 1) ? this.getFont() : new Font("Arial", Font.PLAIN, fontSize);

        this.label.setFont(font);

        String initStr = (initVal != null) ? initVal.toString() : "";
        this.textField = new JTextField(initStr, width);
        this.textField.setEditable(editable);
        this.textField.setFont(font);

        this.unitsLabel = (units != null && !units.isEmpty()) ? new JLabel(units) : null;
        if (this.unitsLabel != null) {
            this.unitsLabel.setFont(font);
        }

        // Add components to the panel
        add(this.label);
        add(this.textField);
        if (this.unitsLabel != null) {
            add(this.unitsLabel);
        }

        // Add listeners
        this.textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (focusLostCallback != null) {
                    focusLostCallback.callback(validateInput(textField.getText()));
                }
            }
        });

        this.textField.addActionListener(e -> {
            if (enterKeyCallback != null) {
                enterKeyCallback.callback(validateInput(textField.getText()));
            }
        });
    }

    // Set focus lost callback
    public void setFocusLostCallback(Callback callback) {
        this.focusLostCallback = callback;
    }

    // Set enter key callback
    public void setEnterKeyCallback(Callback callback) {
        this.enterKeyCallback = callback;
    }

    // Validate and process input based on option
    private String validateInput(String input) {
        try {
            switch (option) {
                case 0: // Integer
                    Integer.parseInt(input);
                    break;
                case 1: // Double
                    Double.parseDouble(input);
                    break;
                case 2: // String
                    // No validation needed
                    break;
                default:
                    throw new IllegalArgumentException("Invalid option: " + option);
            }
            return input; // Valid input
        } catch (NumberFormatException e) {
            return ""; // Return empty string for invalid input
        }
    }

    // Get text from the text field
    public String getText() {
        return textField.getText();
    }

    // Set text in the text field
    public void setText(String text) {
        textField.setText(text);
    }

	public double getDoubleValue() {
		try {
			return Double.parseDouble(textField.getText());
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}


}
