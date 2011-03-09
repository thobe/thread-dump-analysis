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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ThreadDump implements Iterable<ThreadData>
{
    private final Collection<ThreadData> threads;
    private final Map<String, MonitorState> monitors;
    private final String date;
    private final String info;

    ThreadDump( String[] header, Collection<ThreadData> threads )
    {
        this.date = header == null ? "unkown" : header[0];
        this.info = header == null ? "unkown" : header[1];
        this.threads = threads;
        this.monitors = new HashMap<String, MonitorState>();
        for ( ThreadData thread : threads )
        {
            thread.setup( monitors );
        }
    }

    @Override
    public String toString()
    {
        return date + " - " + threads.size() + " threads";
    }

    public Iterator<ThreadData> iterator()
    {
        return threads.iterator();
    }

    void dumpGraphvizFile( File directory, String filter ) throws IOException
    {
        PrintStream output = new PrintStream( new File( directory, date.replace( ' ', '_' ) + ".gv" ) );
        graphviz( output, filter );
    }

    private void graphviz( PrintStream gv, String filter )
    {
        gv.println( "digraph ThreadsAndLocks {" );
        gv.println( "  label=\"" + info + "\"" );
        Set<ThreadData> included = new HashSet<ThreadData>();
        for ( ThreadData thread : threads )
        {
            if ( !thread.isSystemThread() && thread.matches( filter ) )
            {
                thread.graphviz( gv );
                included.add( thread );
            }
        }
        Set<ThreadData> extra = new HashSet<ThreadData>();
        for ( MonitorState monitor : monitors.values() )
        {
            extra.addAll( monitor.graphviz( gv, included ) );
        }
        for ( ThreadData thread : extra )
        {
            thread.graphviz( gv );
        }
        gv.println( "}" );
    }

    public void print( PrintStream out )
    {
        out.println( date );
        out.println( info );
        out.println();
        for ( ThreadData thread : threads )
        {
            thread.print( out );
            out.println();
        }
    }

    public void printLocks( PrintStream out )
    {
        for ( MonitorState monitor : monitors.values() )
        {
            monitor.print( out, threads );
        }
    }
}
