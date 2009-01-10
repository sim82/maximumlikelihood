package ml;


public class LN {

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
