package networking.types;

import java.io.Serializable;

public class ProtocolWrapper extends Wrapper implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1061631379488807987L;

	private String clientVersion;
	private String protocolVersion;

	public ProtocolWrapper(String clientVersion, String protocolVersion)
	{
		this.clientVersion = clientVersion;
		this.protocolVersion = protocolVersion;
	}
	
	public ProtocolWrapper(String[] s)
	{
		clientVersion = s[0];
		protocolVersion = s[1];
	}

	public String getClientVersion()
	{
		return clientVersion;
	}

	public String getProtocolVersion()
	{
		return protocolVersion;
	}

	@Override
	public String[] getStrings()
	{
		return new String[]
		{ clientVersion, protocolVersion };
	}
}
