package networking.types;

import java.io.Serializable;

public class Request implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8977692225934536189L;

	protected int nr;
	protected Object buffer;
	protected String name;


	public Request(String name, Object buffer)
	{
		this.buffer = buffer;
		this.name = name;
	}
	
	public Request(String name, Object buffer, int nr)
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
