package cnuphys.chimera.grid;

import java.util.Objects;

import cnuphys.chimera.util.ThetaPhi;

public class Fivetuple {
	private static final String NTHETA = "n" + ThetaPhi.SMALL_THETA;
	private static final String NPHI = "n" + ThetaPhi.SMALL_PHI;

	public int nx;
	public int ny;
	public int nz;
	public int ntheta;
	public int nphi;
	
	public Fivetuple() {
		set(-1, -1, -1, -1, -1);
	}


	/**
	 * Constructor for the Fiveplet class.
	 *
	 * @param nx     index on the x grid.
	 * @param ny     index on the y grid.
	 * @param nz     index on the z grid.
	 * @param ntheta index on the theta grid.
	 * @param nphi   index on the phi grid.
	 */
	public Fivetuple(int nx, int ny, int nz, int ntheta, int nphi) {
		set(nx, ny, nz, ntheta, nphi);
	}
	
	public void set(int nx, int ny, int nz, int ntheta, int nphi) {
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		this.ntheta = ntheta;
		this.nphi = nphi;
	}

    // Override equals to compare the contents of the tuple
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
			return true;
		}
        if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
        Fivetuple that = (Fivetuple) obj;
        return nx == that.nx && ny == that.ny && nz == that.nz &&
               ntheta == that.ntheta && nphi == that.nphi;
    }

	 // Override hashCode to generate a hash based on all five values
    @Override
    public int hashCode() {
        return Objects.hash(nx, ny, nz, ntheta, nphi);
    }

	@Override
	public String toString() {
		return String.format("[nx = %d, ny = %d, nz = %d, %s = %d, %s = %d]", nx, ny, nz, NTHETA, ntheta, NPHI, nphi);
	}

}
