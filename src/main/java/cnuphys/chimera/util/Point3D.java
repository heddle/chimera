package cnuphys.chimera.util;

/**
 * A class to represent a 3D point in space.
 * Also used to represent a 3D vector.
 */
 
import java.io.Serializable;

public class Point3D implements Serializable {
    public static class Double implements Serializable {
        public double x; // x-coordinate
        public double y; // y-coordinate
        public double z; // z-coordinate

        /**
         * Default constructor. Initializes x, y, and z to 0.
         */
        public Double() {
            this(0.0, 0.0, 0.0);
        }

        /**
         * Constructs a Point3D.Double with the specified coordinates.
         *
         * @param x The x-coordinate.
         * @param y The y-coordinate.
         * @param z The z-coordinate.
         */
        public Double(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Sets the location of the point.
         *
         * @param x The x-coordinate to set.
         * @param y The y-coordinate to set.
         * @param z The z-coordinate to set.
         */
        public void setLocation(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        /**
         * The length of the vector from the origin to this point.
         * @return The length of the vector.
         */
        public double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        @Override
        public String toString() {
            return String.format("Point3D.Double[x=%.4f, y=%.4f, z=%.4f]", x, y, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
				return true;
			}
            if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
            Double other = (Double) obj;
            return java.lang.Double.compare(x, other.x) == 0 &&
                   java.lang.Double.compare(y, other.y) == 0 &&
                   java.lang.Double.compare(z, other.z) == 0;
        }
        
        /**
         * The dot product of two vectors.
         * @param a one vector
         * @param b the other vector
         * @return  The dot product.
         */
		public static double dotProduct(Double a, Double b) {
			return a.x * b.x + a.y * b.y + a.z * b.z;
		}
		
		/**
		 * The dot product of this vector with another
		 * @param other the other vector
		 * @return The dot product.
		 */
		public double dot(Double other) {
			return dotProduct(this, other);
        }
		
		/**
		 * The cross product
		 * @param a one vector
		 * @param b the other vector
		 * @param result the result vector
		 */
		public static void crossProduct(Double a, Double b, Double result) {
			result.x = a.y * b.z - a.z * b.y;
            result.y = a.z * b.x - a.x * b.z;
            result.z = a.x * b.y - a.y * b.x;
		}
		
		/**
		 * The cross product of this vector with another
		 * 
		 * @param b      the other vector
		 * @param result the result vector
		 */
		public void cross(Double b, Double result) {
			crossProduct(this, b, result);
		}
		
	    /**
	     * Subtracts vector b from vector a and stores the result in 'result'.
	     *
	     * @param a      The minuend vector.
	     * @param b      The subtrahend vector.
	     * @param result The result vector (a - b).
	     */
	    public static void subtract(Point3D.Double a, Point3D.Double b, Point3D.Double result) {
	        result.x = a.x - b.x;
	        result.y = a.y - b.y;
	        result.z = a.z - b.z;
	    }
	    
		/**
		 * Subtracts vector b from this vector and stores the result in 'result'.
		 *
		 * @param b      The subtrahend vector.
		 * @param result The result vector (this - b).
		 */
	    public void minus(Point3D.Double b, Point3D.Double result) {
	    	            subtract(this, b, result);
	    }

		/**
		 * Adds vector b to vector a and stores the result in 'result'.
		 *
		 * @param a      The first vector.
		 * @param b      The second vector.
		 * @param result The result vector (a + b).
		 */
		public void add(Point3D.Double b, Point3D.Double result) {
            result.x = x + b.x;
            result.y = y + b.y;
            result.z = z + b.z;
		}

        @Override
        public int hashCode() {
            return java.util.Objects.hash(x, y, z);
        }
    }
}
