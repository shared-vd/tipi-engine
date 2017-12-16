package ch.sharedvd.tipi.engine.engine;

import org.springframework.beans.factory.InitializingBean;

public class ConnectionCap implements InitializingBean {
	
	private boolean _default;
	private String name;
	private String description;
	private int nbMaxConcurrent;
	private ConnectionCapManager manager;

	@Override
	public void afterPropertiesSet() throws Exception {
		manager.register(this);
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDefault() {
		return _default;
	}
	public void setDefault(boolean d) {
		_default = d;
	}

	public int getNbMaxConcurrent() {
		return nbMaxConcurrent;
	}
	public void setNbMaxConcurrent(int nbMaxConcurrent) {
		this.nbMaxConcurrent = nbMaxConcurrent;
	}
	
	public void setManager(ConnectionCapManager mgr) {
		manager = mgr;
	}
	
	@Override
	public String toString() {
		return name+"(default="+_default+" max="+nbMaxConcurrent+")";
	}

}
