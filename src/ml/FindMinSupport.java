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

	public static LN getNThReducedTree( LN n, int num ) {
		LN[] nodelist = getAsList(n);

        System.out.printf( "nodes: %d\n", nodelist.length );

        int nTT = 0;
		int i = 0;

        for( LN node : nodelist ) {
            int nt = numTips(node);

			if( node.data.getSupport() < 100.0 ) {
				continue;
			}

            if( nt == 2 ) {
                nTT++;

                String[] tn = getTipNames(node);

                assert( tn.length == 2 );

                System.out.printf( "%s %f (%s %s): %d\n", node.data, node.data.getSupport(), tn[0], tn[1], nt);

                LN tnt = getTowardsNonTip(node);

                int c = 2;
                if( i == num ) {
                    tnt.back.back = tnt.next.back;
                    tnt.next.back.back = tnt.back;

					return n;
                } 
				i++;
				if( i == num ) {
                    tnt.back.back = tnt.next.next.back;
                    tnt.next.next.back.back = tnt.back;
					return n;
                }
				i++;
			}
//            else if( nt == 1 ) {
//                String[] tn = getTipNames(node);
//
//                assert( tn.length == 1 );
//
//                System.out.printf( "%s %f (%s): %d\n", node.data, node.data.getSupport(), tn[0], nt);
//            }



        }

		return null;
	}

    public static void main( String[] args ) {
		String[] inlist = {"RAxML_bipartitions.125.BEST.WITH", "RAxML_bipartitions.1908.BEST.WITH", "RAxML_bipartitions.354.BEST.WITH", "RAxML_bipartitions.59.BEST.WITH", "RAxML_bipartitions.855.BEST.WITH",
			"RAxML_bipartitions.140.BEST.WITH", "RAxML_bipartitions.2000.BEST.WITH", "RAxML_bipartitions.404.BEST.WITH", "RAxML_bipartitions.628.BEST.WITH", "RAxML_bipartitions.8.BEST.WITH",
			"RAxML_bipartitions.150.BEST.WITH", "RAxML_bipartitions.217.BEST.WITH", "RAxML_bipartitions.500.BEST.WITH", "RAxML_bipartitions.714.BEST.WITH",
			"RAxML_bipartitions.1604.BEST.WITH", "RAxML_bipartitions.218.BEST.WITH", "RAxML_bipartitions.53.BEST.WITH", "RAxML_bipartitions.81.BEST.WITH"};


		for( String filename : inlist ) {
			createReducedTrees(filename);
		}

	}

	public static void createReducedTrees( String filename ) {
		File basedir = new File( "/space_tmp/raxml/VINCENT/" );
		
		File outdir = new File( "/space/redtree" );

		for( int i = 0;; i++ ) {
			File f = new File( basedir, filename );

			TreeParser tp = new TreeParser(f);


			LN n = tp.parse();

			n = getNThReducedTree(n, i);
			if( n == null ) {
				System.out.printf( "finished after %d trees\n", i );
				break;
			}


			try {
				File outfile = new File( outdir, filename + "_" + padchar( "" + i, '0', 4 ) );

				PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
				TreePrinter.printRaw(n, ps);
				ps.close();
			} catch (FileNotFoundException ex) {
				Logger.getLogger(FindMinSupport.class.getName()).log(Level.SEVERE, null, ex);
			}
        
        
		}
        //System.out.printf( "nTT: %d\n", nTT );
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

	private static String padchar(String string, char c, int num) {
		while( string.length() < num ) {
			string = c + string;
		}
		return string;
	}
}
