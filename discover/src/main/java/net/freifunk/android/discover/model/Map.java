package net.freifunk.android.discover.model;

/**
 * Created by bjoern petri on 12/4/14.
 */
public class Map {

    String mapName;
    String mapUrl;

    public Map(String name, String mapUrl) {
        this.mapName = name;
        this.mapUrl = mapUrl;
    }

    public String getMapName() {
        return mapName;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public String details() {
        final StringBuffer sb = new StringBuffer("MapMaster{");
        sb.append("name='").append(mapName).append('\'');
        sb.append(", mapUrl='").append(mapUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
