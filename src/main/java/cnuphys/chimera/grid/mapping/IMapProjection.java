package cnuphys.chimera.grid.mapping;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;

public interface IMapProjection {

	/**
	 * Convert from lat/lon to x/y
	 *
	 * @param latLon the lat/lon point (latitude in y, longitude in x)
	 * @param xy     the x/y point
	 */
	public void latLonToXY(Point2D.Double latLon, Point2D.Double xy);

	/**
	 * Convert from x/y to lat/lon
	 *
	 * @param latLon the lat/lon point (latitude in y, longitude in x)
	 * @param xy     the x/y point
	 */
	public void latLonFromXY(Point2D.Double latLon, Point2D.Double xy);

	/**
	 * Draw the map outline
	 *
	 * @param g         the graphics context
	 * @param container the {@link cnuphys.bCNU.graphics.container.IContainer
	 *                  IContainer}
	 */
	public void drawMapOutline(Graphics g, IContainer container);


	/**
	 * Check if a given xy point is on the map
	 * @param xy the point to check
	 * @return <code>true</code> if the point is on the map
	 */
	public boolean isPointOnMap(Point2D.Double xy);

	/**
	 * Draw a latitude line
	 * @param g2 the graphics context
	 * @param container the container
	 * @param latitude the latitude of the line in radians
	 */
	public void drawLatitudeLine(Graphics2D g2, IContainer container, double latitude);

	/**
	 * Draw a longitude line
	 *
	 * @param g2        the graphics context
	 * @param container the container
	 * @param latitude  the longitude of the line in radians should be [-PI, PI]
	 */
	public void drawLongitudeLine(Graphics2D g2, IContainer container, double longitude);

	/**
	 * Check if a given lat/lon point is visible on the map. This is false
	 * ony for projections that do not cover the entire globe the Orthographic.
	 *
	 * @param latLon the point to check (latitude in y, longitude in x)
	 * @return <code>true</code> if the point is visible
	 */
	public boolean isPointVisible(Point2D.Double latLon);

	/**
	 * Get the projection
	 *
	 * @return the projection enum value
	 */
	public EProjection getProjection();

	/**
	 * Get the name of the projection
	 * @return the name of the projection
	 */
	public String name();

}
