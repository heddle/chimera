package cnuphys.mosaic.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cnuphys.mosaic.patch.BasePatch;

/**
 * A class to represent a "country" in the context of the Mosaic application.
 * It is essentially a patch where the theta and phi in radians are converted
 * to latitude and longitude in degrees. It will be written out in geojson format
 * so that other mapping programs can use it.
 */
public class Country {
    
    private String _name;
    
    private double _latitudes[];
    private double _longitudes[];

    public Country(BasePatch patch, int n) {
        // Use the tuple for the name
        _name = patch.getTuple().toString();
        
        List<ThetaPhi> spVertices = patch.getSphericalVertices(n);
        int count = spVertices.size();
        
        _latitudes = new double[count];
        _longitudes = new double[count];
        
        for (int i = 0; i < count; i++) {
            ThetaPhi tp = spVertices.get(i);
            _latitudes[i] = Math.toDegrees(tp.getLatitude());
            _longitudes[i] = Math.toDegrees(tp.getPhi());
        }
    }
    
    public String getName() {
        return _name;
    }
    
    public double[] getLatitudes() {
        return _latitudes;
    }
    
    public double[] getLongitudes() {
        return _longitudes;
    }

    /**
     * Write out the countries in GeoJSON format.
     * 
     * @param countries the list of countries
     * @param path      the path to write the geojson file
     */
    public static void geoJson(List<Country> countries, String path) {
        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{ \"type\": \"FeatureCollection\", \"features\": [");

        for (int i = 0; i < countries.size(); i++) {
            Country country = countries.get(i);
            geoJson.append("\n  { \"type\": \"Feature\", \"properties\": { \"name\": \"")
                   .append(country.getName()).append("\" }, \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[");

            double[] latitudes = country.getLatitudes();
            double[] longitudes = country.getLongitudes();

            for (int j = 0; j < latitudes.length; j++) {
                geoJson.append("[").append(longitudes[j]).append(", ").append(latitudes[j]).append("]");
                if (j < latitudes.length - 1) {
                    geoJson.append(", ");
                }
            }

            // Close the polygon
            geoJson.append("]] } }");

            if (i < countries.size() - 1) {
                geoJson.append(",");
            }
        }

        geoJson.append("\n]}");

        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(geoJson.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
