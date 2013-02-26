package de.nijen.freifunknord;

import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import de.nijen.freifunknord.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class MapView_UI extends MapActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);

		MapView mapView = (MapView) findViewById(R.id.mapview);
		 mapView.setBuiltInZoomControls(true);
		 
		 //Standort bestimmen
		 final MapController mapControl = mapView.getController();
		 mapControl.setZoom(16);
		 
		 LocationGPS gps = new LocationGPS(this);
		 if(gps.canGetLocation()){
			 GeoPoint pointGPS = new GeoPoint((int) ((gps.getLatitude())*1e6), (int) (( gps.getLongitude())*1e6));
		 
			 mapControl.setCenter(pointGPS);

			 MapView.LayoutParams mapMarkerParams = new MapView.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, pointGPS, MapView.LayoutParams.CENTER ); 
			 ImageView mapMarker = new ImageView(getApplicationContext()); 
			 mapMarker.setImageResource(android.R.drawable.ic_menu_mylocation);//ic_menu_refresh); 
			 mapView.addView(mapMarker, mapMarkerParams);
		 }
		 
		 //Nodes auf der Karte ausgeben
		 List<Overlay> mapOverlays = mapView.getOverlays();
		 Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		 Map_ItemizedOverlay itemizedoverlay = new Map_ItemizedOverlay(drawable,this);
		 
		 for(int i=0;i < Nodes.geolist.size(); i++){
			 String name = Nodes.geolist.get(i).get("name");
			 double geonord = Double.valueOf( Nodes.geolist.get(i).get("geoNord"));
			 double geoost  = Double.valueOf( Nodes.geolist.get(i).get("geoOst"));
			 
			 
			 GeoPoint point = new GeoPoint((int) (geonord * 1e6),(int) (geoost * 1e6));
			 OverlayItem overlayitem = new OverlayItem( point, "Node: " + name ,"Route hierher");
			 
			 itemizedoverlay.addOverlay(overlayitem);
			 
		 }
		 
		 mapOverlays.add(itemizedoverlay);
		 }
		 @Override
		 protected boolean isRouteDisplayed()
		 {
		 return false;
		 }
		 
}