package cnuphys.chimera.util;

import bCNU3D.Panel3D;

public class PanelKeys {

	public static void addKeyListener(Panel3D panel3D, float delX, float delY, float delZ) {
		// Ensure the GLJPanel can receive focus
		panel3D.setFocusable(true);
		panel3D.requestFocusInWindow();
	// Ensure the GLJPanel can receive focus
		panel3D.setFocusable(true);
		panel3D.requestFocusInWindow();

	// Add a key listener for keyboard navigation:
		panel3D.addKeyListener(new java.awt.event.KeyAdapter() {
	    @Override
	    public void keyPressed(java.awt.event.KeyEvent e) {
	        int key = e.getKeyCode();
	        float zoomStep = 10f;   // adjust for zoom in/out

	        switch(key) {
	            case java.awt.event.KeyEvent.VK_LEFT:
	                // Move left: decrease x translation
	            	panel3D.deltaX(-delX);
	                break;
	            case java.awt.event.KeyEvent.VK_RIGHT:
	                // Move right: increase x translation
	            	panel3D.deltaX(delX);
	                break;
	            case java.awt.event.KeyEvent.VK_UP:
	                // Move up: increase y translation
	            	panel3D.deltaY(delY);
	                break;
	            case java.awt.event.KeyEvent.VK_DOWN:
	                // Move down: decrease y translation
	            	panel3D.deltaY(-delY);
	                break;
	            default:
	                // For zoom keys, we check the key character
	                char keyChar = e.getKeyChar();
	                if (keyChar == 'j') {
	                    // Zoom in (for example, move the camera closer)
	                	panel3D.deltaZ(delZ);
	                } else if (keyChar == 'k') {
	                    // Zoom out (move the camera further away)
	                	panel3D.deltaZ(-delZ);
	                } else if (keyChar == 'Z' && e.isShiftDown()) {
	                    // Alternatively, with Shift+Z, you might want to change scale
	                    // Here we increase the scaling factors for zooming in.
	                	panel3D._xscale += 0.1f;
	                	panel3D._yscale += 0.1f;
	                	panel3D._zscale += 0.1f;
	                }
	                break;
	        }
	        // Refresh the panel to show the changes
	        panel3D.refresh();
	    }
	});
	}

}
