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


class PrettyPrinter {
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

class ANode {

	boolean isTip;
	private String tipName;

	public void setTipName(String name) {
		assert( isTip && name != null );
		tipName = name;
	}

	public String getTipName() {
		assert( isTip );
		return tipName;
	}
}

class LN {

	ANode data;
	LN next;
	LN back;
	double backLen;


	public LN( ANode data ) {
		this.data = data;
	}

	public static LN create() {
		ANode data = new ANode();

		LN n = new LN(data);
		n.next = new LN(data);
		n.next.next = new LN(data);
		n.next.next.next = n;
		return n;
	}
}

class TreeParser2 {

	String input;

	// input as char array
	char[] inputA;

	// pointer to next char in input string
	int ptr = 0;

	int nLeafs = 0;
	int nInnerNodes = 0;

	public TreeParser2(String input) {
		this.input = input;
		this.inputA = input.toCharArray();
		ptr = 0;
	}

	public TreeParser2(File f) {
		this.input = readFile(f);
		this.inputA = input.toCharArray();
		ptr = 0;
	}

	static String readFile(File f) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(f));
			return r.readLine();
		} catch (FileNotFoundException ex) {

			Logger.getLogger(TreeParser2.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} catch (IOException ex) {
			Logger.getLogger(TreeParser2.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public void printLocation() {
		int pos1 = Math.max(0, ptr - 40);
		int pos2 = Math.min(input.length(), ptr + 40);

		System.out.println(input.substring(pos1, pos2));

		for (int i = pos1; i < ptr; i++) {
			System.out.print(" ");
		}
		System.out.println("^");
	}

	void skipWhitespace() {
		while ( ptr < inputA.length && Character.isSpaceChar(inputA[ptr])) {
			ptr++;
		}
	}

	public LN parse() {
		nLeafs = 0;
        nInnerNodes = 0;

        skipWhitespace();

        if( ptr >= inputA.length ) {
            // seems like we hit the end of the file
            return null;
        }
		// expect at least one node
		LN node = parseNode();

		// expect terminating ';'
		if (ptr >= inputA.length) {
			throw new RuntimeException("parse error. parse: end of input. missing ';'");
		}

		if (inputA[ptr] != ';') {
			printLocation();
			throw new RuntimeException("parse error. parse expects ';'");
		}

		return node;
	}

	private LN parseNode() {
		skipWhitespace();

		// lookahead: determine node type
		if (inputA[ptr] == '(') {
			return parseInnerNode();
		} else {
			return parseLeaf();
		}
	}

	private double parseBranchLength() {
		skipWhitespace();

		// expect + consume ':'
		if (inputA[ptr] != ':') {
			throw new RuntimeException("parse error: parseBranchLength expects ':' at " + ptr);
		}

		ptr++;

		skipWhitespace();

		int lend = findFloat(ptr);
		if (lend == ptr) {
			throw new RuntimeException("missing float number at " + ptr);
		}

		double l = Double.parseDouble(input.substring(ptr, lend));
		ptr = lend;

		return l;
	}

	private LN parseInnerNode() {
		skipWhitespace();


		// expect + consume '('
		if (inputA[ptr] != '(') {
			throw new RuntimeException("parse error: parseInnerNode expects '(' at " + ptr);
		}
		ptr++;

		// parse left node + branch length
		LN nl = parseNode();
		double l1 = parseBranchLength();

		skipWhitespace();


		// expect + consume ','
		if (inputA[ptr] != ',') {
			printLocation();
			throw new RuntimeException("parse error: parseInnerNode expects ',' at " + ptr);
		}
		ptr++;


		// parse right node + branch length
		LN nr = parseNode();
		double l2 = parseBranchLength();

		skipWhitespace();


		nInnerNodes++;
		if (inputA[ptr] == ')') {
			// 'normal' inner node: two childs
			ptr++;

			LN n = LN.create();

			twiddle( nl, n.next, l1 );
			twiddle( nr, n.next.next, l2 );


			return n;
		} else if( inputA[ptr] == ',') {
			// second comma found: three child nodes == pseudo root
			ptr++;

			LN nx = parseNode();
			double l3 = parseBranchLength();

			skipWhitespace();

			if( inputA[ptr] != ')' ) {
				printLocation();
				throw new RuntimeException("parse error: parseInnerNode (at root) expects ') at " + ptr);
			}
			ptr++;
			skipWhitespace();

			LN n = LN.create();

			twiddle( nl, n.next, l1 );
			twiddle( nr, n.next.next, l2 );
			twiddle( nx, n, l3 );

			return n;
		} else {
			printLocation();
			throw new RuntimeException("parse error: parseInnerNode expects ')'or ',' at " + ptr);
		}

		
	}
	private static void twiddle( LN n1, LN n2, double branchLen ) {
		if( n1.back != null ) {
			throw new RuntimeException( "n1.back != null" );
		}

		if( n2.back != null ) {
			throw new RuntimeException( "n2.back != null" );
		}

		n1.back = n2;
		n2.back = n1;

        n1.backLen = branchLen;
        n2.backLen = branchLen;


	}


	private LN parseLeaf() {


		skipWhitespace();

		// a leaf consists just of a data string. use the ':' as terminator for now (this is not correct, as there doesn't have to be a branch length (parsr will crash on tree with only one leaf...));
		int end = findNext(ptr, ':');
		String ld = input.substring(ptr, end);

		ptr = end;


		System.out.printf("leaf: %s\n", ld);
		LN n = LN.create();
		n.data.setTipName(ld);
		//n.data = ld;
		n.data.isTip = true; // fake

		nLeafs++;
		return n;
	}

	private int findNext(int pos, char c) {
		while (inputA[pos] != c) {
			pos++;
		}

		return pos;
	}

	private boolean isFloatChar(char c) {
		return Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '-';
	}

	private int findFloat(int pos) {
		while (isFloatChar(inputA[pos])) {
			pos++;
		}

		return pos;
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
		TreeParser2 tp = new TreeParser2(new File("/space/raxml/VINCENT/354.WITH.boot.tre.phy"));
	//	TreeParser2 tp = new TreeParser2("(a:1.0,b:2.0,(e:3.0,f:4.0):5.0);");
		
		//PrettyPrinter.print(n, true);
        try {
            PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream("/tmp/tree.txt")));
            LN n;// = tp.parse();
            while( ( n = tp.parse()) != null ) {
                PrettyPrinter.printRaw(n, ps );
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
