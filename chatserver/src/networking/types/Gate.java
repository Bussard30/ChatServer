package networking.types;

import networking.server.ServerHandler;

public class Gate
{
	private ServerHandler h0, h1;

	public Gate(ServerHandler h0, ServerHandler h1)
	{
		this.h0 = h0;
		this.h1 = h1;
	}
	
	public ServerHandler getFirstHandler()
	{
		return h0;
	}
	
	public ServerHandler getSecondHandler()
	{
		return h1;
	}
	
	/**
	 * Sends request from ServerHandler h to other ServerHandler
	 * Please detail further future me
	 * @param h 
	 * @param r
	 */
	public void sendRequestFrom(ServerHandler h, Request r)
	{
		try
		{
			((h == h0) ? h1 : h0).send(r);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends request from ServerHandler h to other ServerHandler
	 * @param h
	 * @param r
	 */
	public void sendResponseFrom(ServerHandler h, Response r)
	{
		try
		{
			((h == h0) ? h1 : h0).send(r);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
