/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ml;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

enum Base {

	A,
	G,
	C,
	T
}

class Node {

	double[][] w;
	boolean wValid;
	final int N;
	final static char[] baseChars = {'A', 'G', 'C', 'T'}; // same order as enum Base
	final static double PI = 0.25;
	Node cL = null;
	Node cR = null;
	boolean isLeaf = false;
	String data;

	Node(int N) {
		this.N = N;

		w = new double[Base.values().length][N];
		wValid = false;
	}

	void addLeft(Node n) {
		assert (!isLeaf);
		cL = n;
	}

	void addRight(Node n) {
		assert (!isLeaf);
		cR = n;
	}

	void add(Node l, Node r) {
		addLeft(l);
		addRight(r);
	}

	void initLeaf(String seq) {
		assert (seq.length() == N);
		assert (cL == null && cR == null);

		for (int i = 0; i < N; i++) {
			char c = seq.charAt(i);
			boolean found = false;
			for (int j = 0; j < baseChars.length; j++) {
				if (c == baseChars[j]) {
					w[j][i] = 1.0;
					found = true;
					break;
				}

			}

			if (!found) {
				throw new RuntimeException("bad char in seq: " + seq);
			}
		}

		wValid = true;
		isLeaf = true;
	}

	public static double jukesCantorSub(int x, int y, double t) {
		if (x != y) {
			return (1.0 - Math.exp(-t)) * PI;
		} else {
			return Math.exp(-t) * (1.0 - Math.exp(-t)) * PI;
		}
	}

	void prettyPrint() {
		prettyPrint(this, 0);
	}

	private void updateW(int i) {
		for (int x = 0; x < baseChars.length; x++) {
			//w[i][x]

			double sumLeft = 0.0;
			double sumRight = 0.0;

			for (int yz = 0; yz < baseChars.length; yz++) {
				sumLeft += jukesCantorSub(x, yz, 0.1) * cL.getW(yz, i);
				sumRight += jukesCantorSub(x, yz, 0.1) * cR.getW(yz, i);
			}

			w[x][i] = sumLeft * sumRight;

		}

	}

	public void updateW() {
		if (isLeaf) {
			throw new RuntimeException("update called on leaf");
		}

		for (int i = 0; i < N; i++) {
			updateW(i);
		}

		wValid = true;
	}

	public double getW(int x, int i) {
		if (!wValid) {
			updateW();
		}

		return w[x][i];
	}

	double subtreeLikelihood() {
		if (!wValid) {
			updateW();
		}

		double likelihood = 1;

		for (int i = 0; i < N; i++) {
			double sum = 0.0;
			for (int x = 0; x < baseChars.length; x++) {
				sum += PI * w[x][i];
			}
			likelihood *= sum;
		}

		return likelihood;
	}

	private static void prettyPrint(Node next, int level) {
		for (int i = 0; i < level; i++) {
			System.out.print(" ");
		}

		if (next.isLeaf) {
			System.out.printf("leaf: %s\n", next.data);
		} else {
			System.out.printf("inner node:\n");
			prettyPrint(next.cL, level + 1);
			prettyPrint(next.cR, level + 1);
		}

	}
}








/**
 *
 * @author sim
 */
public class Main {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		testParser();
		System.exit(0);

		int N = 10;

		Node leaf_0_1 = new Node(N);
		Node leaf_0_2 = new Node(N);
		Node leaf_0_3 = new Node(N);
		Node leaf_0_4 = new Node(N);


		String seq1;
		String seq2;
		String seq3;
		String seq4;


		if (true) {
			seq1 = "ACGTATCTTA";
			seq2 = "ACTTATATTA";
			seq3 = "GTCGTTACGT";
			seq4 = "GTCTGTAAGT";
		} else {
			seq1 = "ACGTATCTTA";
			seq2 = "ACGTATCTTA";
			seq3 = "ACGTATCTTA";
			seq4 = "ACGTATCTTA";
		}

		leaf_0_1.initLeaf(seq1);
		leaf_0_2.initLeaf(seq2);
		leaf_0_3.initLeaf(seq3);
		leaf_0_4.initLeaf(seq4);

		Node node_1_1 = new Node(N);
		Node node_1_2 = new Node(N);


		node_1_1.add(leaf_0_1, leaf_0_2);
		node_1_2.add(leaf_0_3, leaf_0_4);

		Node node_2_1 = new Node(N);
		node_2_1.add(node_1_1, node_1_2);

		System.out.printf("l: %g\n", node_2_1.subtreeLikelihood());
	}

	private static void testParser() {
		//TreeParser tp = new TreeParser(test);
		//TreeParser tp = new TreeParser(new File("/space/src/ml/benchmark/real_data_trees/GTR_ESTIMATED/101_SC_TREE"));
		TreeParser tp = new TreeParser(new File("/space/raxml/VINCENT/354.WITH.boot.tre.phy"));
	//	TreeParser2 tp = new TreeParser2("(a:1.0,b:2.0,(e:3.0,f:4.0):5.0);");
		
		//PrettyPrinter.print(n, true);
        try {
            PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream("/tmp/tree.txt")));
            LN n;// = tp.parse();
            while( ( n = tp.parse()) != null ) {
                System.out.printf( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> parse finished\n" );
                TreePrinter.printRaw(n, ps );
                ps.println();
            }

            ps.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        System.out.println();
        
		System.out.printf( "num leafs: %d\n", tp.nLeafs );
		System.out.printf( "num inner nodes: %d\n", tp.nInnerNodes );
	}
	static String test = "((((caaa[0]:0.0162412,haaa[0]:0.0162412):0.121338,(((raaa[1]:0.00150384,(dbaa[1]:0.000772447,(yaaa[1]:0.000246929,qaaa[1]:0.000246929):0.000525518):0.000731395):0.00125391,(oaaa[1]:0.00053079,bbaa[1]:0.00053079):0.00222696):0.0459541,(((taaa[1]:0.00427759,abaa[1]:0.00427759):0.000117345,(kaaa[1]:0.00226356,(((uaaa[1]:0.00103836,xaaa[1]:0.00103836):7.33073e-06,naaa[1]:0.00104569):0.000400717,ebaa[1]:0.0014464):0.000817163):0.00213137):0.000170342,(paaa[1]:0.000765108,maaa[1]:0.000765108):0.00380017):0.0441466):0.0888671):0.128575,((((cbaa[1]:0.00896541,(waaa[1]:0.000987231,vaaa[1]:0.000987231):0.00797818):0.00521325,saaa[1]:0.0141787):0.0154227,((kbaa[2]:0.00113176,mbaa[2]:0.00113176):0.00128529,jbaa[2]:0.00241706):0.0271843):0.0459589,((((ibaa[2]:0.00420465,(lbaa[2]:0.00256841,obaa[2]:0.00256841):0.00163624):0.000641241,gbaa[2]:0.00484589):0.00297421,hbaa[2]:0.00782011):0.0144918,(fbaa[2]:0.00289432,nbaa[2]:0.00289432):0.0194176):0.0532484):0.190593):0.883064,(((eaaa[0]:0.00321491,faaa[0]:0.00321491):0.0169111,(iaaa[0]:0.00625661,((laaa[1]:0.00271183,baaa[0]:0.00271183):0.00108033,jaaa[0]:0.00379216):0.00246445):0.0138694):0.181314,(daaa[0]:0.0430344,(gaaa[0]:0.0358183,aaaa[0]:0.0358183):0.00721605):0.158406):0.947778);";
}
