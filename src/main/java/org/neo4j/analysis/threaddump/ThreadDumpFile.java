package org.neo4j.analysis.threaddump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ThreadDumpFile implements Iterator<ThreadDump>
{
    private final BufferedReader input;
    private ThreadDump next;
    private String[] dumpChunk = {};

    public ThreadDumpFile( BufferedReader bufferedInputStream )
    {
        this.input = bufferedInputStream;
    }

    public ThreadDumpFile( File input ) throws FileNotFoundException
    {
        this( new BufferedReader( new InputStreamReader( new FileInputStream( input ) ) ) );
    }

    private ThreadDump loadNext()
    {
        if ( dumpChunk == null ) return null;
        List<ThreadData> dump = new ArrayList<ThreadData>();
        String[] header = null;
        while ( dumpChunk != null && dump.isEmpty() )
        {
            for ( String[] chunk = dumpChunk; chunk != null; chunk = readChunk() )
            {
                if ( chunk.length != 0 )
                {
                    if ( isHeaderChunk( chunk ) )
                    {
                        header = chunk;
                    }
                    else
                    {
                        ThreadData data = ThreadData.readFrom( chunk );
                        if ( data != null ) dump.add( data );
                    }
                }
            }
        }
        if ( dump.isEmpty() ) return null;
        return new ThreadDump( header, dump );
    }

    private String[] readChunk()
    {
        List<String> result = new ArrayList<String>();
        String line;
        while ( ( line = readLine() ) != null )
        {
            line = line.trim();
            if ( line.equals( "" ) ) break;
            result.add( line );
        }
        dumpChunk = ( line == null ) ? null : result.toArray( new String[result.size()] );
        if ( dumpChunk == null || isHeaderChunk( dumpChunk ) ) return null;
        return dumpChunk;
    }

    private boolean isHeaderChunk( String[] chunk )
    {
        if ( chunk.length == 2 )
        {
            if ( chunk[1].startsWith( "Full thread dump" ) )
            {
                return true;
            }
        }
        return false;
    }

    private String readLine()
    {
        try
        {
            return input.readLine();
        }
        catch ( IOException e )
        {
            return null;
        }
    }

    public boolean hasNext()
    {
        if ( next != null ) return true;
        next = loadNext();
        return next != null;
    }

    public ThreadDump next()
    {
        if ( hasNext() )
        {
            try
            {
                return next;
            }
            finally
            {
                next = null;
            }
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException( "java.util.Iterator<ThreadDump>.remove()" );
    }

    public static void main( String... args ) throws Exception
    {
        for ( String filename : args )
        {
            try
            {
                Iterator<ThreadDump> dumps = new ThreadDumpFile( new File( filename ) );
                File dir = new File( "." ).getAbsoluteFile();
                while ( dumps.hasNext() )
                {
                    ThreadDump dump = dumps.next();
                    System.out.println( dump );
                    dump.printLocks( System.out );
                    dump.dumpGraphvizFile( dir, "neo4j" );
                    System.out.println();
                }
            }
            catch ( FileNotFoundException e )
            {
                System.err.println( e );
            }
        }
    }
}
