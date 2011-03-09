/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.analysis.threaddump;

import java.io.PrintStream;
import java.util.Set;

import org.neo4j.analysis.threaddump.StackElement.MonitorEntry;

final class ThreadMonitor
{
    private final MonitorEntry monitor;
    private final ThreadData thread;
    private final int index;

    ThreadMonitor( MonitorEntry monitor, ThreadData thread, int index )
    {
        this.monitor = monitor;
        this.thread = thread;
        this.index = index;
    }

    @Override
    public int hashCode()
    {
        return thread.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return ( obj instanceof ThreadMonitor ) && ( (ThreadMonitor) obj ).thread == thread;
    }

    ThreadData graphviz( PrintStream gv, String id, Set<ThreadData> included )
    {
        String port = ":l" + index;
        gv.println( "  " + thread.id + port + " -> \"" + id + "\" [color=" + monitor.color() + "]" );
        return included.contains( thread ) ? thread : null;
    }
}
