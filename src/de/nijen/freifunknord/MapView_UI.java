package de.nijen.freifunknord;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapView_UI extends Activity {

    private void centerMapOnMyLocation(GoogleMap map) {

        map.setMyLocationEnabled(true);

        Location location = map.getMyLocation();

        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    15));

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // Get a handle to the Map Fragment
        GoogleMap map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        centerMapOnMyLocation(map);

        //Nodes auf der Karte ausgeben
        for (int i = 0; i < Nodes.geolist.size(); i++) {
            String name = Nodes.geolist.get(i).get("name");
            double geonord = Double.valueOf(Nodes.geolist.get(i).get("geoNord"));
            double geoost = Double.valueOf(Nodes.geolist.get(i).get("geoOst"));

            LatLng point = new LatLng(geonord, geoost);
            System.out.println("Added " + name + point);
            map.addMarker(new MarkerOptions()
                    .title(name)
                    .snippet("FF-Node")
                    .position(point));

        }
    }
}