package cnuphys.chimera.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cnuphys.bCNU.util.Bits;
import cnuphys.chimera.curve.GeneralCurve;
import cnuphys.chimera.curve.Patch;
import cnuphys.chimera.curve.PoleEnclosureChecker;
import cnuphys.chimera.util.MosaicPlane;
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
	public static final int kiss = 8;

	public static final int polar = 99; //polar is an attribute, not another type

	public static int allTypes = -1;

	// corresponding to the intersection types
	public static final String[] intersectionTypes = { "cornerIn", "cornerOut", "doubleCornerIn", "doubleCornerOut",
			"faceCut", "cornerPull", "cornerPush", "skewCut", "kiss" };

	// the indices on the Cartesian grid
	public int nx;
	public int ny;
	public int nz;

	// the corners that are inside the sphere (bitwise)
	private int insideCorners;

	// the edges that intersect the sphere
	private int[] edgeIntersections;
	private Edge[] edges;

	// The intersection boundary curves
	private List<GeneralCurve> _boundaryCurves = new ArrayList<>();

	// The intersection type
	private int intersectionType = -1;

	// The Cartesian grid
	private CartesianGrid grid;

	//sphere radius
	private double radius;

	//pole enclosure flag
	private int poleEnclosed = 0;

	//closest point (just inside) if this is a kiss
	public Point3D.Double closestPoint;
	
	//associated prePatch
	private Patch prepatch;

	/**
	 * Constructor for the Cell class.
	 *
	 * @param nx        index on the x grid.
	 * @param ny        index on the y grid.
	 * @param nz        index on the z grid.
	 * @param inCorners the bitwise indices of the corners of the cell that are
	 *                  inside the sphere
	 * @param radius    the radius of the sphere
	 */
	public Cell(CartesianGrid grid, int nx, int ny, int nz, int inCorners, double radius) {
		this.grid = grid;
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		this.radius = radius;
		insideCorners = inCorners;
		edgeIntersections = GridSupport.findIntersectingEdges(inCorners);
		intersectionType = getIntersectionType();
		makeEdges(radius);

		if (edges != null) {
	        int numEdges = edges.length;
			for (int i = 0; i < numEdges; i++) {
				int j = (i + 1) % numEdges;
				int commonFace = edges[i].getCommonFace(edges[j]);
                if (commonFace >= 0) {
                    GeneralCurve curve = new GeneralCurve(this, commonFace, edges[i].getIntersection(), edges[j].getIntersection(), radius);
                    _boundaryCurves.add(curve);
                }
			} //i loop

			//pole enclosed?
			poleEnclosed = PoleEnclosureChecker.checkPoleEnclosure(_boundaryCurves);
			if (poleEnclosed != 0) {
				System.err.println("Pole enclosed type " + poleEnclosed + " for cell (" + nx + ", " + ny + ", " + nz + ")");
			}

		} //edges not null

	}
	
	/**
	 * Get the associated prePatch
	 * 
	 * @return the associated prePatch
	 */
	public Patch getPrepatch() {
		return prepatch;
	}

	/**
	 * Set the associated prePatch
	 * @param prePatch the prePatch to set
	 */
	public void setPrepatch(Patch prePatch) {
		this.prepatch = prePatch;
	}
	
	/**
	 * Get the closest point (just inside) if this is a kiss
	 * @return the closest point (just inside) if this is a kiss,
	 * othewise null.
	 */
	public Point3D.Double getClosestPoint() {
		return closestPoint;
	}

	/**
	 * Get the pole enclosure flag
	 *
	 * @return the pole enclosure flag
	 */
	public int getPoleEnclosed() {
		return poleEnclosed;
	}

	/**
	 * Set the closest point (it means this cell is a kiss)
	 * @param p the closest point
	 */
	public void setClosestPoint(Point3D.Double p) {
		closestPoint = new Point3D.Double(p.x, p.y, p.z);
	}

	// make the edges that intersect the sphere
	private void makeEdges(double radius) {
		if (edgeIntersections == null) {
			return;
		}

		edges = new Edge[edgeIntersections.length];

		int i = 0;
		for (int edgeIndex : edgeIntersections) {
			// get the end points
			int[] corners = GridSupport.getCornersOfEdges(edgeIndex);
			edges[i] = new Edge(grid, corners[0], corners[1], nx, ny, nz, radius);
			i++;
		}

		// order the edges
		edges = EdgeOrdering.reorderEdges(edges);
	}

	/**
	 * Get the edges that intersect the sphere
	 *
	 * @return the edges that intersect the sphere
	 */
	public Edge[] getEdges() {
        return edges;
	}

	public CartesianGrid getCartesianGrid() {
		return grid;
	}

	public List<GeneralCurve> getBoundaryCurves() {
		return _boundaryCurves;
	}

	/**
	 * Get the radius of the sphere
	 * @return the radius of the sphere
	 */
	public double getRadius() {
		return radius;
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

			System.err.println("Unknown intersection type with " + numInside + " inside corners and " + numEdges
					+ " edge intersections.");
			System.exit(-1);
		}

		return intersectionType;
	}

	/**
	 * Get the intersection type as a string
	 *
	 * @return the intersection type as a string
	 */
	public String getIntersectionTypeString() {
		return intersectionTypes[getIntersectionType()];
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

	/**
	 * Returns a unit normal to the plane of the specified face.
	 * The face is identified by its canonical index:
	 * <ul>
	 *   <li>Face 0: corners {0, 1, 3, 2} (typically the plane z = z0)</li>
	 *   <li>Face 1: corners {4, 5, 7, 6} (typically the plane z = z1)</li>
	 *   <li>Face 2: corners {0, 1, 5, 4} (typically the plane y = y0)</li>
	 *   <li>Face 3: corners {2, 3, 7, 6} (typically the plane y = y1)</li>
	 *   <li>Face 4: corners {0, 2, 6, 4} (typically the plane x = x0)</li>
	 *   <li>Face 5: corners {1, 3, 7, 5} (typically the plane x = x1)</li>
	 * </ul>
	 *
	 * @param face the face index (0 to 5)
	 * @return a double[3] representing the unit normal to that face.
	 * @throws IllegalArgumentException if the face index is invalid.
	 */
	public double[] getUnitNormal(int face) {
	    // Get all 8 cell corners (in canonical order) for this cell.
	    double[][] cellCorners = GridSupport.getCellCorners(grid, nx, ny, nz);

	    // Get the four canonical indices for the requested face.
	    int[] faceIndices = GridSupport.getFaceCornerIndices(face);

	    // Pick three points on the face.
	    double[] p0 = cellCorners[faceIndices[0]];
	    double[] p1 = cellCorners[faceIndices[1]];
	    double[] p2 = cellCorners[faceIndices[2]];

	    // Compute two vectors that lie in the face.
	    double[] v1 = new double[] { p1[0] - p0[0], p1[1] - p0[1], p1[2] - p0[2] };
	    double[] v2 = new double[] { p2[0] - p0[0], p2[1] - p0[1], p2[2] - p0[2] };

	    // The cross product v1 x v2 gives a vector perpendicular to the face.
	    double[] normal = new double[3];
	    normal[0] = v1[1] * v2[2] - v1[2] * v2[1];
	    normal[1] = v1[2] * v2[0] - v1[0] * v2[2];
	    normal[2] = v1[0] * v2[1] - v1[1] * v2[0];

	    // Normalize the vector.
	    double mag = Math.sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]);
	    if (mag == 0) {
	        throw new IllegalStateException("Degenerate face: cannot compute a normal.");
	    }
	    normal[0] /= mag;
	    normal[1] /= mag;
	    normal[2] /= mag;

	    return normal;
	}

	   /**
     * Returns a ChimeraPlane corresponding to the given face index.
     * <p>
     * The face index must be between 0 and 5 (inclusive) and uses the canonical
     * face definitions:
     * <ul>
     *   <li>Face 0: corners {0, 1, 3, 2} (typically the plane z = z0)</li>
     *   <li>Face 1: corners {4, 5, 7, 6} (typically the plane z = z1)</li>
     *   <li>Face 2: corners {0, 1, 5, 4} (typically the plane y = y0)</li>
     *   <li>Face 3: corners {2, 3, 7, 6} (typically the plane y = y1)</li>
     *   <li>Face 4: corners {0, 2, 6, 4} (typically the plane x = x0)</li>
     *   <li>Face 5: corners {1, 3, 7, 5} (typically the plane x = x1)</li>
     * </ul>
     * </p>
     *
     * @param face the face index (0 to 5)
     * @return the ChimeraPlane corresponding to that face.
     * @throws IllegalArgumentException if the face index is invalid.
     */
    public MosaicPlane getPlane(int face) {
        // Validate face index.
        if (face < 0 || face > 5) {
            throw new IllegalArgumentException("Face index must be between 0 and 5: " + face);
        }

        // Get all 8 cell corners (each a double[3]) for this cell.
        double[][] cellCorners = GridSupport.getCellCorners(grid, nx, ny, nz);

        // Get the canonical face corner indices.
        int[] faceIndices = GridSupport.getFaceCornerIndices(face);

        // Convert the first three corners for the face into Point3D.Double objects.
        // (Assuming the ordering in GridSupport is such that these three points are non-collinear.)
        Point3D.Double p0 = new Point3D.Double(cellCorners[faceIndices[0]][0],
                                                 cellCorners[faceIndices[0]][1],
                                                 cellCorners[faceIndices[0]][2]);
        Point3D.Double p1 = new Point3D.Double(cellCorners[faceIndices[1]][0],
                                                 cellCorners[faceIndices[1]][1],
                                                 cellCorners[faceIndices[1]][2]);
        Point3D.Double p2 = new Point3D.Double(cellCorners[faceIndices[2]][0],
                                                 cellCorners[faceIndices[2]][1],
                                                 cellCorners[faceIndices[2]][2]);

        // Create and return a new ChimeraPlane defined by these three points.
        return new MosaicPlane(p0, p1, p2);
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Cell that = (Cell) obj;
		return nx == that.nx && ny == that.ny && nz == that.nz;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nx, ny, nz);
	}

	/**
	 * Get the center of the cell
	 *
	 * @return the center of the cell
	 */
	public double[] getCenter() {
		double[] center = new double[3];
		double[][] corners = GridSupport.getCellCorners(grid, nx, ny, nz);
		for (int i = 0; i < 8; i++) {
			center[0] += corners[i][0];
			center[1] += corners[i][1];
			center[2] += corners[i][2];
		}
		center[0] /= 8.0;
		center[1] /= 8.0;
		center[2] /= 8.0;
		return center;
	}

	@Override
	public String toString() {
		return String.format("Cell (%d, %d, %d) %s", nx, ny, nz, getIntersectionTypeString());
	}

	/**
	 * Report a list of Threeplets
	 *
	 * @param list the list to report
	 */
	public static void report(List<Cell> list) {
		System.out.println("\nIntersecting Cells Overview");
		int[] counts = { 0, 0, 0, 0, 0, 0, 0, 0, 0};

		for (Cell t : list) {
			counts[t.intersectionType]++;
		}

		int total = 0;
		for (int count : counts) {
			total += count;
		}

		for (int i = 0; i < counts.length; i++) {
			System.out.println(String.format("%18s %d", intersectionTypes[i], counts[i]));
		}
		System.out.println("Total intersecting cells: " + total);

	}
}