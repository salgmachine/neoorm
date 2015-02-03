package me.bcfh.neoorm;

import jo4neo.Nodeid;


/**
 * This is the basic NeoEntity class, all Node Classes extend from this.<br/>
 * It holds the {@link Nodeid} for each node class
 * 
 * @author salgmachine
 * @version 0.5.0
 */
public abstract class NeoEntity {

	public transient Nodeid neo;

	public boolean containsPropertyValue(String value) {
		return false;
	}
}
