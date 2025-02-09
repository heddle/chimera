package cnuphys.chimera.grid;

import java.util.List;
import java.util.Objects;

import cnuphys.bCNU.util.Bits;
import cnuphys.chimera.util.Point3D;

/**
 * A class to represent a a Cartesian grid cell that intersects a sphere.
 */
public class Cell {

	// the possible intersection types
	public static final int cornerIn = 0;
	public static final int cornerOut = 1;
	public static final int doubleCornerIn = 2;
	public static final int doubleCornerOut = 3;
	public static final int faceCut = 4;
	public static final int cornerPull = 5;
	public static final int cornerPush = 6;
	public static final int skewCut = 7;
	public static final int singleEdge = 8; // probably doesn't happen
	public static final int kiss = 9;
	
	//corresponding to the intersection types
	public static final String[] intersectionTypes = {
            "cornerIn", "cornerOut", "doubleCornerIn", "doubleCorner",
            "faceCut", "cornerPull", "cornerPush", "skewCut", "singleEdge", "kiss"};

	// the indices on the Cartesian grid
	public int nx;
	public int ny;
	public int nz;

	// the corners that are inside the sphere (bitwise)
	private int insideCorners;

	// the edges that intersect the sphere
	private int[] edgeIntersections;
	private Edge[] edges;

	//The intersection type
	private int intersectionType = -1;
	
	//The Cartesian grid
	private CartesianGrid grid;

	/**
	 * Constructor for the Cell class.
	 *
	 * @param nx        index on the x grid.
	 * @param ny        index on the y grid.
	 * @param nz        index on the z grid.
	 * @param inCorners the bitwise indices of the corners of the cell that are
	 *                  inside the sphere
	 */
	public Cell(CartesianGrid grid, int nx, int ny, int nz, int inCorners, double radius) {
		this.grid = grid;
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		insideCorners = inCorners;
		edgeIntersections = GridSupport.findIntersectingEdges(inCorners);
		intersectionType = getIntersectionType();
		getEdges(radius);
	}
	
	private void getEdges(double radius) {
		if (edgeIntersections == null) {
			return;
		}
		
		edges = new Edge[edgeIntersections.length];
		
		int i = 0;
		for (int edgeIndex : edgeIntersections) {
			//get the end points
			int[] corners = GridSupport.getCornersOfEdges(edgeIndex);
			
			Point3D.Double startCorner = new Point3D.Double(GridSupport.getCellCorner(grid, nx, ny, nz, corners[0]));
			Point3D.Double endCorner = new Point3D.Double(GridSupport.getCellCorner(grid, nx, ny, nz, corners[1]));
			
			edges[i] = new Edge(startCorner, endCorner, radius);
			i++;
		}
	}

	/**
	 * Get the intersection type
	 * 
	 * @return the intersection type
	 */
	public int getIntersectionType() {
		
		if (intersectionType < 0) {
			
			int numInside = numInsideCorners();
			
			
			
			int numEdges = edgeIntersections.length;
			if (numInside == 0 && numEdges == 0) {
				return kiss;
			}
			
			if (numInside == 1) {
				if (numEdges == 3) {
					return cornerIn;
				}
			}
			
			if (numInside == 2) {
				if (numEdges == 4) {
					return doubleCornerIn;
				}
				if (numEdges == 2) {
					return singleEdge;
				}
			}
			
			if (numInside == 3) {
				if (numEdges == 5) {
					return cornerPull;
				}

			}
			
			if (numInside == 4) {
				if (numEdges == 4) {
					return faceCut;
				}
				if (numEdges == 6) {
					return skewCut;
				}

			}
			
			if (numInside == 5) {
				if (numEdges == 5) {
                    return cornerPush;
				}
			}
			
			if (numInside == 6) {
				if (numEdges == 4) {
					return doubleCornerOut;
				}
			}
			
			if (numInside == 7) {
				if (numEdges == 3) {
					return cornerOut;
				}
			}
			
			System.err.println("Unknown intersection type with " + numInside + " inside corners and " + numEdges + " edge intersections.");
			System.exit(-1);
		}
		
		
		return intersectionType;
	}

	/**
	 * Get the corners (stored bitwise) that are inside the sphere
	 * 
	 * @return the corners that are inside the sphere
	 */
	public int getInsideCorners() {
		return insideCorners;
	}

	/**
	 * Get the count of the number of corners that are inside the sphere
	 * 
	 * @return the number of inside corners
	 */
	public int numInsideCorners() {
		return Bits.countBits(insideCorners);
	}

	/**
	 * Get the count of the number of edges that intersect the sphere
	 * 
	 * @return the number of intersecting edges
	 */
	public int getNumEdgeIntersections() {
		return edgeIntersections.length;
	}

	/**
	 * Get the count of the number of corners that are outside the sphere
	 * 
	 * @return the number of outside corners
	 */
	public int numOutsideCorners() {
		return 8 - numInsideCorners();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Cell that = (Cell) obj;
		return nx == that.nx && ny == that.ny && nz == that.nz;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nx, ny, nz);
	}

	@Override
	public String toString() {
		return String.format("[nx = %d, ny = %d, nz = %d]", nx, ny, nz);
	}

	/**
	 * Report a list of Threeplets
	 * 
	 * @param list the list to report
	 */
	public static void report(List<Cell> list) {
		System.out.println("\nIntersecting Cells Overview");
		int[] counts = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

		for (Cell t : list) {
			counts[t.intersectionType]++;
		}

		int total = 0;
		for (int i = 0; i < counts.length; i++) {
			total += counts[i];
		}

		for (int i = 0; i < counts.length; i++) {
			System.out.println(String.format("%18s %d", intersectionTypes[i], counts[i]));
		}
		System.out.println("Total intersecting cells: " + total);

	}
}