package cnuphys.mosaic.patch;

public class Tuple {

	private int[] indices;
	public int length;

    private static final String indexNames[] = {"Nx", "Ny", "Nz", "Ntheta", "Nphi"};
    private static final String patchNames[] = {"Prepatch", "Thetapatch", "Patch"};

	/**
     * Constructor for the Tuple class. The number of indices must be 3, 4, or 5.
     * A prepatch tuple has three indices, a thetapatch tuple has four indices,
     * and a patch tuple has five indices.
     *
     * @param indices the indices of the tuple
     * @throws IllegalArgumentException if the number of indices is not 3, 4, or 5
     */
	public Tuple(int... indices) {
		set(indices);
	}

	/**
	 * Does the given indices match the tuple?
	 * @param indices the indices
	 * @return <code>true</code> if the indices match
	 */
	public boolean matches(int... indices) {
		int len = indices.length;
		if (len != length) {
			throw new IllegalArgumentException("Wrong number of tuple indices");
		}

		for (int i = 0; i < len; i++) {
			if (this.indices[i] != indices[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the indices of the tuple
	 *
	 * @return the indices
	 */
	public int[] getIndices() {
		return indices;
	}

	public int getNx() {
		return indices[0];
	}

	public int getNy() {
		return indices[1];
	}

	public int getNz() {
		return indices[2];
	}

	public int getNtheta() {
		return indices[3];
	}

	public int getNphi() {
		return indices[4];
	}

	/**
	 * Set the indices of the tuple, overriding any existing indices.
	 *
	 * @param indices the indices
	 */
	public void set(int... indices) {
		int len = indices.length;
		if (len < 3 || len > 5) {
			(new IllegalArgumentException("A Tuple must have 3, 4, or 5 five indices.")).printStackTrace();
			System.exit(1);
		}
		this.length = len;
		this.indices = indices;
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
		Tuple other = (Tuple) obj;
		if (other.length != length) {
			return false;
		}

		for (int i = 0; i < length; i++) {
			if (other.indices[i] != indices[i]) {
				return false;
			}
		}
		return true;
	}


    public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(patchNames[length - 3] + " ");
		sb.append("[");
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(indexNames[i] + " = " + indices[i]);
		}
		sb.append("]");
		return sb.toString();
    }


}
