package org.jboss.ddoyle.brms.cep.ha.fact;

import java.io.Serializable;
import java.util.Date;


/**
 * Interface for all facts in the ERDF rules engine. Allows to retrieve the <code>id</code> of a fact. The <code>id</code> is an important
 * feature in this HA system as it is used when generating a unique, but deterministic, <code>id</code> for a {@link Command}. This allows
 * us to create the same ID on each BRMS HA node, which allows us to implement equality of {@link Command Commands} over multiple BRMS
 * processing engines. This in turn allows us to implement an idempotency layer which is responsible for executing the same {@link Command}
 * only once.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface Fact extends Serializable {
	
	/**
	 * @return the <code>id</code> of this fact.
	 */
	public String getId();
	
	public long getTimestamp();
	
	
}
