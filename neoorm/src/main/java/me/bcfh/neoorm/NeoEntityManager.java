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
import javax.inject.Qualifier;


/**
 * This is the Annotation marking a NeoEntityManager (NeoORM) instance
 * 
 * @author salgmachine
 * @version 0.5.0
 */
@Target(value = { METHOD, TYPE, FIELD })
@Retention(value = RUNTIME)
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Qualifier
public @interface NeoEntityManager {

	/**
	 * This String can point to a neo.properties file or a directory
	 */
	String neopath() default "";

}