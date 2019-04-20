package networking.server;

import java.security.PublicKey;

import networking.types.Protocol;

public enum Responses
{
	//RESPONSE TO KEY (exchange)
	RSP_KEY("RSP_KEY", NetworkPhases.PRE0, PublicKey.class),
	
	//RESPONSE TO PROTOCOL
	RSP_PROTOCOL("RSP_PROTOCOL", NetworkPhases.PRE1, Protocol.class),
	
	// TRANSMIT (user) CREDENTIALS, responds with new token
	RSP_CREDS("RSP_CREDS", NetworkPhases.PRE2, String.class),
	
	// TRANSMIT (user) CREDENTIALS, responds with new token
	RSP_TOKEN("RSP_TOKEN", NetworkPhases.PRE2, String.class),
	
	RCV_MESSAGE("RCV_MESSAGE", NetworkPhases.COM, Boolean.class),
	
	;
	private final String name;
	private final NetworkPhases np;
	private final Class<?> type;

	Responses(String name, NetworkPhases np, Class<?> type)
	{
		this.np = np;
		this.type = type;
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public NetworkPhases getNPhase()
	{
		return np;
	}

	public Class<?> getType()
	{
		return type;
	}
}
