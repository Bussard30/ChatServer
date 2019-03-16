package networking.server;

import java.util.concurrent.TimeUnit;

import networking.logger.Logger;

/**
 * Workaround for Thread not being able to access itself
 * 
 * @author Jonas
 *
 */
public class ServerThread
{
	private String name;
	private Thread t;
	private ServerThread st;

	public ServerThread()
	{
		name = "SERVERTHREAD";
		st = this;
		run();
	}

	public ServerThread(String name)
	{
		this.name = name;
		st = this;
		run();
	}
	public void run()
	{
		t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(Server.getInstance().isOnline())
				{
					for(ServerHandler h : Server.getInstance().getAssigments(st))
					{
						try
						{
							Diagnostics.getInstance().process(h, true);
							h.run();
							try
							{
								Thread.sleep(0, 500000);
							} catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Diagnostics.getInstance().process(h, false);
						} catch (Exception e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try
					{
						Thread.sleep(0, 500000);
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		t.start();
	}
}
