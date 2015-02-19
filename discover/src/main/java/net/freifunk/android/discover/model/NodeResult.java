/*
 * NodeResult.java
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

package net.freifunk.android.discover.model;

import java.util.HashMap;

public class NodeResult
{
    public enum NodeResultType {LOAD_MAP, UPDATE_MAP, LOAD_NODES, SAVE_NODES, UPDATE_NODES};

    private NodeResultType resultType;
    private NodeMap map;
    private HashMap<String, NodeMap> maps;

    public NodeResult(NodeResultType resultType, NodeMap map)
    {
        this.resultType = resultType;
        this.map = map;
    }

    public NodeResult(NodeResultType resultType, HashMap<String, NodeMap> maps)
    {
        this.resultType = resultType;
        this.maps = maps;
    }


    public NodeResultType getResultType()
    {
        return this.resultType;
    }

    public NodeMap getResult()
    {
        return this.map;
    }

    public HashMap<String, NodeMap> getResults()
    {
        return this.maps;
    }

}
