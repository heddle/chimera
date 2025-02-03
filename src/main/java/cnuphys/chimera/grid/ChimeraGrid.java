package cnuphys.chimera.grid;

public class ChimeraGrid {

	private CartesianGrid cartGrid;
	private SphericalGrid sphGrid;

	public ChimeraGrid(CartesianGrid cartGrid, SphericalGrid sphGrid) {
		this.cartGrid =cartGrid;
		this.sphGrid = sphGrid;
	}

	public void setCartesianGrid(CartesianGrid cartGrid) {
		this.cartGrid = cartGrid;
	}

	public void setSphericalGrid(SphericalGrid sphGrid) {
		this.sphGrid = sphGrid;
	}

	public CartesianGrid getCartesianGrid() {
		return cartGrid;
	}

	public SphericalGrid getSphericalGrid() {
		return sphGrid;
	}
}
