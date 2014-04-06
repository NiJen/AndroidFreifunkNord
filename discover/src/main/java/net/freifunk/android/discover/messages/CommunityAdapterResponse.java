package net.freifunk.android.discover.messages;

import android.widget.ArrayAdapter;

import net.freifunk.android.discover.model.Community;

/**
 * Created by pd on 01.04.14.
 */
public class CommunityAdapterResponse {

    public ArrayAdapter<Community> getCommunityAdapter() {
        return communityAdapter;
    }

    private final ArrayAdapter communityAdapter;

    public CommunityAdapterResponse(ArrayAdapter<Community> communityAdapter) {
        this.communityAdapter = communityAdapter;
    }
}
