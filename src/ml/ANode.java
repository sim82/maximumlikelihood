package ml;

public class ANode {

	boolean isTip;
	private String tipName;
    private double support;
	public void setTipName(String name) {
		assert( isTip && name != null );
		tipName = name;
	}

	public String getTipName() {
		assert( isTip );
		return tipName;
	}

    void setSupport(double support) {
        this.support = support;
    }

    double getSupport() {
        return support;
    }
}



