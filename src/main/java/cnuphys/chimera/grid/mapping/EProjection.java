package cnuphys.chimera.grid.mapping;

import java.util.EnumMap;

import cnuphys.bCNU.component.EnumComboBox;
import cnuphys.chimera.frame.Chimera;

public enum EProjection {
	MOLLWEIDE, MERCATOR, ORTHOGRAPHIC;

	/**
	 * A map for the names of the projections
	 */
	public static EnumMap<EProjection, String> names = new EnumMap<>(EProjection.class);

	static {
		names.put(MOLLWEIDE, "Mollweide");
		names.put(MERCATOR, "Mercator");
		names.put(ORTHOGRAPHIC, "Orthographic");
	}

	/**
	 * Get the nice name of the enum.
	 *
	 * @return the nice name, for combo boxes, menus, etc.
	 */
	public String getName() {
		return names.get(this);
	}

	/**
	 * Returns the enum value from the name.
	 *
	 * @param name the name to match.
	 * @return the <code>EProjection</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found. Note it will check (case
	 *         insensitive) both the map and the <code>name()</code>.
	 */
	public static EProjection getValue(String name) {
		if (name == null) {
			return null;
		}

		for (EProjection val : values()) {
			// check the nice name
			// check the base name
			if (name.equalsIgnoreCase(val.toString()) || name.equalsIgnoreCase(val.name())) {
				return val;
			}
		}
		return null;
	}

	/**
	 * Obtain a combo box of choices.
	 *
	 * @param defaultChoice
	 * @return the combo box of projection choices
	 */
	public static EnumComboBox getComboBox(EProjection defaultChoice) {
		return new EnumComboBox(names, defaultChoice);
	}

	public static IMapProjection getProjection(String name) {
		if (name == null) {
            return null;
        }

		EProjection val = getValue(name);
		if (val == null) {
			return null;
		}
		
		double radius = Chimera.getInstance().getRadius();

		switch (val) {
		case MOLLWEIDE:
			return new MollweideProjection(radius);
		case MERCATOR:
			return new MercatorProjection(radius);
		case ORTHOGRAPHIC:
			return new OrthographicProjection(radius);
		}
       return null;

	}
}