package de.nijen.freifunknord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Nodes {

    public static int intOnline = 0;
    public static int intClients = 0;
    public static int intGateways = 0;
    public static int intKnoten = 0;
    static ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
    static ArrayList<HashMap<String, String>> geolist = new ArrayList<HashMap<String, String>>();
    String result;

    public static ArrayList<HashMap<String, String>> getDisplayString(String responseFromInternet, boolean adding) {
        if (!adding) {
            mylist.removeAll(mylist);
            geolist.removeAll(geolist);
        }

        JSONObject jArray = new JSONObject();
        try {
            jArray = new JSONObject(responseFromInternet);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (!adding) {
            mylist.clear();
        }
        try {
			JSONArray  nodes = jArray.getJSONArray("nodes");
            if (!adding) {
                intOnline = 0;
                intGateways = 0;
                intKnoten = 0;
                intClients = 0;
            }
            System.out.println("Found " + nodes.length());
            for(int i=0;i<nodes.length();i++){
                 JSONObject e = nodes.getJSONObject(i);

				 //Auslesen der Geocoordinaten
				 if( e.getString("geo").startsWith("[") ){
					HashMap<String, String> geocoords = new HashMap<String, String>();
				 	geocoords.put("name", e.getString("name"));
				 
				 	String Geocoordinaten = e.getString("geo");
				 	String[] splitString = Geocoordinaten.split(",");
				 
				 	geocoords.put("geoNord", splitString[0].substring(1));
				 	geocoords.put("geoOst", splitString[1].substring(0, splitString[1].length()-1));
				 	
				 	geolist.add(geocoords);
				 }
				 
				 //Auslesen der Nodes für die Liste
				 HashMap<String, String> map = new HashMap<String, String>();
				 
				 JSONObject jsonFlags = e.getJSONObject("flags");
				 if(jsonFlags.getString("online").contentEquals("true")){
					 intOnline += 1;
					 if(jsonFlags.getString("client").contentEquals("true")){
						 intClients += 1;
					 }else{
						 map.put("id", e.getString("id"));
						 map.put("name", e.getString("name"));
						 map.put("geo", e.getString("geo"));
                         map.put("online", "online");

                         mylist.add(map);
					 }
				 }
				 
				 if(jsonFlags.getString("gateway").contentEquals("true")){
					 intGateways += 1;
				 }	 				  
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