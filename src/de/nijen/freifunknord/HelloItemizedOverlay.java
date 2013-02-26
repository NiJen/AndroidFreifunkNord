package de.nijen.freifunknord;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class HelloItemizedOverlay extends ItemizedOverlay {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	public HelloItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
		}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	
	public HelloItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
	}
	
	protected boolean onTap(int index) {
	  final OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  
	  final int lat = item.getPoint().getLatitudeE6();
	  final int lon = item.getPoint().getLongitudeE6();
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.setNeutralButton("Route hierher", 
		new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
              final Intent i = new Intent(Intent.ACTION_VIEW);
              //i.setData( Uri.parse("google.navigation:q=" + lat/1e6 + "," + lon/1e6)); //Direkte Navigation mit Google Maps
              i.setData( Uri.parse("geo:0,0?q=" + lat/1e6 + "," + lon/1e6 + " (" + item.getSnippet() + ")"));
              mContext.startActivity(i);
              return;
          }
      });
	  dialog.show();
	  return true;
	}
	
	@Override
	public int size() {
	  return mOverlays.size();
	}
	
}
