package net.freifunk.android.discover.model;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pd on 31.03.14.
 */
public class Community {
    private static final String TAG = "Community";
    public static final Set<Community> communities = Collections.synchronizedSet(new HashSet<Community>());

    String name;
    String detailUrl;
    private String addressCity;
    private String addressName;
    private String addressStreet;
    private String addressZipcode;
    private double lat;
    private double lon;
    private String url;
    private String apiName;
    private String api;
    private String metacommunity;

    public Community(String name, String detailUrl) {
        this.name = name;
        this.detailUrl = detailUrl;
    }

    public String getApiName() {
        return apiName;
    }

    @Override
    public String toString() {
        return apiName;
    }

    public interface CommunityReady {
        void ready(Community c);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public void populate(final RequestQueue rq, final CommunityReady communityReady) {

        rq.add(new JsonObjectRequest(getDetailUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    setApiName(jsonObject.getString("name"));
                    setApi(jsonObject.getString("api"));
                    if (jsonObject.has("metacommunity"))
                        setMetacommunity(jsonObject.getString("metacommunity"));

                    JSONObject location = jsonObject.getJSONObject("location");
                    setAddressCity(location.getString("city"));
                    if (location.has("address")) {
                        JSONObject address = jsonObject.getJSONObject("address");
                        if (address.has("Name"))
                            setAddressName(address.getString("Name"));

                        if (address.has("Street"))
                            setAddressStreet(address.getString("Street"));

                        if (address.has("Zipcode"))
                            setAddressZipcode(address.getString("Zipcode"));
                    }
                    setLat(location.getDouble("lat"));
                    setLon(location.getDouble("lon"));
                    setUrl(jsonObject.getString("url"));
                    Log.d(TAG, getCommunity().toString());
                    communityReady.ready(getCommunity());
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
               Log.e(TAG, volleyError.toString());
            }
        }));
    }

    private Community getCommunity() {
        return this;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public void setMetacommunity(String metacommunity) {
        this.metacommunity = metacommunity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressZipcode(String addressZipcode) {
        this.addressZipcode = addressZipcode;
    }

    public String getAddressZipcode() {
        return addressZipcode;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLat() {
        return lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLon() {
        return lon;
    }

    public String details() {
        final StringBuffer sb = new StringBuffer("Community{");
        sb.append("name='").append(name).append('\'');
        sb.append(", detailUrl='").append(detailUrl).append('\'');
        sb.append(", addressCity='").append(addressCity).append('\'');
        sb.append(", addressName='").append(addressName).append('\'');
        sb.append(", addressStreet='").append(addressStreet).append('\'');
        sb.append(", addressZipcode='").append(addressZipcode).append('\'');
        sb.append(", lat=").append(lat);
        sb.append(", lon=").append(lon);
        sb.append(", url='").append(url).append('\'');
        sb.append(", apiName='").append(apiName).append('\'');
        sb.append(", api='").append(api).append('\'');
        sb.append(", metacommunity='").append(metacommunity).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
