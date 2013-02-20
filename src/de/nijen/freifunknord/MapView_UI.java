package de.nijen.freifunknord;

import java.util.List;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
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

		 List<Overlay> mapOverlays = mapView.getOverlays();
		 Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		 Map_ItemizedOverlay itemizedoverlay = new Map_ItemizedOverlay(drawable,this);
		 /*
		 //Beispiel
		 GeoPoint point = new GeoPoint(30443769,-91158458);
		 OverlayItem overlayitem = new OverlayItem(point, "Laissez les bon temps rouler!", "I'm in Louisiana!");

		 GeoPoint point2 = new GeoPoint(17385812,78480667);
		 OverlayItem overlayitem2 = new OverlayItem(point2, "Namashkaar!", "I'm in Hyderabad, India!");
		 
		 GeoPoint point3 = new GeoPoint((int) (53.55108 * 1e6),(int) (9.99368 * 1e6));
		 OverlayItem overlayitem3 = new OverlayItem(point3, "Hamburg!", "I'm in Hamburg, Germany!");

		 itemizedoverlay.addOverlay(overlayitem);
		 itemizedoverlay.addOverlay(overlayitem2);
		 itemizedoverlay.addOverlay(overlayitem3);
		 // Ende Beispiel
		 */
		 for(int i=0;i < Nodes.geolist.size(); i++){
			 String name = Nodes.geolist.get(i).get("name");
			 double geonord = Double.valueOf( Nodes.geolist.get(i).get("geoNord"));
			 double geoost  = Double.valueOf( Nodes.geolist.get(i).get("geoOst"));
			 
			 
			 GeoPoint point = new GeoPoint((int) (geonord * 1e6),(int) (geoost * 1e6));
			 OverlayItem overlayitem = new OverlayItem( point, "Node" ,name);
			 
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