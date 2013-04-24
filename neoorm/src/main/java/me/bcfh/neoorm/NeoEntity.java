package me.bcfh.neoorm;

import jo4neo.Nodeid;

/**
 * This is the basic NeoEntity class, all Node Classes extend from this.<br/>
 * It holds the {@link Nodeid} for each node class
 * */
public abstract class NeoEntity {

	public transient Nodeid neo;

	public abstract boolean containsPropertyValue(String value);
}
