package org.neo4j.analysis.threaddump;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MonitorState
{
    private final String id;
    final Set<ThreadMonitor> waiters = new HashSet<ThreadMonitor>();
    final Set<ThreadMonitor> owners = new HashSet<ThreadMonitor>();

    MonitorState( String id )
    {
        this.id = id;
    }

    void print( PrintStream out, Iterable<ThreadData> threads )
    {
        out.print( id + ": " );
        for ( ThreadData thread : threads )
        {
            ThreadMonitor dummy = new ThreadMonitor( null, thread, 0 );
            if ( owners.contains( dummy ) )
                out.print( "o " );
            else if ( waiters.contains( dummy ) )
                out.print( "x " );
            else
                out.print( "  " );
        }
        out.println();
    }

    Collection<ThreadData> graphviz( PrintStream gv, Set<ThreadData> included )
    {
        List<ThreadData> extra = new ArrayList<ThreadData>();
        if ( includesAny( included ) )
        {
            graphviz( extra, gv, owners, included );
            graphviz( extra, gv, waiters, included );
        }
        return extra;
    }

    private boolean includesAny( Set<ThreadData> included )
    {
        for ( ThreadData thread : included )
        {
            ThreadMonitor dummy = new ThreadMonitor( null, thread, 0 );
            if ( owners.contains( dummy ) || waiters.contains( dummy ) ) return true;
        }
        return false;
    }

    private void graphviz( List<ThreadData> extra, PrintStream gv, Set<ThreadMonitor> monitors,
            Set<ThreadData> included )
    {
        for ( ThreadMonitor monitor : monitors )
        {
            ThreadData thread = monitor.graphviz( gv, id, included );
            if ( thread != null ) extra.add( thread );
        }
    }
}
