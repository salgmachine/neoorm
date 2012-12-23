package me.bcfh.neoorm;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;

public class LoggerProducer {

	@Produces
	public Logger produceLogger(InjectionPoint injectionPoint) {
		return org.slf4j.LoggerFactory.getLogger(injectionPoint.getMember()
				.getDeclaringClass().getName());

	}
}
