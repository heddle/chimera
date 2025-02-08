package cnuphys.chimera.grid.mapping;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.chimera.frame.Chimera;
import cnuphys.chimera.grid.ChimeraGrid;
import cnuphys.chimera.grid.Grid1D;
import cnuphys.chimera.grid.SphericalGrid;

public class OrthographicProjection implements IMapProjection {

    private final double _radius; // Sphere radius
    private final double centerLon; // Central longitude of the map in radians
    private final double centerLat; // Central latitude of the map in radians

    private static final double MAXLAT = Math.toRadians(89.999); // Maximum latitude in radians
    private static final double MINLAT = -MAXLAT; // Minimum latitude in raduians


    private ArrayList<double[]> _lonRanges;
    private ArrayList<double[]> _latRanges;

	public OrthographicProjection(double radius) {
		this(radius, Math.toRadians(-15), Math.toRadians(10));
	}

    /**
     * Create a new orthographic projection with a given radius and center.
     * @param radius The radius of the sphere (usually = 1)
     * @param centerLon The central longitude of the map in radians
     * @param centerLat The central latitude of the map in radians
     */
    public OrthographicProjection(double radius, double centerLon, double centerLat) {
    	_radius = radius;
        this.centerLon = centerLon;
        this.centerLat = centerLat;

        _lonRanges = getVisibleLongitudeRange(centerLon);
        _latRanges = getVisibleLatitudeRange(centerLat);
    }

    @Override
    public void latLonToXY(Point2D.Double latLon, Point2D.Double xy) {
        double lon = latLon.x;
        double lat = latLon.y;

        // Convert latitude and longitude to radians
        double deltaLon = lon - centerLon;

        // Calculate projection coordinates
        double cosLat = Math.cos(lat);
        double sinLat = Math.sin(lat);
        double cosDeltaLon = Math.cos(deltaLon);

        double x = _radius * cosLat * Math.sin(deltaLon);
        double y = _radius * (Math.cos(centerLat) * sinLat - Math.sin(centerLat) * cosLat * cosDeltaLon);

        // Check if the point is visible (dot product with viewing direction > 0)
        double z = Math.sin(centerLat) * sinLat + Math.cos(centerLat) * cosLat * cosDeltaLon;
        if (z <= 0) {
            // Point is not visible
            xy.setLocation(Double.NaN, Double.NaN);
        } else {
            xy.setLocation(x, y);
        }
    }

    @Override
    public void latLonFromXY(Point2D.Double latLon, Point2D.Double xy) {
        double x = xy.x;
        double y = xy.y;

        // Compute z from x and y
        double zSquared = _radius * _radius - x * x - y * y;
        if (zSquared < 0) {
            // Point is outside the sphere
            latLon.setLocation(Double.NaN, Double.NaN);
            return;
        }

        double z = Math.sqrt(zSquared);

        // Convert back to latitude and longitude
        double lat = Math.asin(z * Math.sin(centerLat) + y * Math.cos(centerLat) / _radius);
        double lon = centerLon + Math.atan2(x, (z * Math.cos(centerLat) - y * Math.sin(centerLat)));

        latLon.setLocation(lon, lat);
    }

    @Override
    public void drawMapOutline(Graphics g, IContainer container) {
		Graphics2D g2 = (Graphics2D) g;
		ChimeraGrid grid = Chimera.getInstance().getChimeraGrid();
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

    @Override
    public boolean isPointOnMap(Point2D.Double xy) {
        double x = xy.x;
        double y = xy.y;

        // Check if the point lies within the circle of radius R
        return x * x + y * y <= _radius * _radius;
    }

    @Override
    public void drawLatitudeLine(Graphics2D g2, IContainer container, double latitude) {

		GeneralPath path = new GeneralPath();
		int numPoints = 50;
		Point2D.Double xy = new Point2D.Double();
		Point2D.Double latLon = new Point2D.Double();
		Point screenPoint = new Point();

		latLon.y = latitude;

		double step = 2*Math.PI / numPoints;
		for (int i = 0; i <= numPoints; i++) {
			double lon = -Math.PI + i * step;

			latLon.x = lon;

			if (!isPointVisible(latLon)) {
                continue;
			}

			latLonToXY(latLon, xy);
			container.worldToLocal(screenPoint, xy);

            if (path.getCurrentPoint() == null) {
                path.moveTo(screenPoint.x, screenPoint.y);
            } else {
                path.lineTo(screenPoint.x, screenPoint.y);
            }
		}

		boolean isEquator = Math.abs(latitude) < 1e-6;
		g2.setColor(isEquator ? Color.red : Color.black);
		g2.draw(path);
   }

    @Override
    public void drawLongitudeLine(Graphics2D g2, IContainer container, double longitude) {
		GeneralPath path = new GeneralPath();
		int numPoints = 50;
		Point2D.Double xy = new Point2D.Double();
		Point2D.Double latLon = new Point2D.Double();
		Point screenPoint = new Point();

		latLon.x = longitude;

		double step = Math.PI / numPoints;
		for (int i = 0; i <= numPoints; i++) {
			double lat = -Math.PI / 2 + i * step;
			lat = Math.max(MINLAT, Math.min(MAXLAT, lat));

			latLon.y = lat;
			if (!isPointVisible(latLon)) {
                continue;
			}

			latLonToXY(latLon, xy);
			container.worldToLocal(screenPoint, xy);

            if (path.getCurrentPoint() == null) {
                path.moveTo(screenPoint.x, screenPoint.y);
            } else {
                path.lineTo(screenPoint.x, screenPoint.y);
            }
		}

		boolean isPrimeMeridian = Math.abs(longitude) < 1e-6;
		g2.setColor(isPrimeMeridian ? Color.red : Color.black);
		g2.draw(path);
    }

    @Override
    public boolean isPointVisible(Point2D.Double latLon) {
        double lon = latLon.x;
        double lat = latLon.y;

        // Convert latitude and longitude to radians
        double deltaLon = lon - centerLon;

        // Check visibility using the dot product
        double cosLat = Math.cos(lat);
        double sinLat = Math.sin(lat);
        double cosDeltaLon = Math.cos(deltaLon);

        double z = Math.sin(centerLat) * sinLat + Math.cos(centerLat) * cosLat * cosDeltaLon;
        return z > 0; // True if point is on the visible hemisphere
    }

    /**
     * Get the range of visible longitudes for a given central longitude.
     *
     * @param centerLon The central longitude in radians ([-PI, PI])
     * @return A list of visible longitude ranges (each range is [minLon, maxLon])
     */
    public ArrayList<double[]> getVisibleLongitudeRange(double centerLon) {
        // Calculate min and max longitudes in radians
        double minLon = wrapLongitude(centerLon - Math.PI / 2);
        double maxLon = wrapLongitude(centerLon + Math.PI / 2);

        ArrayList<double[]> ranges = new ArrayList<>();

        if (minLon <= maxLon) {
            // The range does not cross the antimeridian
            ranges.add(new double[]{minLon, maxLon});
        } else {
            // The range crosses the antimeridian, split into two segments
            ranges.add(new double[]{minLon, Math.PI});
            ranges.add(new double[]{-Math.PI, maxLon});
        }

        return ranges;
    }

    /**
     * Get the range of visible latitudes for a given central latitude.
     *
     * @param centerLat The central latitude in radians ([-PI/2, PI/2])
     * @return A list of visible latitude ranges (each range is [minLat, maxLat])
     */
    public ArrayList<double[]> getVisibleLatitudeRange(double centerLat) {
    	// Calculate min and max latitudes directly
        double minLat = centerLat - Math.PI / 2;
        double maxLat = centerLat + Math.PI / 2;

        // Clip min and max latitudes to the valid range [-PI/2, PI/2]
        minLat = Math.max(minLat, -Math.PI / 2);
        maxLat = Math.min(maxLat, Math.PI / 2);

        // Return the valid range as a single segment
        ArrayList<double[]> ranges = new ArrayList<>();
        ranges.add(new double[]{minLat, maxLat});
        return ranges;    }

    /**
     * Check if a given longitude is visible within the specified ranges.
     *
     * @param lon    The longitude in radians ([-PI, PI])
     * @param ranges The list of visible longitude ranges (each range is [minLon, maxLon])
     * @return true if the longitude is within any of the ranges, false otherwise
     */
    public boolean isLonVisible(double lon, ArrayList<double[]> ranges) {
        // Normalize longitude to the range [-PI, PI]
        lon = wrapLongitude(lon);

        // Check if the longitude is within any of the ranges
        for (double[] range : ranges) {
            double minLon = range[0];
            double maxLon = range[1];

            if (minLon <= maxLon) {
                // Standard range
                if (lon >= minLon && lon <= maxLon) {
                    return true;
                }
            } else {
                // Range crosses the antimeridian
                if (lon >= minLon || lon <= maxLon) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isLatVisible(double lat, ArrayList<double[]> ranges) {
        // Normalize latitude to the range [-PI/2, PI/2]
        lat = wrapLatitude(lat);

        // Check if the latitude is within any of the ranges
        for (double[] range : ranges) {
            double minLat = range[0];
            double maxLat = range[1];

            if (minLat <= maxLat) {
                // Standard range
                if (lat >= minLat && lat <= maxLat) {
                    return true;
                }
            } else {
                // Range crosses the poles
                if (lat >= minLat || lat <= maxLat) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Wrap a longitude value to the range [-PI, PI].
     *
     * @param lon The longitude in radians
     * @return The wrapped longitude
     */
    public double wrapLongitude(double lon) {
        return lon - (2 * Math.PI) * Math.floor((lon + Math.PI) / (2 * Math.PI));
    }

    /**
     * Wrap a latitude value to the range [-PI/2, PI/2].
     *
     * @param lat The latitude in radians
     * @return The wrapped latitude
     */
    public double wrapLatitude(double lat) {
        return lat - Math.PI * Math.floor((lat + Math.PI / 2) / Math.PI);
    }

	//draw the overall boundary of the map
    protected void drawBoundary(Graphics g, IContainer container, Color lc) {
        Graphics2D g2 = (Graphics2D) g;

        // Semi-axes of the ellipse
        double a = _radius;           // Semi-major axis (horizontal)
        double b = _radius;      // Semi-minor axis (vertical)

        // Parameters for ellipse sampling
        double step = 0.01; // Increment for parametric angle t (in radians)

        // Prepare to draw the ellipse
        Point screenPoint = new Point();
        Point2D.Double worldPoint = new Point2D.Double();
        GeneralPath path = new GeneralPath();

        // Generate points along the ellipse
        for (double t = 0; t < 2 * Math.PI; t += step) {
            // Compute (x, y) on the ellipse
            worldPoint.x = a * Math.cos(t);
            worldPoint.y = b * Math.sin(t);

            // Convert to screen coordinates
            container.worldToLocal(screenPoint, worldPoint);

            // Add the point to the path
            if (t == 0) {
                path.moveTo(screenPoint.x, screenPoint.y); // Start the path
            } else {
                path.lineTo(screenPoint.x, screenPoint.y); // Connect to the next point
            }
        }
        path.closePath(); // Close the path to complete the ellipse

        // Draw the ellipse outline
        g2.setColor(lc);
        g2.draw(path);
    }

	@Override
	public EProjection getProjection() {
		return EProjection.ORTHOGRAPHIC;
	}

	@Override
	public String name() {
		return getProjection().getName();
	}
}
