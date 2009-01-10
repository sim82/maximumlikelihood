/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ml;

import java.io.PrintStream;

/**
 *
 * @author sim
 */
public class TreePrinter {
	static boolean doIndent;

	static void print( LN tree, boolean indent ) {
		doIndent = indent;

		print( tree, 0 );
	}

	private static void print( LN node, int level ) {

		if( doIndent ) {
			for (int i = 0; i < level; i++) {
				System.out.print(" ");
			}
		}

		if( node.data.isTip ) {
			System.out.printf("leaf: %s\n", node.data.getTipName());
		} else {
			System.out.printf("inner node: %s\n", node.data);
			print(node.next.back, level + 1);
			print(node.next.next.back, level + 1);

			if( level == 0 ) {
				print( node.back, level + 1 );
			}
		}
	}

    static void printRaw( LN node, PrintStream s, boolean root) {
        if( node.data.isTip ) {
			s.printf("%s:%G", node.data.getTipName(), node.backLen);
		} else {
			s.print("(");
			printRaw(node.next.back, s ,false);
			s.print(",");
            printRaw(node.next.next.back, s ,false);

			if( root ) {
                s.print(",");
				printRaw( node.back, s ,false);
                s.printf(");" );
			} else {
                s.printf("):%G", node.backLen );
            }
		}
    }
    static void printRaw( LN node, PrintStream s ) {
        printRaw( node, s, true );
    }
}