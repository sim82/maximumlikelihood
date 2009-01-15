package ml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;



public class TreeParser {

	//String input;

	// input as char array
	char[] inputA;

	// pointer to next char in input string
	int ptr = 0;

	int nLeafs = 0;
	int nInnerNodes = 0;

	// change in master branch

	public TreeParser(String input) {
	//	this.input = input;
		this.inputA = input.toCharArray();
		ptr = 0;
	}

	public TreeParser(File f) {

		this.inputA = readFile(f);
      //  this.input = new String(inputA);
        ptr = 0;
	}

	// change in new feature
	static char[] readFile(File f) {

		try {

            //BufferedReader r = new BufferedReader(new FileReader(f));
            FileReader r = new FileReader(f);
//			String line = null;
//
//            String cont = "";
//            int i = 0;
//            while( ( line = r.readLine()) != null ) {
//                cont += line;
//                System.out.printf("line %d\n", i);
//                i++;
//            }
//            return cont;
            long len = f.length();
            char[] data = new char[(int)len];

            r.read(data);
            return data;

		} catch (FileNotFoundException ex) {

			Logger.getLogger(TreeParser.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} catch (IOException ex) {
			Logger.getLogger(TreeParser.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public void printLocation() {
		int pos1 = Math.max(0, ptr - 40);
		int pos2 = Math.min(inputA.length, ptr + 40);

		System.out.println(substring(pos1, pos2));

		for (int i = pos1; i < ptr; i++) {
			System.out.print(" ");
		}
		System.out.println("^");
	}

	void skipWhitespace() {
		while ( ptr < inputA.length && Character.isWhitespace(inputA[ptr])) {// || inputA[ptr] == '\n' || inputA[ptr] == '\r') ) {
           // System.out.printf( "skip ws: %d\n", (int) inputA[ptr]);
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
        // consume terminating ;
        ptr++;
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

		double l = Double.parseDouble(substring(ptr, lend));
		ptr = lend;

		return l;
	}
    private String substring( int from, int to ) {
        return new String( Arrays.copyOfRange( inputA, from, to));
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

            double support;
            if( isFloatChar(inputA[ptr]) ) {
                int lend = findFloat(ptr);
                if (lend == ptr) {
                    printLocation();
                    throw new RuntimeException("missing float number at " + ptr);
                }

                support = Double.parseDouble(substring(ptr, lend));
                ptr = lend;
            } else {
                support = -1.0;
            }

			LN n = LN.create();
            n.data.setSupport(support);

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

	/**
	 * create an edge (=double link) between the two nodes and set branch length
	 * 
	 * @param n1
	 * @param n2
	 * @param branchLen
	 */
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
		String ld = substring(ptr, end);

		ptr = end;


	//	System.out.printf("leaf: %s\n", ld);
		LN n = LN.create();
		n.data.setTipName(ld);
		//n.data = ld;
		n.data.isTip = true; // fake

		nLeafs++;
		return n;
	}

	private int findNext(int pos, char c) {

        try {
            while (inputA[pos] != c) {
                pos++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            printLocation();
            throw new RuntimeException( "reached end of input while looking for character " + c );
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
