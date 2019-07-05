package networking.server;

import java.security.PublicKey;

import com.mysql.cj.protocol.Protocol;

import networking.types.LoginResponseWrapper;
import networking.types.MessageWrapper;
import networking.types.ProfileInfoWrapper;
import networking.types.ProtocolWrapper;
import networking.types.UserVectorWrapper;

public enum Responses
{

	/**
	 * Response to {@link Requests.TRSMT_RSAKEY}.<br>
	 * Holds a RSA public key.
	 * 
	 * @see PublicKey
	 */
	RSP_RSAKEY("RSP_RSAKEY", NetworkPhases.PRE0, PublicKey.class),

	/**
	 * Response to {@link Requests.TRSMT_PROTOCOL}.<br>
	 * Holds client and protocol version of server.
	 * 
	 * @see Protocol
	 */
	RSP_PROTOCOL("RSP_PROTOCOL", NetworkPhases.PRE1, ProtocolWrapper.class),

	/**
	 * Response to {@link Requests.TRSMT_CREDS}<br>
	 * Holds token and whether client is logged in or not.
	 * 
	 * @see LoginResponseWrapper
	 */
	RSP_CREDS("RSP_CREDS", NetworkPhases.PRE2, LoginResponseWrapper.class),

	/**
	 * Response to {@link Requests.TRSMT_TOKEN}.<br>
	 * Holds (new) token and whether client is logged in or not.
	 * 
	 * @see LoginResponseWrapper
	 */
	RSP_TOKEN("RSP_TOKEN", NetworkPhases.PRE2, LoginResponseWrapper.class),

	/**
	 * Response to {@link Requests.TRSMT_MESSAGE}.<br>
	 * Holds message id which has been successfully received by the server or
	 * received and/or read by the destination.
	 * 
	 * @see MessageWrapper
	 */
	RCV_MESSAGE("RCV_MESSAGE", NetworkPhases.COM, MessageWrapper.class),

	/**
	 * Response to {@link Requests.REQST_DATA}.<br>
	 * Holds all information required to load the chatclient.
	 * 
	 * @see ProfileInfoWrapper
	 */
	RSP_DATA("RSP_DATA", NetworkPhases.COM, ProfileInfoWrapper.class),

	/**
	 * Response to {@link Requests.SEARCH_USER}. Holds a vector of different
	 * users.
	 * 
	 * @see UserVectorWrapper
	 */
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
