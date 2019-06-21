package networking.server;

import java.security.PublicKey;

import networking.types.AESKeyWrapper;
import networking.types.CredentialsWrapper;
import networking.types.MessageWrapper;
import networking.types.ProtocolWrapper;
import networking.types.SearchUserWrapper;
import networking.types.TokenWrapper;

public enum Requests
{
	// TRANSMIT PUBLIC KEY
	TRSMT_RSAKEY("TRSMT_RSAKEY", NetworkPhases.PRE0, PublicKey.class),

	// TRANSMIT AES KEY
	TRSMT_AESKEY("TRSMT_AESKEY", NetworkPhases.PRE0, AESKeyWrapper.class),

	// TRANSMIT PROTOCOL
	TRSMT_PROTOCOL("TRSMT_PROTOCOL", NetworkPhases.PRE1, ProtocolWrapper.class),

	// TRANSMIT (user) CREDENTIALS
	TRSMT_CREDS("TRSMT_CREDS", NetworkPhases.PRE2, CredentialsWrapper.class),

	// TRANSMIT TOKEN
	TRSMT_TOKEN("TRSMT_TOKEN", NetworkPhases.PRE2, TokenWrapper.class),

	TRSMT_MESSAGE("TRSMT_MESSAGE", NetworkPhases.COM, MessageWrapper.class),

	REQST_DATA("REQST_DATA", NetworkPhases.COM, Object.class),
	
	SEARCH_USER("SEARCH_USER", NetworkPhases.COM, SearchUserWrapper.class),

	;

	private final String name;
	private final NetworkPhases np;
	private final Class<?> type;

	Requests(String name, NetworkPhases np, Class<?> type)
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
