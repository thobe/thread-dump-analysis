package org.neo4j.analysis.threaddump;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.analysis.threaddump.StackElement.MonitorEntry;

public class ThreadData
{
    private final StackElement[] stack;
    private final String info;
    private final Thread.State state;
    private final String stateComment;
    final String id;
    private static int idgen = 0;

    ThreadData( String info, Thread.State state, String stateComment, StackElement... stack )
    {
        String idString = info.substring( 0, info.indexOf( '"', 1 ) + 1 );
        if ( idString.length() == 0 ) idString = "\"Unknown thread " + ( idgen++ ) + "\"";
        this.id = idString;
        this.info = info;
        this.state = state;
        this.stateComment = stateComment;
        this.stack = stack;
    }

    public boolean isSystemThread()
    {
        return state == null;
    }

    boolean matches( String filter )
    {
        if ( filter == null ) return true;
        for ( StackElement element : stack )
        {
            if ( element.matches( filter ) ) return true;
        }
        return false;
    }

    void setup( Map<String, MonitorState> monitors )
    {
        for ( int index = 0; index < stack.length; index++ )
        {
            StackElement element = stack[index];
            if ( element instanceof MonitorEntry )
            {
                MonitorEntry monitor = (MonitorEntry) element;
                MonitorState monitorState = monitors.get( monitor.id );
                if ( monitorState == null )
                {
                    monitors.put( monitor.id, monitorState = new MonitorState( monitor.id ) );
                }
                monitor.update( this, index, monitorState );
            }
        }
    }

    void print( PrintStream out )
    {
        out.println( info );
        if ( state != null ) out.println( "  java.lang.Thread.State: " + state + stateComment() );
        for ( StackElement element : stack )
        {
            element.print( out );
        }
    }

    void graphviz( PrintStream gv )
    {
        gv.print( "  " + id + " [\n    shape=none\n    label=<<TABLE>" );
        gv.print( "<TR><TD>" + info + "</TD></TR>" );
        if ( state != null )
            gv.print( "<TR><TD>java.lang.Thread.State: " + state + stateComment() + "</TD></TR>" );
        for ( int index = 0; index < stack.length; index++ )
        {
            StackElement element = stack[index];
            element.graphviz( gv, index );
        }
        gv.print( "</TABLE>>\n  ]" );
        gv.println();
    }

    private String stateComment()
    {
        return stateComment != null ? " " + stateComment : "";
    }

    static ThreadData readFrom( String[] chunk )
    {
        if ( chunk.length == 1 ) return new ThreadData( chunk[0], null, null );
        if ( !chunk[0].startsWith( "\"" ) ) return null;
        Thread.State state = null;
        String stateComment = null;
        int i = 1;
        if ( chunk[i].startsWith( "java.lang.Thread.State:" ) )
        {
            String[] stateDescr = chunk[i++].split( ":", 2 )[1].trim().split( " ", 2 );
            state = Thread.State.valueOf( stateDescr[0] );
            if ( stateDescr.length == 2 ) stateComment = stateDescr[1].trim();
        }
        List<StackElement> stack = new ArrayList<StackElement>();
        while ( i < chunk.length )
        {
            stack.add( StackElement.readFrom( chunk[i++] ) );
        }
        return new ThreadData( chunk[0], state, stateComment,
                stack.toArray( new StackElement[stack.size()] ) );
    }
}
