package me.bcfh.neoorm;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Stereotype;
import javax.inject.Qualifier;

/**
 * This is the Annotation marking a NeoEntityManager (NeoORM) instance
 * 
 * @author paul
 * @version 0.9
 * 
 */
@Target(value = { METHOD, TYPE, FIELD })
@Retention(value = RUNTIME)
@Stereotype
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Qualifier
public @interface NeoEntityManager {

	/**
	 * This String can point to a neo.properties file or a directory
	 * */
	String neopath() default "";

}