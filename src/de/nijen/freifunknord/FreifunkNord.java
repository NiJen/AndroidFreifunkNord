package de.nijen.freifunknord;

import java.util.ArrayList;
import java.util.HashMap;

import de.nijen.freifunknord.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FreifunkNord extends Activity {
    /** Called when the activity is first created. */
	private Uri url ;
	ArrayList<HashMap<String, String>> result;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String default_url = getString(R.string.nodes_url_hh);
    	url = Uri.parse(sharedPrefs.getString("ffstaedteValues", default_url));
    	HttpAsyncTask task = new HttpAsyncTask(url.toString(), result, this);
    	task.execute();
    }
    

   public void  updateList(){
	   SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   String default_url = getString(R.string.nodes_url_hh);
	   url = Uri.parse(sharedPrefs.getString("ffstaedteValues", default_url));
  	   HttpAsyncTask task = new HttpAsyncTask(url.toString(), result, this);
  	   task.execute();	 
   }
   
	protected void displayResults(ArrayList<HashMap<String, String>> result2) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.freifunkhh_main);
		TextView StatsTV = (TextView) findViewById(R.id.textViewStats);
		//TODO Besser auslesen lassen
		String ort = "Hamburg";
		try{
			String[] ortetest = getResources().getStringArray(R.array.ffstaedteValues);
			if (sharedPrefs.getString("ffstaedteValues", "").equals(ortetest[0])){
				ort ="Hamburg";
			}
			else if (sharedPrefs.getString("ffstaedteValues", "").equals(ortetest[1])){
				ort ="Lübeck";
			}
			else if (sharedPrefs.getString("ffstaedteValues", "").equals(ortetest[2])){
				ort ="Kiel";
			}
		}finally{};
		//ENDE todo 
		
		StatsTV.setText(  "Knoten: " + String.valueOf(Nodes.intKnoten) + "\t" + "\t"
						+ "Clients: " + String.valueOf(Nodes.intClients) + "\t" + "\t"
						+ "Gateways: " + String.valueOf(Nodes.intGateways) + "\t" + "\t"
						+ ort
						);
		
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
	    case R.id.menu_settings:
	    	Intent settingsActivity = new Intent(getBaseContext(),
                    Preference.class);
	    	startActivity(settingsActivity);
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