/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sim
 */
public class MultipleAlignment {
    int nTaxon;
    int seqLen;
    String[] names;
    String[] data;
    HashMap<String,Integer> nameMap;

    public MultipleAlignment(int nTaxon, int seqLen) {
        this.nTaxon = nTaxon;
        this.seqLen = seqLen;
        this.names = new String[nTaxon];
        this.data = new String[nTaxon];
    }

    public static MultipleAlignment loadPhylip( File file ) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));

            String header = r.readLine();
            StringTokenizer st = new StringTokenizer(header);

            int nTaxon = Integer.parseInt(st.nextToken());
            int seqLen = Integer.parseInt(st.nextToken());

            MultipleAlignment ma = new MultipleAlignment(nTaxon, seqLen);

            for( int i = 0; i < nTaxon; i++ ) {
                String line = r.readLine();
                if( line == null ) {
                    throw new RuntimeException( "cannot read next line in " + file.getPath() );
                }

                st = new StringTokenizer(line);
                String name = st.nextToken();
                String data = st.nextToken();

                if( data.length() != seqLen ) {
                    throw new RuntimeException("wrong sequence length: " + data.length() + " vs " + seqLen );
                }
                ma.names[i] = name;
                ma.data[i] = data;
                
            }

            ma.buildNameIndex();

            r.close();
            return ma;

        } catch (IOException ex) {
            Logger.getLogger(MultipleAlignment.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("bailing out");
        }


    }

    public static String padSpaceRight( String s, int len ) {
        if( s.length() >= len ) {
            return s;
        } else {
            while( s.length() < len ) {
                s += " ";
            }
            return s;
        }
    }

    public void writePhylip( File file ) {
        try {
            PrintWriter w = new PrintWriter(new FileWriter(file));

            w.printf( "%d %d", nTaxon, seqLen);

            int maxNameLen = 0;

            for( String name : names ) {
                maxNameLen = Math.max( maxNameLen, name.length());
            }

            for( int i = 0; i < nTaxon; i++ ) {
                w.printf( "%s%s\n", padSpaceRight(names[i], maxNameLen + 2), data[i]);
            }
            w.close();
        } catch (IOException ex) {
            Logger.getLogger(MultipleAlignment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildNameIndex() {
        nameMap = new HashMap<String, Integer>();
        for( int i = 0; i < nTaxon; i++ ) {
            if( names[i] == null ) {
                throw new RuntimeException("cannot build name map: name is null: " + i );
            }
            nameMap.put( names[i], i );
        }
    }

    public void print() {
        for( int i = 0; i < names.length; i++ ) {
            System.out.printf( "'%s': '%s'\n", names[i], data[i] );
        }
    }

    public int nameToIdx( String name ) {
        if( nameMap.containsKey(name)) {
            return nameMap.get(name);
        } else {
            return -1;
        }
    }

    String getSequence( String name ) {
        int idx = nameToIdx(name);

        if( idx < 0 ) {
            throw new RuntimeException("taxon name not found: " + name );
        }
        return getSequence(idx);
    }

    String getSequence( int i ) {
        return data[i];
    }

    void replaceSequence( String name, String seq ) {
        int idx = nameToIdx(name);
        if( idx < 0 ) {
            throw new RuntimeException("taxon name not found: " + name );
        }

        data[idx] = seq;

    }

    public static void main( String args[] ) {
        MultipleAlignment ma = MultipleAlignment.loadPhylip(new File( "/space/raxml/VINCENT/DATA/150"));
        ma.print();
    }

}
