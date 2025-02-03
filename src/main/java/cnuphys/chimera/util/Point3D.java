package cnuphys.chimera.util;

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

        @Override
        public int hashCode() {
            return java.util.Objects.hash(x, y, z);
        }
    }

    // Test the Point3D.Double class
    public static void main(String[] args) {
        Point3D.Double point = new Point3D.Double(1.0, 2.0, 3.0);
        System.out.println("Initial Point: " + point);

        point.setLocation(4.0, 5.0, 6.0);
        System.out.println("Updated Point: " + point);

        Point3D.Double anotherPoint = new Point3D.Double(4.0, 5.0, 6.0);
        System.out.println("Points Equal: " + point.equals(anotherPoint));

        // Directly access fields
        System.out.println("Access x directly: " + point.x);
        System.out.println("Access y directly: " + point.y);
        System.out.println("Access z directly: " + point.z);
    }
}
