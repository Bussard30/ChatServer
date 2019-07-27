package networking.types;

import java.io.Serializable;
import java.util.Base64;

/**
 * Used to hold a message between two clients.<br>
 * Contains <b>message</b>, <b>source</b>, <b>destination</b>,<br>
 * whether the message has been <b>received</b> by the server or not,<br>
 * whether the message has been <b>read</b> by the other client and the
 * <b>message id</b>
 * 
 * @author Bussard30
 *
 */
public class MessageWrapper extends Wrapper implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5667179476964469748L;
	private String message;
	private byte[] source, destination;
	private boolean received, receivedByDest, read;
	private int id;

	/**
	 * Creates MessageWrapper object.
	 * 
	 * @param message
	 * @param source
	 * @param destination
	 */
	public MessageWrapper(String message, byte[] source, byte[] destination)
	{
		this(message, source, destination, false, false, false, -1);
	}

	public MessageWrapper(MessageWrapper m, boolean received)
	{
		this(m.getMessage(), m.getSource(), m.getDestination(), received, m.receivedByDest(), m.read(), m.getId());
	}

	public MessageWrapper(MessageWrapper m, boolean receivedByDest, boolean read)
	{
		this(m.getMessage(), m.getSource(), m.getDestination(), m.received(), receivedByDest, read, m.getId());
	}

	/**
	 * Creates MessageWrapper object.
	 * 
	 * @param message
	 * @param source
	 * @param destination
	 * @param received
	 * @param read
	 * @param id
	 */
	public MessageWrapper(String message, byte[] source, byte[] destination, boolean received, boolean receivedByDest,
			boolean read, int id)
	{
		this.message = message;
		this.source = source;
		this.destination = destination;
		this.received = received;
		this.id = id;
		this.receivedByDest = receivedByDest;
	}

	/**
	 * Creates CredentialsWrapper object with String obtained by
	 * {@link #getStrings()}
	 * 
	 * @param s
	 */
	public MessageWrapper(String[] s)
	{
		if(s.length != 7) throw new RuntimeException("Too many parameters(" + s.length + ")");
		message = s[0];
		source = Base64.getDecoder().decode(s[1]);
		destination = Base64.getDecoder().decode(s[2]);
		received = s[3].equals("true") ? true : false;
		receivedByDest = s[4].equals("true") ? true : false;
		read = s[5].equals("true") ? true : false;
		try
		{
			id = Integer.parseInt(s[6]);
		} catch (Throwable t)
		{
			throw new RuntimeException("Parameter 5 cannot be converted to an int");
		}
	}

	public String getMessage()
	{
		return message;
	}

	public byte[] getSource()
	{
		return source;
	}

	public byte[] getDestination()
	{
		return destination;
	}

	public boolean received()
	{
		return received;
	}

	public boolean read()
	{
		return read;
	}

	public boolean receivedByDest()
	{
		return receivedByDest;
	}

	public int getId()
	{
		return id;
	}

	/**
	 * @return String array required for {@link #CredentialsWrapper(String[])}
	 */
	@Override
	public String[] getStrings()
	{
		return new String[]
		{ message, Base64.getEncoder().encodeToString(source), Base64.getEncoder().encodeToString(destination),
				received ? "true" : "false", receivedByDest ? "true" : "false", read ? "true" : "false",
				String.valueOf(id) };
	}

}
