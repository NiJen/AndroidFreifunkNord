package de.nijen.freifunknord;

import java.util.ArrayList;
import java.util.HashMap;

import de.nijen.freifunknord.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
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
	//String nodeurl = "http://188.138.99.158/mesh/nodes.json"; //Nodekarte Lübeck
	String nodeurl = "http://freifunk.in-kiel.de/ffmap/nodes.json"; //Nodekarte Kiel
	ArrayList<HashMap<String, String>> result;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        //setContentView(R.layout.activity_json_client);
    	url = Uri.parse(nodeurl);
    	HttpAsyncTask task = new HttpAsyncTask(url.toString(), result, this);
    	task.execute();
    }
    

   public void  updateList(){
	   url = Uri.parse(nodeurl);
  		HttpAsyncTask task = new HttpAsyncTask(url.toString(), result, this);
  		task.execute();	 
   }
   
	protected void displayResults(ArrayList<HashMap<String, String>> result2) {
		setContentView(R.layout.freifunkhh_main);
		TextView StatsTV = (TextView) findViewById(R.id.textViewStats);
		
		StatsTV.setText( "Knoten: " + String.valueOf(Nodes.intKnoten) + "\t"
						+ "Clients: " + String.valueOf(Nodes.intClients) + "\t" 
					//	+ "Online: "  + String.valueOf(NodesHH.intOnline)+ "\n"
						+ "Gateways: " + String.valueOf(Nodes.intGateways)  + "\t" 
						+ "Kiel"//stringOrt
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
	/*    case R.id.help:
	        showHelp();
	        return true;*/
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}