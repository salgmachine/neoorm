package me.bcfh.neoorm;

import java.util.Collection;

/**
 * Generic Neo4j Dao Interface
 * */
public abstract class Dao<T> {

	public abstract Collection<T> findAll();

	public abstract T findById(Object id);

	public abstract Collection<T> findByPropertyName(Object id);

	public abstract Collection<T> findByPropertyValue(Object id);

	public abstract void insertOrUpdate(T t);

	public abstract T delete(T t);
}
