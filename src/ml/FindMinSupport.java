/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sim
 */
public class FindMinSupport {

    public static void main( String[] args ) {
        File f = new File( "/space/raxml/VINCENT/RAxML_bipartitions.125.BEST.WITH" );

        TreeParser tp = new TreeParser(f);


        LN n = tp.parse();

        LN[] nodelist = getAsList(n);

        System.out.printf( "nodes: %d\n", nodelist.length );

        int nTT = 0;
        for( LN node : nodelist ) {
            int nt = numTips(node);

            if( nt == 2 ) {
                nTT++;

                String[] tn = getTipNames(node);

                assert( tn.length == 2 );

                System.out.printf( "%s %f (%s %s): %d\n", node.data, node.data.getSupport(), tn[0], tn[1], nt);

                LN tnt = getTowardsNonTip(node);

                int c = 2;
                if( c == 1 ) {
                    tnt.back.back = tnt.next.back;
                    tnt.next.back.back = tnt.back;
                } else if( c == 2 ) {
                    tnt.back.back = tnt.next.next.back;
                    tnt.next.next.back.back = tnt.back;
                }
                break;
            }
//            else if( nt == 1 ) {
//                String[] tn = getTipNames(node);
//
//                assert( tn.length == 1 );
//
//                System.out.printf( "%s %f (%s): %d\n", node.data, node.data.getSupport(), tn[0], nt);
//            }

            

        }

        
        try {
            PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream("/tmp/smalltree")));
            TreePrinter.printRaw(n, ps);
            ps.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FindMinSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        

        System.out.printf( "nTT: %d\n", nTT );
    }

    private static int countNodes(LN n) {
        if( n.data.isTip ) {
            return 1;
        } else {
            return 1 + countNodes(n.next.back) + countNodes(n.next.next.back);
        }
    }

    private static LN[] getAsList(LN n) {
        int nNodes = countNodes(n);
        LN[] list = new LN[nNodes];



        int xpos = insertDFS( n, list, 0 );

        if( xpos != nNodes ) {
            throw new RuntimeException("xpos != nNodes");
        }

        return list;
    }

    static int insertDFS( LN n, LN[] list, int pos ) {
        if( n.data.isTip ) {
            list[pos] = n;
            return pos + 1;
        } else {

            pos = insertDFS(n.next.back, list, pos);
            pos = insertDFS(n.next.next.back, list, pos);
            list[pos] = n;
            return pos + 1;
        }
    }

    static int numTips( LN n ) {
        if( n.data.isTip ) {
            return 0;
        } else {
            LN start = n;
            LN cur = n.next;
            int nTips = 0;

            while( cur != start ) {
                if( cur.back.data.isTip ) {
                    nTips++;
                }
                cur = cur.next;
            }

            return nTips;
        }
    }

    static LN getTowardsNonTip( LN n ) {
        LN start = n;
        LN cur = n.next;
        
        while( cur != start ) {
            
            cur = cur.next;

            if( !cur.back.data.isTip ) {
                break;
            }
        }

        return cur;
    }

    static String[] getTipNames( LN n ) {
        ArrayList<String> tipnames = new ArrayList<String>();
        
        LN start = n;
        LN cur = n.next;
        int nTips = 0;

        while( cur != start ) {
            if( cur.back.data.isTip ) {
                tipnames.add( cur.back.data.getTipName());
            }
            cur = cur.next;
        }

        String[] ra = new String[tipnames.size()];


        for( int i = 0; i < tipnames.size(); i++ ) {
            ra[i] = tipnames.get(i);
        }

        return ra;
    }
}
