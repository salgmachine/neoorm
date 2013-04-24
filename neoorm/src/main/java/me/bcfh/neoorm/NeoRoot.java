package me.bcfh.neoorm;

import jo4neo.Nodeid;
import jo4neo.neo;

public class NeoRoot {
	public transient Nodeid neo;

	@neo
	private final int id = 0;

	public int getId() {
		return id;
	}

}
