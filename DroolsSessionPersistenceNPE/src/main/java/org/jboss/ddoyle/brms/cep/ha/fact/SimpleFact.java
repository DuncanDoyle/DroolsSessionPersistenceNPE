package org.jboss.ddoyle.brms.cep.ha.fact;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a simple fact in the system.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class SimpleFact implements Fact {

	
	/**
	 * SerialVersionUID 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String id;
	//private final Date timestamp;
	private final long timestamp;
	
	private String status;

	public SimpleFact(Date timestamp, String status) {
		this(FactIdGenerator.generateId(), timestamp, status);
	}
	
	public SimpleFact(long timestamp, String status) {
		this(FactIdGenerator.generateId(), timestamp, status);
	}
	
	public SimpleFact(String id, Date timestamp, String status) {
			this(id, timestamp.getTime(), status);
	}
	
	public SimpleFact(String id, long timestamp, String status) {
		this.id = id;
		this.timestamp = timestamp;
		this.status = status;
	}
	

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(final String s) {
		status = s;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+" (status=" + status+", id: " + id + ", timestamp: " + new SimpleDateFormat("yyyyMMdd:HHmmssSSS").format(timestamp) + ")";
	}
	
}

