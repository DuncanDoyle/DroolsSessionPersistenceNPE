package org.jboss.ddoyle.brms.cep.ha.fact;

import java.util.UUID;


public abstract class FactIdGenerator {

	public static String generateId() {
		//return System.nanoTime() + "-" + Math.random();
		return UUID.randomUUID().toString();
	}
}
