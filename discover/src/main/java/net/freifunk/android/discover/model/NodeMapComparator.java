package net.freifunk.android.discover.model;

import java.util.Comparator;

/**
 * Created by bjoern petri on 1/25/15.
 */
public class NodeMapComparator implements Comparator<NodeMap> {

        @Override
        public int compare(NodeMap o1, NodeMap o2) {
            return o1.getMapName().compareTo(o2.getMapName());
        }

}

