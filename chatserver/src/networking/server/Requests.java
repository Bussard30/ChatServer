package networking.server;

import java.security.PublicKey;

import networking.types.AESKeyWrapper;
import networking.types.AudioWrapper;
import networking.types.ByteArrayWrapper;
import networking.types.CredentialsWrapper;
import networking.types.MessageWrapper;
import networking.types.ProtocolWrapper;
import networking.types.SearchUserWrapper;
import networking.types.TokenWrapper;
import networking.types.Wrapper;

public enum Requests
{

	/**
	 * Transmission of a RSA public key used to decrypt the AES key<br>
	 * which is required for encrypted data transmission.<br>
	 * Response: {@link Responses.RSP_RSAKEY}
	 */
	TRSMT_RSAKEY("TRSMT_RSAKEY", NetworkPhases.PRE0, PublicKey.class),

	/**
	 * Transmission of a AES secret key used to decrypt and encrypt<br>
	 * the network traffic.<br>
	 * Response: null
	 */
	TRSMT_AESKEY("TRSMT_AESKEY", NetworkPhases.PRE0, AESKeyWrapper.class),

	/**
	 * Transmission of protocol version to check if updates are required.<br>
	 * Holds client and protocol version of client.<br>
	 * Response: {@link Responses.RSP_PROTOCOL}
	 */
	TRSMT_PROTOCOL("TRSMT_PROTOCOL", NetworkPhases.PRE1, ProtocolWrapper.class),

	/**
	 * Transmission of login credentials to log in.<br>
	 * Response: {@link Responses.RSP_CREDS}
	 */
	TRSMT_CREDS("TRSMT_CREDS", NetworkPhases.PRE2, CredentialsWrapper.class),

	/**
	 * Transmission of a token to log in.<br>
	 * Response: {@link Responses.RSP_TOKEN}
	 */
	TRSMT_TOKEN("TRSMT_TOKEN", NetworkPhases.PRE2, TokenWrapper.class),

	/**
	 * Transmission of a message between two clients.<br>
	 * Response: {@link Responses.RCV_MESSAGE}
	 */
	TRSMT_MESSAGE("TRSMT_MESSAGE", NetworkPhases.COM, MessageWrapper.class),

	/**
	 * Request for data required to load the chatclient interface. <br>
	 * This request does not contain any data.<br>
	 * Response: {@link Responses.RSP_DATA}
	 */
	REQST_DATA("REQST_DATA", NetworkPhases.COM, Object.class),

	/**
	 * Request to search for user for the auto-complete friend search function.<br>
	 * Response: {@link Responses.USER_QUERY}
	 */
	SEARCH_USER("SEARCH_USER", NetworkPhases.COM, SearchUserWrapper.class),

	TRSMT_AUDIO("TRSMT_AUDIO", NetworkPhases.COM, AudioWrapper.class),
	
	REQST_PING("REQST_PING", NetworkPhases.COM, ByteArrayWrapper.class),
	;

	private final String name;
	private final NetworkPhases np;
	private final Class<?> type;

	/**
	 * 
	 * @param name
	 *            Name of Request used to network transmission
	 * @param np
	 *            Networkphase in which these requests are being used
	 * @param type
	 *            Type of data, e.g. String or {@link Wrapper}
	 */
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
