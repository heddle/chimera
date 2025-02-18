package cnuphys.chimera.grid.mapping;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.chimera.frame.Mosaic;
import cnuphys.chimera.grid.MosaicGrid;
import cnuphys.chimera.grid.Grid1D;
import cnuphys.chimera.grid.SphericalGrid;

public class MercatorProjection implements IMapProjection {

    private static final double MAX_LAT = Math.toRadians(89); // Avoid poles (85° limit)
    
    @Override
    public void latLonToXY(Point2D.Double latLon, Point2D.Double xy) {
        double lon = latLon.x; // Longitude in radians
        double lat = latLon.y; // Latitude in radians

        // Clamp latitude to avoid undefined values at poles
        lat = Math.max(-MAX_LAT, Math.min(MAX_LAT, lat));

        // Mercator projection formula
        xy.x = lon;
        xy.y = Math.log(Math.tan((Math.PI / 4) + (lat / 2)));
    }

    @Override
    public void latLonFromXY(Point2D.Double latLon, Point2D.Double xy) {
        double x = xy.x;
        double y = xy.y;

        // Inverse Mercator projection formula
        latLon.x = x; // Longitude
        latLon.y = 2 * Math.atan(Math.exp(y)) - Math.PI / 2; // Latitude
    }

    @Override
    public void drawMapOutline(Graphics g, IContainer container) {
		Graphics2D g2 = (Graphics2D) g;
		MosaicGrid grid = Mosaic.getInstance().getMosaicGrid();
		SphericalGrid sgrid = grid.getSphericalGrid();
		Grid1D thetaGrid = sgrid.getThetaGrid();
		Grid1D phiGrid = sgrid.getPhiGrid();

		// Define ranges and step sizes for sampling
		int numLat = sgrid.getNumTheta(); // Number of latitude samples
		int numLon = sgrid.getNumPhi(); // Number of longitude samples

		drawBoundary(g, container, Color.black);

		for (int i = 0; i < numLat; i++) {
			double theta = thetaGrid.valueAt(i);
			double lat = Math.PI / 2 - theta;
			drawLatitudeLine(g2, container, lat);
		}

		for (int i = 0; i < numLon; i++) {
			double lon = phiGrid.valueAt(i);
			drawLongitudeLine(g2, container, lon);
		}


    }
    
	//draw the overall boundary of the map
    protected void drawBoundary(Graphics g, IContainer container, Color lc) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(lc);
    	drawLatitudeLine(g2, container, MAX_LAT);
     	drawLatitudeLine(g2, container, -MAX_LAT);
    }

    @Override
    public boolean isPointOnMap(Point2D.Double xy) {
        // Check if the point's Mercator latitude is within valid range
        double lat = 2 * Math.atan(Math.exp(xy.y)) - Math.PI / 2;
        return (lat >= -MAX_LAT && lat <= MAX_LAT && xy.x >= -Math.PI && xy.x <= Math.PI);
    }

    @Override
    public void drawLatitudeLine(Graphics2D g2, IContainer container, double latitude) {
        if (Math.abs(latitude) > MAX_LAT) {
            return; // Skip drawing lines outside visible range
        }
        
        
        Point2D.Double latLon = new Point2D.Double();
        Point2D.Double xy = new Point2D.Double();;  
        Point p0 = new Point();
        Point p1 = new Point();
        
        latLon.setLocation(-Math.PI, latitude);
        latLonToXY(latLon, xy);
        container.worldToLocal(p0, xy);
        
        latLon.setLocation(Math.PI, latitude);
        latLonToXY(latLon, xy);
        container.worldToLocal(p1, xy);
        
        
        // Draw the latitude line
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(p0.x, p0.y, p1.x, p1.y);
    }

    @Override
    public void drawLongitudeLine(Graphics2D g2, IContainer container, double longitude) {
        Point2D.Double latLon = new Point2D.Double();
        Point2D.Double xy = new Point2D.Double();;  
        Point p0 = new Point();
        Point p1 = new Point();
        
        latLon.setLocation(longitude, MAX_LAT);
        latLonToXY(latLon, xy);
        container.worldToLocal(p0, xy);
        
        latLon.setLocation(longitude, -MAX_LAT);
        latLonToXY(latLon, xy);
        container.worldToLocal(p1, xy);

        // Draw the longitude line
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(p0.x, p0.y, p1.x, p1.y);
    }

    @Override
    public boolean isPointVisible(Point2D.Double latLon) {
        double lat = latLon.y;
        return lat >= -MAX_LAT && lat <= MAX_LAT;
    }

    @Override
    public EProjection getProjection() {
        return EProjection.MERCATOR;
    }

    @Override
    public String name() {
        return getProjection().getName();
    }
}
