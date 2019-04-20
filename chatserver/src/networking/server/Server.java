package networking.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

import main.main.Main;
import networking.logger.Logger;
import networking.types.MessageWrapper;

public class Server
{
	private Thread acceptorThread;
	private Thread distributorThread;

	private ServerSocket serverSocket;
	private boolean online;

	private BufferedReader in;
	private PrintWriter out;
	private boolean passwordRequired;

	private static PrintWriter writer;

	private static Server server;

	private int threadAmount;

	private Vector<ServerThread> threads;

	private volatile Vector<ServerHandler> handlers;
	private volatile Vector<ServerHandler> unassignedHandlers;
	private volatile HashMap<ServerThread, Vector<ServerHandler>> assignments;
	private volatile HashMap<String, ServerHandler> uuidAssignments;
	private volatile HashMap<String, MessageWrapper> messages;

	private volatile Vector<Integer> numbers;

	/**
	 * Creates a new server with a dynamic amount of threads
	 * 
	 */
	public Server()
	{
		log("Initializing server...");
		server = this;
		threads = new Vector<ServerThread>();
		handlers = new Vector<ServerHandler>();
		unassignedHandlers = new Vector<>();
		assignments = new HashMap<ServerThread, Vector<ServerHandler>>();
		uuidAssignments = new HashMap<String, ServerHandler>();
		messages = new HashMap<String, MessageWrapper>();
		log("Initialized server.");
	}

	public static Server getInstance()
	{
		return server;
	}

	@Deprecated
	public void startServer()
	{
		// starts server thread
		acceptorThread = new Thread()
		{
			@Override
			public void run()
			{
				this.setName("Server Thread");
				online = true;
				try
				{
					serverSocket = new ServerSocket(Main.port);

				} catch (IOException e)
				{
					log(e);
					e.printStackTrace();
				}

				while (online)
				{

					Socket s;
					try
					{
						s = serverSocket.accept();
						if (s != null)
						{
							log(s.getInetAddress().getHostAddress() + " connected.");
							ServerHandler sh = new ServerHandler(s, new DataInputStream(s.getInputStream()),
									new DataOutputStream(s.getOutputStream()));
							handlers.addElement(sh);
							// register(sh);
						}
					} catch (IOException e)
					{
						log(e);
						e.printStackTrace();
					}

				}

				// shutdown

				try
				{
					serverSocket.close();
				} catch (IOException e)
				{
					log(e);
					e.printStackTrace();
				}
			}
		};
		acceptorThread.start();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (online)
				{
					for (ServerHandler h : handlers)
					{
						try
						{
							if (h.getInputStream().available() > 0)
							{
								try
								{
									h.run();
								} catch (Exception e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (IOException e)
						{
							e.printStackTrace();
						}

					}
				}
				try
				{
					this.finalize();
				} catch (Throwable e)
				{
					e.printStackTrace();
				}
			}

		}).start();
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (online)
				{
					Diagnostics.getInstance().printStuff();
				}
				try
				{
					Thread.sleep(1500);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 
	 * @param port
	 */
	public void startServer(int port)
	{
		log("Starting Server ...");
		// starts server thread
		acceptorThread = new Thread()
		{
			@Override
			public void run()
			{
				this.setName("Server Thread");
				online = true;
				try
				{
					serverSocket = new ServerSocket(port);

				} catch (IOException e)
				{
					log(e);
					e.printStackTrace();
				}

				while (online)
				{

					Socket s;
					try
					{
						s = serverSocket.accept();
						log(s.getInetAddress().getHostAddress() + " connected.");
						ServerHandler sh = new ServerHandler(s, new DataInputStream(s.getInputStream()),
								new DataOutputStream(s.getOutputStream()));
						handlers.addElement(sh);
						unassignedHandlers.addElement(sh);
					} catch (IOException e)
					{
						log(e);
						e.printStackTrace();
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

				// shutdown

				try
				{
					serverSocket.close();
				} catch (IOException e)
				{
					log(e);
					e.printStackTrace();
				}
			}
		};
		acceptorThread.start();
		distributorThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Vector<ServerHandler> tbr = new Vector<>();
				while (online)
				{
					for (ServerHandler h : unassignedHandlers)
					{
						try
						{
							assignments.get(Diagnostics.getInstance().getThread()).add(h);
						} catch (NoSuchElementException e)
						{
							log("First handler is being assigned ...");
							ServerThread st = new ServerThread();
							Vector<ServerHandler> hs = new Vector<ServerHandler>();
							hs.add(h);

							Diagnostics.getInstance().assign(st, h);

							threads.add(st);
							assignments.put(st, hs);
						}

						tbr.add(h);
						log("Assigned ServerHandler to state: UNASSIGNED");
					}
					for (ServerHandler h : tbr)
					{
						unassignedHandlers.remove(h);
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
		distributorThread.start();
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (online)
				{
					Diagnostics.getInstance().printStuff();
					try
					{
						Thread.sleep(1500);
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}).start();
		log("Done.");
	}

	public String generateToken(int n)
	{
		byte[] array = new byte[256];
		new Random().nextBytes(array);
		String randomString = new String(array, Charset.forName("UTF-8"));
		StringBuffer r = new StringBuffer();
		for (int k = 0; k < randomString.length(); k++)
		{
			char ch = randomString.charAt(k);

			if (((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) && (n > 0))
			{
				r.append(ch);
				n--;
			}
		}
		return r.toString();
	}

	public void startServer(int port, boolean multithreading)
	{
		if (multithreading)
		{

		}
	}

	public boolean isOnline()
	{
		return online;
	}

	public ServerHandler getHandlerByUUID(String uuid)
	{
		return uuidAssignments.get(uuid);
	}

	public void register(String uuid, ServerHandler sh)
	{
		uuidAssignments.put(uuid, sh);
	}

	public int getIndex()
	{
		int i = 0;
		for (; !numbers.contains(i); i++)
			;
		return i;
	}

	public void queueMessageForUUID(MessageWrapper message)
	{
		// TODO
	}

	public MessageWrapper messageDueForUUID(String uuid)
	{
		if(messages.containsKey(uuid))
		{
			return messages.get(uuid);
		}
		else
		{
			return null;
		}
	}

	public void splitThread(ServerThread t)
	{
		int i = 0;
		ServerThread st = new ServerThread();
		threads.add(st);
		assignments.put(st, new Vector<ServerHandler>());
		for (ServerHandler h : assignments.get(t))
		{
			if (i++ % 2 == 0)
			{
				assignments.get(t).remove(h);
				assignments.get(st).add(h);
			}
		}
	}

	public void closeThread(ServerThread t)
	{

	}

	public void closeHandler(ServerHandler sh)
	{
		// TODO
	}

	public Vector<ServerHandler> getAssigments(ServerThread t)
	{
		return assignments.get(t);
	}

	private void log(String s)
	{
		Logger.info("SERVER", s);
	}

	private void log(Throwable t)
	{
		Logger.error("SERVER", t);
	}

	public void overloadDetected(ServerHandler s)
	{

	}

}
