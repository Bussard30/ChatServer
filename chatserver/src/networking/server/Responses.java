package networking.server;

import java.security.PublicKey;

import networking.types.LoginResponseWrapper;
import networking.types.MessageWrapper;
import networking.types.ProfileInfoWrapper;
import networking.types.ProtocolWrapper;
import networking.types.UserVectorWrapper;

public enum Responses
{
	// RESPONSE TO RSA KEY (exchange)
	RSP_RSAKEY("RSP_RSAKEY", NetworkPhases.PRE0, PublicKey.class),

	// // RESPONSE TO AES KEY (exchange)
	// RSP_AESKEY("RSP_AESKEY", NetworkPhases.PRE0, Object.class),

	// RESPONSE TO PROTOCOL
	RSP_PROTOCOL("RSP_PROTOCOL", NetworkPhases.PRE1, ProtocolWrapper.class),

	// TRANSMIT (user) CREDENTIALS, responds with new token
	RSP_CREDS("RSP_CREDS", NetworkPhases.PRE2, LoginResponseWrapper.class),

	// TRANSMIT (user) CREDENTIALS, responds with new token
	RSP_TOKEN("RSP_TOKEN", NetworkPhases.PRE2, LoginResponseWrapper.class),

	RCV_MESSAGE("RCV_MESSAGE", NetworkPhases.COM, MessageWrapper.class),

	RSP_DATA("RSP_DATA", NetworkPhases.COM, ProfileInfoWrapper.class),

	USER_QUERY("USER_QUERY", NetworkPhases.COM, UserVectorWrapper.class),
	
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
