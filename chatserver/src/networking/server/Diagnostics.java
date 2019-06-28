package networking.server;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;


/**
 * Does currently not work.
 * @author Bussard30
 *
 */
public class Diagnostics
{
	private static Diagnostics instance;

	private boolean noThread;
	private volatile HashMap<ServerThread, Vector<ServerHandler>> handlers;
	private volatile HashMap<ServerHandler, InfoContainer> handlerstates;
	private volatile HashMap<ServerHandler, UptimeContainer> uptime;
	private volatile HashMap<ServerThread, Double> ranking;
	int i = 0;
	public Diagnostics()
	{
		if (instance == null)
		{
			instance = this;
		} else
		{
			throw new RuntimeException();
		}
		handlers = new HashMap<>();
		handlerstates = new HashMap<>();
		uptime = new HashMap<>();
		ranking = new HashMap<>();
		noThread = true;
		i = 0;
	}

	public synchronized void assign(ServerThread t, ServerHandler h)
	{
		if (handlers.containsKey(t))
		{
			handlers.get(t).add(h);
		} else
		{
			Vector<ServerHandler> hh = new Vector<ServerHandler>();
			hh.add(h);
			handlers.put(t, hh);
		}
	}

	public synchronized void process(ServerHandler h, boolean state)
	{
		if (state)
		{
			if (handlerstates.containsKey(h))
			{
				if (uptime.containsKey(h))
				{
					uptime.get(h).setnActiveTime(System.currentTimeMillis() - handlerstates.get(h).getTime());
				} else
				{
					uptime.put(h, new UptimeContainer(0, System.currentTimeMillis() - handlerstates.get(h).getTime()));
				}
			}
			handlerstates.put(h, new InfoContainer(true, System.currentTimeMillis()));
		} else
		{
			InfoContainer i = handlerstates.get(h);
			if (uptime.containsKey(h))
			{
				UptimeContainer uc = uptime.get(h);
				uc.setActiveTime(System.currentTimeMillis() - i.getTime());
				uptime.put(h, uc);
			} else
			{
				uptime.put(h, new UptimeContainer(System.currentTimeMillis() - i.getTime(), 0));
			}
			handlerstates.put(h, new InfoContainer(false, System.currentTimeMillis()));
		}
	}

	/**
	 * 8/8 for creative naming //actually even after deleting the method and
	 * inserting another one the statement is still true
	 */
	public void check()
	{
		for (Map.Entry<ServerThread, Vector<ServerHandler>> m0 : handlers.entrySet())
		{
			long l = 0;
			boolean b = true;
			for (ServerHandler h : m0.getValue())
			{
				if (uptime.containsKey(h))
				{
					if (uptime.get(h).getActiveTime() + uptime.get(h).getnActiveTime() < 30000)
					{
						l += uptime.get(h).getActiveTime();
					} else
					{
						b = false;
					}
				}
			}
			if (l / 30000 > 0.15 && b)
			{
				// reallocate handlers
				Server.getInstance().splitThread(m0.getKey());
			}
		}
	}

	/**
	 * 
	 * @return thread with lowest usage
	 */
	public ServerThread getThread() throws NoSuchElementException
	{
		double d = Integer.MAX_VALUE;
		for (Map.Entry<ServerThread, Double> m : ranking.entrySet())
		{
			if (m.getValue() < d)
			{
				d = m.getValue();
			}
		}

		for (Map.Entry<ServerThread, Double> m : ranking.entrySet())
		{
			if (m.getValue() == d)
				return m.getKey();
		}
		
		// return getThread(1);
		throw new NoSuchElementException();
	}

	/**
	 * just a loop protection
	 * 
	 * @param i+
	 * @return
	 */
	private ServerThread getThread(int i)
	{
		if (i > 3)
			throw new RuntimeException();
		double d = Integer.MAX_VALUE;
		for (Map.Entry<ServerThread, Double> m : ranking.entrySet())
		{
			if (m.getValue() < d)
				d = m.getValue();
		}

		for (Map.Entry<ServerThread, Double> m : ranking.entrySet())
		{
			if (m.getValue() == d)
				return m.getKey();
		}
		return getThread(i++);
	}

	private class InfoContainer
	{
		private boolean b;
		private long l;

		public InfoContainer(boolean b, long l)
		{
			this.b = b;
			this.l = l;
		}

		public boolean getState()
		{
			return b;
		}

		public long getTime()
		{
			return l;
		}
	}

	public void printStuff()
	{
//		Logger.info("Printing stuff");
//		for(Map.Entry<ServerHandler, UptimeContainer> m : uptime.entrySet())
//		{
//			System.out.println(m.getValue().getActiveTime());
//			System.out.println(m.getValue().getnActiveTime());
//		}
	}
	
	private class UptimeContainer
	{
		private long activeTime, nActiveTime;
		private long aActiveTime, anActiveTime;
		// running for more than 30 secs
		private boolean isInitiliazed;

		public UptimeContainer(long activeTime, long nActiveTime)
		{
			this.activeTime = activeTime;
			this.nActiveTime = nActiveTime;
			this.isInitiliazed = false;
		}

		public long getnActiveTime()
		{
			return anActiveTime;
		}

		public void setnActiveTime(long nActiveTime)
		{
			this.nActiveTime += nActiveTime;
			if (this.nActiveTime > 1000)
			{
				if (this.aActiveTime + this.anActiveTime > 30000)
				{
					this.aActiveTime -= this.activeTime;
					this.nActiveTime += this.activeTime;
				} else
				{
					this.anActiveTime += this.nActiveTime;
				}
			}
		}

		public long getActiveTime()
		{
			return aActiveTime;
		}

		public void setActiveTime(long activeTime)
		{
			this.activeTime += activeTime;
			if (this.activeTime > 1000)
			{
				if ((this.aActiveTime + this.anActiveTime) > 30000)
				{
					if((aActiveTime | anActiveTime ) > 30000)
					{
						this.aActiveTime += this.activeTime;
						this.anActiveTime -= this.activeTime;
						this.activeTime = 0;
						this.isInitiliazed = true;
					}
					else
					{
						if(aActiveTime > 30000)
						{
							
						}
					}
				} else
				{
					this.aActiveTime += this.activeTime;
					this.activeTime = 0;
				}
			}

		}

		public boolean isInitiliazed()
		{
			return isInitiliazed;
		}

	}

	public static Diagnostics getInstance()
	{
		return instance;
	}
}
