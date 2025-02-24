package cnuphys.chimera.graphics;

import java.awt.Component;

import bCNU3D.Panel3D;
import cnuphys.bCNU.dialog.SimpleDialog;

public class Cell3DSupport  {
	

    public static SimpleDialog cellDialog(final String title, final Panel3D panel3D) {
    	SimpleDialog oneCellDialog = new SimpleDialog(title, false, "Close") {
            @Override
            public Component createCenterComponent() {
                return panel3D;
            }
            
        };
        
        return oneCellDialog;
    }
    
    
  

}
