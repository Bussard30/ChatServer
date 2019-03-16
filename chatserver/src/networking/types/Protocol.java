package networking.types;

import java.io.Serializable;

public class Protocol implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1061631379488807987L;
	
	private String clientVersion;
	private String protocolVersion;
	
	public Protocol(String clientVersion, String protocolVersion)
	{
		this.clientVersion = clientVersion;
		this.protocolVersion = protocolVersion;
	}
	
	public String getClientVersion()
	{
		return clientVersion;
	}
	
	public String getProtocolVersion()
	{
		return protocolVersion;
	}
}
