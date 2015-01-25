/*
 * SettingsActivity.java
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.CompoundButton;
import android.widget.RadioGroup;


import net.freifunk.android.discover.model.MapMaster;
import net.freifunk.android.discover.model.NodeMap;
import net.freifunk.android.discover.model.NodeMapComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_data_sync);

        // get data via the key
        final MapMaster mapMaster = MapMaster.getInstance();


        ArrayList<NodeMap> nodeMapArrayList = new ArrayList<NodeMap>(mapMaster.getMaps());
        PreferenceCategory communities = (PreferenceCategory) findPreference("communities");

        // sort by Name
        Collections.sort(nodeMapArrayList, new NodeMapComparator());

        if (nodeMapArrayList != null && communities != null) {
            for (final NodeMap nm : nodeMapArrayList ) {

                 PreferenceScreen communityPreferenceScreen = getPreferenceManager().createPreferenceScreen(SettingsActivity.this);

                 communityPreferenceScreen.setTitle(nm.getMapName());
                 communityPreferenceScreen.setKey(nm.getMapName());

                 final CheckBoxPreference deactivateCommunityPreference = new CheckBoxPreference(SettingsActivity.this);

                 // TODO: move Strings to resources
                 deactivateCommunityPreference.setTitle("Community aktiv");
                 deactivateCommunityPreference.setSummary("deaktivieren, falls Community nicht auf der Karte angezeigt werden soll");
                 deactivateCommunityPreference.setKey("community_deactivate_" + nm.getMapName());
                 deactivateCommunityPreference.setChecked(nm.isActive());
                 deactivateCommunityPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                     @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                         MapMaster mapMaster = MapMaster.getInstance();
                         DatabaseHelper db = DatabaseHelper.getInstance(null);
                         boolean newActive = newValue.toString().equals("true") ? true : false;

                         nm.setActive(newActive);
                         db.addNodeMap(nm);

                         // triggers map update
                         nm.setAddedToMap(false);
                         mapMaster.updateMap(nm);

                         return true;
                    }
                 });

                 EditTextPreference editCommunityPreference = new EditTextPreference(SettingsActivity.this);

                 // TODO: move Strings to resources
                 editCommunityPreference.setTitle("URL bearbeiten");
                 editCommunityPreference.setSummary("aendern, falls eine andere Quelle genutzt werden soll.");
                 editCommunityPreference.setKey("community_edit_" + nm.getMapName());
                 editCommunityPreference.setText(nm.getMapUrl());


                 editCommunityPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                     @Override
                     public boolean onPreferenceChange(Preference preference, Object newValue) {

                         DatabaseHelper db = DatabaseHelper.getInstance(null);



                         // remove old nodes from node
                         nm.setActive(false);
                         nm.setAddedToMap(false);
                         mapMaster.updateMap(nm);

                         nm.setMapUrl(newValue.toString());
                         db.addNodeMap(nm);
                         db.deleteAllNodesForMap(nm);

                         // load new nodes
                         nm.setActive(true);
                         nm.loadNodes();
                         mapMaster.updateMap(nm);

                         return true;
                     }
                 });

                 communityPreferenceScreen.addPreference(deactivateCommunityPreference);
                 communityPreferenceScreen.addPreference(editCommunityPreference);

                 communities.addPreference((Preference) communityPreferenceScreen);
            }
        }

        setupActionBar();

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_wifi"));
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));


        }
    }
}
