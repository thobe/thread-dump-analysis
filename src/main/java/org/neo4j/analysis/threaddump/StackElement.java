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

public abstract class StackElement
{
    public static final class MethodEntry extends StackElement
    {
        private final String line;

        MethodEntry( String line )
        {
            this.line = line;
        }

        @Override
        public String toString()
        {
            return "at " + line;
        }

        @Override
        boolean matches( String filter )
        {
            return line.contains( filter );
        }
    }

    public static abstract class MonitorEntry extends StackElement
    {
        final String id;

        MonitorEntry( String id )
        {
            this.id = id;
        }

        protected static String readId( String line )
        {
            return line.substring( line.indexOf( '<' ) + 1, line.indexOf( '>' ) );
        }

        abstract void update( ThreadData thread, int index, MonitorState state );

        @Override
        void graphviz( PrintStream gv, String line, int index )
        {
            gv.print( "<TR><TD ALIGN=\"LEFT\" PORT=\"l" + index + "\">" + line
                      + "</TD></TR>" );
        }

        abstract String color();
    }

    public static final class LockMonitor extends MonitorEntry
    {
        private final String line;

        LockMonitor( String line )
        {
            super( readId( line ) );
            this.line = line;
        }

        @Override
        public String toString()
        {
            return " - " + line;
        }

        @Override
        void update( ThreadData thread, int index, MonitorState state )
        {
            state.owners.add( new ThreadMonitor( this, thread, index ) );
        }

        @Override
        String color()
        {
            return "green";
        }
    }

    public static final class WaitMonitor extends MonitorEntry
    {
        private final String line;

        WaitMonitor( String line )
        {
            super( readId( line ) );
            this.line = line;
        }

        @Override
        public String toString()
        {
            return " - " + line;
        }

        @Override
        void update( ThreadData thread, int index, MonitorState state )
        {
            state.waiters.add( new ThreadMonitor( this, thread, index ) );
        }

        @Override
        String color()
        {
            return "red";
        }
    }

    boolean matches( String filter )
    {
        return false;
    }

    void print( PrintStream out )
    {
        out.println( "    " + this );
    }

    final void graphviz( PrintStream gv, int index )
    {
        String repr = toString();
        repr = repr.replace( "&", "&amp;" );
        repr = repr.replace( "<", "&lt;" );
        repr = repr.replace( ">", "&gt;" );
        graphviz( gv, repr, index );
    }

    void graphviz( PrintStream gv, String line, int index )
    {
        gv.print( "<TR><TD ALIGN=\"LEFT\">" + line + "</TD></TR>" );
    }

    static StackElement readFrom( String line )
    {
        if ( line.startsWith( "at" ) )
        {
            return new MethodEntry( line.substring( 3 ) );
        }
        else if ( line.startsWith( "-" ) )
        {
            line = line.substring( 2 );
            if ( line.startsWith( "lock" ) )
            {
                return new LockMonitor( line );
            }
            else if ( line.startsWith( "park" ) || line.startsWith( "wait" ) )
            {
                return new WaitMonitor( line );
            }
        }
        else if ( line.startsWith( "Local Variable:" ) )
        {
            // a root in a heap dump
        }
        throw new IllegalArgumentException( line );
    }
}
