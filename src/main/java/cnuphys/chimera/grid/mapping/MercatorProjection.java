package cnuphys.chimera.grid.mapping;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;

public class MercatorProjection implements IMapProjection {

    private static final double R = 1.0; // Radius of the sphere (normalized)
    private static final double MAX_LAT = Math.toRadians(85); // Avoid poles, latitude limit (~85°)

    @Override
    public void latLonToXY(Point2D.Double latLon, Point2D.Double xy) {
        double lon = latLon.x; // Longitude in radians
        double lat = latLon.y; // Latitude in radians

        // Clamp latitude to avoid infinity near poles
        lat = Math.max(-MAX_LAT, Math.min(MAX_LAT, lat));

        // Mercator projection formula
        xy.x = R * lon;
        xy.y = R * Math.log(Math.tan(Math.PI / 4 + lat / 2));
    }

    @Override
    public void latLonFromXY(Point2D.Double latLon, Point2D.Double xy) {
        double x = xy.x;
        double y = xy.y;

        // Inverse Mercator projection formula
        latLon.x = x / R; // Longitude
        latLon.y = 2 * Math.atan(Math.exp(y / R)) - Math.PI / 2; // Latitude
    }

    @Override
    public void drawMapOutline(Graphics g, IContainer container) {
        Graphics2D g2 = (Graphics2D) g;
//        int width = container.getWidth();
//        int height = container.getHeight();
//        int centerX = width / 2;
//        int centerY = height / 2;
//
//        // Draw the outline of the map as a rectangle
//        g2.setColor(Color.BLACK);
//        g2.drawRect(0, centerY - height / 2, width, height);
    }

    @Override
    public boolean isPointOnMap(Point2D.Double xy) {
        // Check if the point is within the latitude limits of the Mercator projection
        double lat = 2 * Math.atan(Math.exp(xy.y / R)) - Math.PI / 2;
        return lat <= MAX_LAT && lat >= -MAX_LAT;
    }

    @Override
    public void drawLatitudeLine(Graphics2D g2, IContainer container, double latitude) {
        if (Math.abs(latitude) > MAX_LAT) {
            return; // Skip drawing lines outside visible range
        }

//        int width = container.getWidth();
//        int height = container.getHeight();
//        int centerY = height / 2;
//
//        // Compute the y-coordinate for the latitude line
//        double y = R * Math.log(Math.tan(Math.PI / 4 + latitude / 2));
//
//        // Map Mercator y-coordinate to screen y-coordinate
//        int screenY = centerY - (int) (y * (height / (2 * Math.log(Math.tan(Math.PI / 4 + MAX_LAT / 2)))));
//
//        // Draw the latitude line
//        g2.setColor(Color.LIGHT_GRAY);
//        g2.drawLine(0, screenY, width, screenY);
    }

    @Override
    public void drawLongitudeLine(Graphics2D g2, IContainer container, double longitude) {
//        int width = container.getWidth();
//        int height = container.getHeight();
//        int centerX = width / 2;
//
//        // Compute the x-coordinate for the longitude line
//        double x = R * longitude;
//
//        // Map Mercator x-coordinate to screen x-coordinate
//        int screenX = centerX + (int) (x * (width / (2 * Math.PI)));
//
//        // Draw the longitude line
//        g2.setColor(Color.LIGHT_GRAY);
//        g2.drawLine(screenX, 0, screenX, height);
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
