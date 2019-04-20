package networking.types;

public class MessageWrapper
{
	private String message, source, destination;
	private boolean received, read;
	private int id;

	public MessageWrapper(String message, String source, String destination)
	{
		this(message, source, destination, false, false, -1);
	}

	public MessageWrapper(String message, String source, String destination, boolean received, boolean read, int id)
	{
		this.message = message;
		this.source = source;
		this.destination = destination;
		this.received = received;
		this.id = id;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public boolean isReceived()
	{
		return received;
	}

	public void setReceived(boolean received)
	{
		this.received = received;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

}
