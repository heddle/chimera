package cnuphys.chimera.grid.mapping;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.chimera.frame.Chimera;
import cnuphys.chimera.grid.ChimeraGrid;
import cnuphys.chimera.grid.SphericalGrid;

public class MercatorProjection implements IMapProjection {

    private double radius;
    private static final double MAX_LAT = Math.toRadians(85); // Avoid poles (85° limit)
    
    public MercatorProjection(double radius) {
        this.radius = radius;
    }

    @Override
    public void latLonToXY(Point2D.Double latLon, Point2D.Double xy) {
        double lon = latLon.x; // Longitude in radians
        double lat = latLon.y; // Latitude in radians

        // Clamp latitude to avoid undefined values at poles
        lat = Math.max(-MAX_LAT, Math.min(MAX_LAT, lat));

        // Mercator projection formula
        xy.x = radius * lon;
        xy.y = radius * Math.log(Math.tan((Math.PI / 4) + (lat / 2)));
    }

    @Override
    public void latLonFromXY(Point2D.Double latLon, Point2D.Double xy) {
        double x = xy.x;
        double y = xy.y;

        // Inverse Mercator projection formula
        latLon.x = x / radius; // Longitude
        latLon.y = 2 * Math.atan(Math.exp(y / radius)) - Math.PI / 2; // Latitude
    }

    @Override
    public void drawMapOutline(Graphics g, IContainer container) {
		Graphics2D g2 = (Graphics2D) g;
		ChimeraGrid grid = Chimera.getInstance().getChimeraGrid();
		SphericalGrid sgrid = grid.getSphericalGrid();

		// Define ranges and step sizes for sampling
		double latStep = sgrid.getThetaDel(); // Step size for latitude (radians)
		double lonStep = sgrid.getPhiDel(); // Step size for longitude (radians)
		int numLat = sgrid.getNumTheta(); // Number of latitude samples
		int numLon = sgrid.getNumPhi(); // Number of longitude samples

		drawBoundary(g, container, Color.black);

		for (int i = 0; i < numLat; i++) {
			double lat = Math.PI / 2 - i * latStep;
			drawLatitudeLine(g2, container, lat);
		}

		for (int i = 0; i < numLon; i++) {
			double lon = -Math.PI + i * lonStep;
			drawLongitudeLine(g2, container, lon);
		}


    }
    
	//draw the overall boundary of the map
    protected void drawBoundary(Graphics g, IContainer container, Color lc) {
    }

    @Override
    public boolean isPointOnMap(Point2D.Double xy) {
        // Check if the point's Mercator latitude is within valid range
        double lat = 2 * Math.atan(Math.exp(xy.y / radius)) - Math.PI / 2;
        return (lat >= -MAX_LAT && lat <= MAX_LAT);
    }

    @Override
    public void drawLatitudeLine(Graphics2D g2, IContainer container, double latitude) {
        if (Math.abs(latitude) > MAX_LAT) {
            return; // Skip drawing lines outside visible range
        }

        int width = container.getComponent().getWidth();
        int height = container.getComponent().getHeight();
        int centerY = height / 2;

        // Compute the y-coordinate for the latitude line in Mercator projection
        double projectedY = radius * Math.log(Math.tan(Math.PI / 4 + latitude / 2));

        // Map Mercator y-coordinate to screen y-coordinate
        int screenY = centerY - (int) (projectedY * (height / (2 * radius * Math.log(Math.tan(Math.PI / 4 + MAX_LAT / 2)))));

        // Draw the latitude line
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(0, screenY, width, screenY);
    }

    @Override
    public void drawLongitudeLine(Graphics2D g2, IContainer container, double longitude) {
        int width = container.getComponent().getWidth();
        int height = container.getComponent().getHeight();
        int centerX = width / 2;

        // Compute the x-coordinate for the longitude line
        double projectedX = radius * longitude;

        // Map Mercator x-coordinate to screen x-coordinate
        int screenX = centerX + (int) (projectedX * (width / (2 * Math.PI * radius)));

        // Draw the longitude line
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(screenX, 0, screenX, height);
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
