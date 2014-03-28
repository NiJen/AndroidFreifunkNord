package de.nijen.freifunknord;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FreifunkNord extends Activity {
	ArrayList<HashMap<String, String>> result;
    /**
     * Called when the activity is first created.
     */
    private Uri url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateList();
    }
    

   public void  updateList() {
       Resources res = getResources();
       String[] staedte = res.getStringArray(R.array.staedte);
       List<Uri> uris = new ArrayList<Uri>();
       for (String s : staedte) {
           uris.add(Uri.parse(s));
       }
       HttpAsyncTask task = new HttpAsyncTask(uris, result, this);
       task.execute();
   }
   
	protected void displayResults(ArrayList<HashMap<String, String>> result2) {
		setContentView(R.layout.freifunkhh_main);
		TextView StatsTV = (TextView) findViewById(R.id.textViewStats);

		StatsTV.setText(  "Knoten: " + String.valueOf(Nodes.intKnoten) + "\t" + "\t"
						+ "Clients: " + String.valueOf(Nodes.intClients) + "\t" + "\t"
                + "Gateways: " + String.valueOf(Nodes.intGateways));

        ListView listView = (ListView) findViewById(R.id.listView1);
		ListAdapter adapter = new SimpleAdapter(FreifunkNord.this, Nodes.mylist ,R.layout.custom_row_view,
				 						new String[]{"id", "name", "geo", "online"},
				 						new int[]{ R.id.nodeid , R.id.nodename, R.id.nodegeo, R.id.nodeonline });
	 
	    listView.setAdapter(adapter);
		this.result = result2;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.freifunknord_menu, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_updatelist:
	    	updateList();
	        return true;
	    case R.id.map_view:
	    	Intent map_view = new Intent(this,
                    MapView_UI.class);
	    	startActivity(map_view);
	        return true;
	    case R.id.menu_settings:
	        return true;
	    case R.id.WWW:
        	String www=this.getString(R.string.www);
        	Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(www));
        	startActivity(viewIntent);  
            return true;
        case R.id.feedback:
        	final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        	emailIntent.setType("plain/text");
        	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{this.getString(R.string.email)});
        	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, this.getString(R.string.feedback)+" "+this.getString(R.string.app_name));
        	startActivity(emailIntent); 
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void showSettings() {
	  
	}
}
