package networking.types;

import java.io.Serializable;

/**
 * This class contains the number of the response,<br>
 * which is actually not in use right now,<br>
 * the Object which is being buffered,<br>
 * and the name of the response (see {@link Responses})
 * 
 * @author Bussard30
 *
 */
public class Response implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -608768784249987736L;
	protected int nr;
	protected Object buffer;
	protected String name;

	public Response(String name, Object buffer)
	{
		this.buffer = buffer;
		this.name = name;
	}

	public Response(String name, Object buffer, int nr)
	{
		this.buffer = buffer;
		this.nr = nr;
		this.name = name;
	}

	public void setNr(int nr)
	{
		this.nr = nr;
	}

	public Object getBuffer()
	{
		return buffer;
	}

	public int getNr()
	{
		return nr;
	}

	public String getName()
	{
		return name;
	}
}
