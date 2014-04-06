/*
 * CommunityListActivity.java
 *
 * Copyright (C) 2014  Philipp Dreimann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package net.freifunk.android.discover;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import net.freifunk.android.discover.model.Community;


/**
 * An activity representing a list of Communities. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CommunityDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CommunityListFragment} and the item details
 * (if present) is a {@link CommunityDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link CommunityListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class CommunityListActivity extends FragmentActivity
        implements CommunityListFragment.Callbacks, GmapsFragment.Callbacks {

    private static final String TAG = "CommunityListActivity";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_list);
        // Show the Up button in the action bar.

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.community_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((CommunityListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.community_list))
                    .setActivateOnItemClick(true);

            showGmapsDefault();
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    private void showGmapsDefault() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.community_detail_container, GmapsFragment.newInstance(GmapsFragment.COMMUNITY_TYPE))
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link CommunityListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Community c) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(CommunityDetailFragment.ARG_ITEM_ID, c.getApiName());
            CommunityDetailFragment fragment = new CommunityDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.community_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, CommunityDetailActivity.class);
            detailIntent.putExtra(CommunityDetailFragment.ARG_ITEM_ID, c.getApiName());
            startActivity(detailIntent);
        }
    }

    @Override
    public void onMarkerClicked(Object o) {
        Log.d(TAG, "onMarkerClicked");

        Community c = (Community) o;
        onItemSelected(c);
        Log.d(TAG, "onMarkerClicked");
    }
}
