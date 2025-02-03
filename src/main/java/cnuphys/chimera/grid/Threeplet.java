package cnuphys.chimera.grid;

import java.util.Objects;


/**
 * A class to represent a threeplet of integers.
 * Used to hold Cartesian cell indices.
 */
public class Threeplet {
    public int nx;
    public int ny;
    public int nz;

    /**
     * Constructor for the Threeplet class.
     *
     * @param nx     index on the x grid.
     * @param ny     index on the y grid.
     * @param nz     index on the z grid.
     */
    public Threeplet(int nx, int ny, int nz) {
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Threeplet that = (Threeplet) obj;
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
}