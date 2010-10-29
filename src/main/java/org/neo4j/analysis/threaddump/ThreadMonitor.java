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
