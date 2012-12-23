package me.bcfh.neoorm;

public enum NeoKey {

	NeoConfig("neo.config"), NeoDbPath("neo.path");

	private final String value;

	private NeoKey(String str) {
		value = str;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}
