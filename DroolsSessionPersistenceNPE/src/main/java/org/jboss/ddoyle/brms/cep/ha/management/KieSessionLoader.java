package org.jboss.ddoyle.brms.cep.ha.management;

import org.kie.api.runtime.KieSession;

/**
 * Serializes the WorkingMemory and stores it to be reloaded, possibly by another (CEP) node.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface KieSessionLoader {

	public abstract void save(KieSession kieSession);
	
	public abstract KieSession load();

}
