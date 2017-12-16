package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.engine.ConnectionCap;
import ch.sharedvd.tipi.engine.engine.ConnectionCapManager;

public class ConnectionCapInfos {

	private ConnectionCap cap;
	private ConnectionCapManager capManager;


	public ConnectionCapInfos(ConnectionCap aConnectionType, ConnectionCapManager aConnectionsCup) {
		cap = aConnectionType;
		capManager = aConnectionsCup;
	}

	// Info statique du connection cup
	public ConnectionCap getConnectionCap() {
		return cap;
	}


	public long getNbMaxConnections() {
		return cap.getNbMaxConcurrent();
	}

	public long getNbCurrentConnections() {
		return capManager.getNbCurrentConcurrent(cap.getName());
	}

}
