/*
 * SaveNodeAsyncTask.java
 *
 * Copyright (C) 2015 Bjoern Petri
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

package net.freifunk.android.discover.async;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import net.freifunk.android.discover.helper.Database;
import net.freifunk.android.discover.helper.EventBus;
import net.freifunk.android.discover.model.Node;
import net.freifunk.android.discover.model.NodeMap;
import net.freifunk.android.discover.model.NodeResult;

import java.util.HashMap;


public class SaveNode extends AsyncTask<NodeMap, Object, NodeMap[]> {

    private static final String TAG = SaveNode.class.getName();

    @Override
    protected NodeMap[] doInBackground(NodeMap[] nodeMaps) {
        Database database = Database.getInstance();

        for (NodeMap nodeMap : nodeMaps) {
            HashMap<String, Node> nodeList = nodeMap.getNodes();

            for (Node node : nodeList.values()) {
                database.addNode(node);
            }
        }

        return nodeMaps;
    }

    @Override
    protected void onPostExecute(NodeMap[] nodeMaps) {
        for (NodeMap nodeMap : nodeMaps) {
            int size = nodeMap.getNodes().size();
            Log.e(TAG, "Finished database saving" + nodeMap.getMapName() + " (" + size + " entries)");

            EventBus.getInstance().post(new NodeResult(NodeResult.NodeResultType.SAVE_NODES,nodeMap));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    public void executeAsyncTask(NodeMap[] params) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        }
        else {
            super.execute(params);
        }
    }
}
