package networking.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

import main.main.Main;
import networking.logger.Logger;
import networking.types.Gate;
import networking.types.MessageWrapper;

public class Server
{
	private Thread acceptorThread;
	private Thread distributorThread;

	private ServerSocket serverSocket;
	private boolean online;

	private static Server server;

	// private int threadAmount;

	private Vector<ServerThread> threads;

	private volatile Vector<ServerHandler> handlers;
	private volatile Vector<ServerHandler> unassignedHandlers;
	private volatile HashMap<ServerThread, Vector<ServerHandler>> assignments;
	private volatile HashMap<String, ServerHandler> uuidAssignments;
	private volatile HashMap<byte[], MessageWrapper> messages;
	private volatile HashMap<Long, Gate> voice;

	private volatile Vector<Integer> numbers;
	public Object dsmlock = new Object();

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
		messages = new HashMap<byte[], MessageWrapper>();
		voice = new HashMap<>();
		log("Initialized server.");
	}

	public static Server getInstance()
	{
		return server;
	}

	@Deprecated
	public void startServer()
	{
		synchronized (dsmlock)
		{
			try
			{
				dsmlock.wait();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
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
		acceptorThread.setName("Connection-negotiator");
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
		acceptorThread.setName("Connection-negotiator");
		acceptorThread.start();
		distributorThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				/**
				 * to be removed
				 */
				Vector<ServerHandler> tbr = new Vector<>();

				while (online)
				{
					unassignedHandlers.forEach(h ->
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
					});
					tbr.forEach(h -> unassignedHandlers.remove(h));
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
		distributorThread.setName("Connection-distributor");
		distributorThread.start();

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (online)
				{
//					Logger.info("Doing something");
//					Field[] attributes = server.getClass().getDeclaredFields();
//					Logger.info("Count" + attributes.length);
//					for (Field field : attributes)
//					{
//						// Dynamically read Attribute Name
//						System.out.print("ATTRIBUTE NAME: " + field.getName());
//
//						try
//						{
//							Object o = new Object();
//							field.get(o);
//							printObjectSize(o);
//							System.out.println();
//						} catch (Exception e)
//						{
//							System.out.println("<ERROR;Could not retrieve object.>");
//						}
//
//					}
					
//			        System.out.println(VM.current().details());
//			        assignments.forEach((k,v)-> v.forEach(h -> System.out.println(ClassLayout.parseClass(h.getClass()).toPrintable())));
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

	public String generateToken()
	{
		byte[] array = new byte[32];
		new Random().nextBytes(array);
		return Base64.getEncoder().encodeToString(array);
	}

	public void startServer(int port, boolean multithreading)
	{
		if (multithreading)
		{
			// TODO
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

	public void queueMessageForUUID(byte[] uuid, MessageWrapper message)
	{
		messages.put(uuid, message);
	}

	public MessageWrapper messageDueForUUID(byte[] uuid) throws NoSuchElementException
	{
		if (messages.containsKey(uuid))
		{
			MessageWrapper m = messages.get(uuid);
			messages.remove(uuid);
			return m;
		} else
		{
			throw new NoSuchElementException();
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
		assignments.forEach((t, v) ->
		{
			if (v.contains(sh))
				v.remove(sh);
		});
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
