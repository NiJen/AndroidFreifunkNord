package de.nijen.freifunknord;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Nodes {
	
	private JSONObject jArray;
	String result;
	static ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
	public static int intOnline = 0;
	public static int intClients = 0;
	public static int intGateways = 0;
	public static int intKnoten = 0;
	
	public ArrayList<HashMap<String, String>> getDisplayString(String responseFromInternet) {
		try {
			jArray = new JSONObject(responseFromInternet);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mylist.clear();
		try {
			JSONArray  nodes = jArray.getJSONArray("nodes");
			 JSONObject e = nodes.getJSONObject(0);
			 
			 intOnline = 0;
			 intGateways = 0;
			 intKnoten = 0;
			 intClients = 0;
			 String online = null;
			 
			 for(int i=0;i<nodes.length();i++){
				 e = nodes.getJSONObject(i);
				 
				 HashMap<String, String> map = new HashMap<String, String>();
				 map.put("id", e.getString("id"));
				 map.put("name", e.getString("name"));
				 map.put("geo", e.getString("geo"));
				 
				 JSONObject jsonFlags = e.getJSONObject("flags");
				 if(jsonFlags.getString("online").contentEquals("true")){
					 online = "online";
					 intOnline += 1;
					 if(jsonFlags.getString("client").contentEquals("true")){
						 intClients += 1;
					 }
				 }
				 else{
					 online = "offline";
				 }
				 
				 if(jsonFlags.getString("gateway").contentEquals("true")){
					 intGateways += 1;
				 }
				 				 
				 map.put("online",online); 			 
				 mylist.add(map);
				 
			 }
			 intKnoten = intOnline - intClients  ;
			 intClients = intClients - intGateways - intKnoten;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mylist;
	}
}
