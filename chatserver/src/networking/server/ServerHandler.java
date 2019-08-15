package networking.server;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import datastorage.main.DSManager;
import main.main.Main;
import main.types.User;
import networking.exceptions.BadPacketException;
import networking.logger.Logger;
import networking.types.AESKeyWrapper;
import networking.types.ByteArrayWrapper;
import networking.types.CredentialsWrapper;
import networking.types.LoginResponseWrapper;
import networking.types.MessageWrapper;
import networking.types.ProfileInfoWrapper;
import networking.types.ProtocolWrapper;
import networking.types.Request;
import networking.types.Response;
import networking.types.SearchUserWrapper;
import networking.types.UserVectorWrapper;
import networking.types.Wrapper;

/**
 * Handles incoming traffic for one client.<br>
 * Initiated by {@link Server}<br>
 * Run method executed by {@link ServerThread}<br>
 * 
 * @author Bussard30
 *
 */
public class ServerHandler
{
	private Socket s;

	private boolean queueEmpty;

	private long millis;

	private DataInputStream in;
	private DataOutputStream out;

	private KeyPair kp;
	private PublicKey pub;
	private PrivateKey pvt;

	private PublicKey pub1;

	private NetworkPhases phase;

	private int current = 0;

	private HashMap<NetworkPhases, boolean[]> networkphaseprogress;

	private User u;

	private SecretKey key;

	private long lastPing;
	private boolean pinging;
	private byte[] ping;

	/**
	 * Generates new server handler to handle socket
	 * 
	 * @param s
	 */
	public ServerHandler(Socket s)
	{
		this.s = s;
		KeyPairGenerator kpg = null;
		try
		{
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		kpg.initialize(3072);
		kp = kpg.generateKeyPair();
		pub = kp.getPublic();
		pvt = kp.getPrivate();
		networkphaseprogress = new HashMap<NetworkPhases, boolean[]>();
		phase = NetworkPhases.PRE0;
		networkphaseprogress.put(phase, new boolean[5]);
		for (int i = 0; i < networkphaseprogress.get(phase).length; i++)
		{
			networkphaseprogress.get(phase)[i] = false;
		}
		try
		{
			s.setSoTimeout(3000);
		} catch (SocketException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			s.setKeepAlive(false);
		} catch (SocketException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lastPing = System.currentTimeMillis();
		
		ping = new byte[32];
		new Random().nextBytes(ping);
	}

	public ServerHandler(Socket s, DataInputStream in, DataOutputStream out)
	{
		this(s);
		this.out = out;
		this.in = in;
	}

	boolean bbbb = false;

	/**
	 * <h1>Connectivity Check</h1> First of all it checks if the server is
	 * already closed<br>
	 * and if that's true it closes the Handler<br>
	 * <h1>Deserialization</h1> First it checks if the queue is empty.<br>
	 * If not, it's going to read an 32-bit integer,<br>
	 * which is the length of the incoming packet.<br>
	 * Then it gets decrypted with the AES key (see {@link #key} &<br>
	 * {@link #decrypt(SecretKey, byte[])}).<br>
	 * After that it get deserialized (see {@link #deserialize(byte[])}).<br>
	 * <h1>Interpretation</h1> Now depending on the type of request/response
	 * and/or the type of the object<br>
	 * a different response will be triggered. <br>
	 * (see {@link Requests} and {@link Responses})
	 * <h1>The end</h1> Now it is being checked if the conditions for
	 * advancing<br>
	 * to the next network phase have been met.<br>
	 * It also may initiate new requests/responses depending on the network
	 * phase.<br>
	 * (see {@link NetworkPhases})
	 * 
	 * @throws Exception
	 * @see Wrapper
	 * @see Request
	 * @see Requests
	 * @see Response
	 * @see Responses
	 * @see NetworkPhases
	 * @see Server
	 */
	public void run() throws Exception
	{

		if (s.isClosed() || !s.isConnected() || s.isInputShutdown() || s.isOutputShutdown())
		{
			s.close();
			Logger.info(s.getInetAddress().getHostAddress() + " has disconnected.");
			Server.getInstance().closeHandler(this);
			return;
		}
		if (queueEmpty == true)
		{
			queueEmpty = false;
			millis = System.currentTimeMillis();
		} else
		{
			if (millis - System.currentTimeMillis() > 50)
			{
				Server.getInstance().overloadDetected(this);
			}
		}
		byte b[] = null;

		if (System.currentTimeMillis() - lastPing > 5000 && !pinging)
		{
			ping();
			lastPing = System.currentTimeMillis();
			pinging = true;
		} else if (System.currentTimeMillis() - lastPing > 10000 && pinging)
		{
			s.close();
			Logger.info(s.getInetAddress().getHostAddress() + " has disconnected.");
			Server.getInstance().closeHandler(this);
			return;
		}

		while (in.available() != 0)
		{
			Logger.info(s.getInetAddress().getHostAddress(), "Received bytes...");
			try
			{
				int ch1 = in.read();
				int ch2 = in.read();
				int ch3 = in.read();
				int ch4 = in.read();
				if ((ch1 | ch2 | ch3 | ch4) < 0)
				{
					s.close();
					Logger.info(s.getInetAddress().getHostAddress() + " has disconnected.");
					Server.getInstance().closeHandler(this);
					// throw new EOFException();
					return;
				}
				int length = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
				b = new byte[length];
				for (int a = 0; a < length; a++)
				{
					b[a] = in.readByte();
				}
			} catch (Throwable t)
			{
				if (t instanceof ArrayIndexOutOfBoundsException)
				{

				} else
				{
					
				}
			}
			Logger.info("Decrypting in phase " + phase.name());
			if (phase == NetworkPhases.COM)
			{
				b = decrypt(key, b);

			} else if (phase == NetworkPhases.PRE2 || phase == NetworkPhases.POST)
			{
				b = decrypt(key, b);
			} else if (phase == NetworkPhases.PRE1)
			{
				if (networkphaseprogress.get(phase)[0])
				{
					b = decrypt(key, b);
				}
			}
			if (b == null)
			{
				throw new RuntimeException();
			}
			Logger.info("" + b.length);
			Object o = deserialize(b);
			if (o instanceof Request)
			{
				switch (phase)
				{
				case PRE0:
					Logger.info("Received request in " + phase.name());
					for (Requests r : Requests.values())
					{
						if (r.getName().equals(((Request) o).getName()))
						{
							switch (r)
							{
							case REQST_PING:
								if (((Request) o).getBuffer() instanceof ByteArrayWrapper)
								{
									send(new Response(Responses.RSP_PING.getName(),
											((ByteArrayWrapper) ((Request) o).getBuffer()).getBytes()));
								}
								break;
							case TRSMT_RSAKEY:
								if (((Request) o).getBuffer() instanceof PublicKey)
								{
									Logger.info(s.getInetAddress().getHostAddress(), "Received Key.");
									pub1 = (PublicKey) ((Request) o).getBuffer();
									networkphaseprogress.get(phase)[0] = true;
									send(new Response(Responses.RSP_RSAKEY.getName(), pub));
								} else
								{
									Logger.info(s.getInetAddress().getHostAddress(),
											"Received invalid key trasmission.");
								}
								break;
							default:
								break;
							}
						}
					}
					break;
				case PRE1:
					Logger.info("Received request in " + phase.name());
					for (Requests r : Requests.values())
					{
						if (r.getName().equals(((Request) o).getName()))
						{
							switch (r)
							{
							case REQST_PING:
								if (((Request) o).getBuffer() instanceof ByteArrayWrapper)
								{
									send(new Response(Responses.RSP_PING.getName(),
											((ByteArrayWrapper) ((Request) o).getBuffer()).getBytes()));
								}
								break;
							case TRSMT_AESKEY:
								Logger.info("Received AES key...");
								if (((Request) o).getBuffer() instanceof AESKeyWrapper)
								{
									key = ((AESKeyWrapper) ((Request) o).getBuffer()).getKey();
									networkphaseprogress.get(phase)[0] = true;
								}
								break;

							case TRSMT_PROTOCOL:
								Logger.info("Checking protocol...");
								if (((Request) o).getBuffer() instanceof ProtocolWrapper)
								{
									if (!(Main.protocol.getProtocolVersion().equals(
											((ProtocolWrapper) ((Request) o).getBuffer()).getProtocolVersion())))
									{
										send(new Response(Responses.RSP_PROTOCOL.getName(), Main.protocol,
												((Request) o).getNr()));
										// TODO DELAY because elsewise it might
										// not be delivered
										Server.getInstance().closeHandler(this);
										Logger.info("Client not up to date, closing connection.");
									} else if (networkphaseprogress.get(phase)[0])
									{
										send(new Response(Responses.RSP_PROTOCOL.getName(), Main.protocol,
												((Request) o).getNr()));
										Logger.info("Client up to date!");
										networkphaseprogress.get(phase)[1] = true;
									} else
									{
										Logger.info("No decryption established yet.");
									}
								} else
								{
									Logger.info("Invalid version.");
								}
								break;
							default:
								break;

							}
						}
					}

					break;

				case PRE2:
					Logger.info("Received request in " + phase.name());
					for (Requests r : Requests.values())
					{
						if (r.getName().equals(((Request) o).getName()))
						{
							switch (r)
							{
							case REQST_PING:
								if (((Request) o).getBuffer() instanceof ByteArrayWrapper)
								{
									send(new Response(Responses.RSP_PING.getName(),
											((ByteArrayWrapper) ((Request) o).getBuffer()).getBytes()));
								}
								break;
							case TRSMT_CREDS:
								if (((Request) o).getBuffer() instanceof CredentialsWrapper)
								{
									try
									{
										this.u = DSManager.getInstance().getUser(
												((CredentialsWrapper) ((Request) o).getBuffer()).getUsername(),
												((CredentialsWrapper) ((Request) o).getBuffer()).getPassword());
										Logger.info("Found user corresponding to the credentials !"
												+ ((((CredentialsWrapper) ((Request) o).getBuffer()).wantsToken())
														? "TOKEN!" : "NO TOKEN!"));
										send(new Response(Responses.RSP_CREDS.getName(),
												new LoginResponseWrapper(true,
														(((CredentialsWrapper) ((Request) o).getBuffer()).wantsToken())
																? "TOKENTODO" : "null")));
										networkphaseprogress.get(phase)[0] = true;
									} catch (SQLException e)
									{
										e.printStackTrace();
										Logger.info("Did not find user corresponding to the credentials !");
										send(new Response(Responses.RSP_CREDS.getName(),
												new LoginResponseWrapper(false, "null")));
									}
								} else
								{
									Logger.info("Couldn't recognize user credentials.");
									send(new Response(Responses.RSP_CREDS.getName(),
											new LoginResponseWrapper(false, "null")));
								}
								break;
							case TRSMT_TOKEN:
								if (((Request) o).getBuffer() instanceof String)
								{
									// CHECK TOKEN
								}
								break;
							default:
								Logger.info("Default");
								break;

							}
						}
					}
					break;
				case COM:
					Logger.info("Received request in " + phase.name());
					for (Requests r : Requests.values())
					{
						if (r.getName().equals(((Request) o).getName()))
						{
							switch (r)
							{
							case REQST_PING:
								if (((Request) o).getBuffer() instanceof ByteArrayWrapper)
								{
									send(new Response(Responses.RSP_PING.getName(),
											((ByteArrayWrapper) ((Request) o).getBuffer()).getBytes()));
								}
								break;
							case TRSMT_MESSAGE:
								if (((Request) o).getBuffer() instanceof MessageWrapper)
								{
									// Client wants to send a message to another
									// client
									if (((MessageWrapper) (((Request) o).getBuffer())).getSource() == null
											&& ((MessageWrapper) (((Request) o).getBuffer())).getDestination() != null
											&& ((MessageWrapper) (((Request) o).getBuffer())).getId() != -1
											&& ((MessageWrapper) (((Request) o).getBuffer())).getMessage() != null
											&& ((MessageWrapper) (((Request) o).getBuffer())).received() == false)
									{

										// sends message to destination
										Server.getInstance().queueMessageForUUID(
												((MessageWrapper) (((Request) o).getBuffer())).getDestination(),
												new MessageWrapper(((MessageWrapper) (((Request) o).getBuffer())),
														true));

										send(new Response(Responses.RCV_MESSAGE.getName(), new MessageWrapper(
												((MessageWrapper) (((Request) o).getBuffer())).getMessage(),
												u.getUuid(),
												((MessageWrapper) (((Request) o).getBuffer())).getDestination(), true,
												false, false, ((MessageWrapper) (((Request) o).getBuffer())).getId())));

									}
									if (((MessageWrapper) (((Request) o).getBuffer())).getSource() != null
											&& ((MessageWrapper) (((Request) o).getBuffer())).getDestination() != null
											&& ((MessageWrapper) (((Request) o).getBuffer())).getId() != -1
											&& ((MessageWrapper) (((Request) o).getBuffer())).getMessage() == null
											&& ((MessageWrapper) (((Request) o).getBuffer())).received() == true
											&& ((MessageWrapper) (((Request) o).getBuffer())).receivedByDest() == true
											|| ((MessageWrapper) (((Request) o).getBuffer())).read() == true)
									{
										if (((MessageWrapper) (((Request) o).getBuffer())).receivedByDest() == true)
										{
											// check if not received by dest
										}
										if (((MessageWrapper) (((Request) o).getBuffer())).read() == true)
										{
											// check if not read by dest
										}
										Server.getInstance().queueMessageForUUID(
												((MessageWrapper) (((Request) o).getBuffer())).getDestination(),
												new MessageWrapper(((MessageWrapper) (((Request) o).getBuffer())),
														((MessageWrapper) (((Request) o).getBuffer())).receivedByDest(),
														((MessageWrapper) (((Request) o).getBuffer())).read()));
									}
								}
								break;
							case REQST_DATA:
								Logger.info("Request for data...");
								send(new Response(Responses.RSP_DATA.getName(),
										new ProfileInfoWrapper(u.getUsername(), u.getStatus(), u.getProfilepic())));
								break;
							case SEARCH_USER:
								if (((Request) o).getBuffer() instanceof SearchUserWrapper)
								{
									Logger.info("Searches for user with current search query...");
									send(new Response(Responses.USER_QUERY.getName(),
											new UserVectorWrapper(DSManager.getInstance().searchUser(
													((SearchUserWrapper) ((Request) o).getBuffer()).getName()))));
								}
							default:
								break;
							}
						}
					}
					break;
				case POST:
					for (Requests r : Requests.values())
					{
						if (r.getName().equals(((Request) o).getName()))
						{
							switch (r)
							{
							case REQST_PING:
								if (((Request) o).getBuffer() instanceof ByteArrayWrapper)
								{
									send(new Response(Responses.RSP_PING.getName(),
											((ByteArrayWrapper) ((Request) o).getBuffer()).getBytes()));
								}
								break;
							default:
								break;
							}
						}
					}
					break;
				default:
					throw new RuntimeException("Networkphase could not be identified.");
				}
			} else if (o instanceof Response)
			{
				switch (phase)
				{
				case PRE0:
					for (Responses r : Responses.values())
					{
						if (r.getName().equals(((Response) o).getName()))
						{
							switch (r)
							{
							case RSP_PING:
								Logger.info("TLD: " + (System.currentTimeMillis() - lastPing));
								lastPing = System.currentTimeMillis();
								pinging = false;
								break;
							default:
								break;
							}
						}
					}
					break;
				case PRE1:
					for (Responses r : Responses.values())
					{
						if (r.getName().equals(((Response) o).getName()))
						{
							switch (r)
							{
							case RSP_PING:
								Logger.info("TLD: " + (System.currentTimeMillis() - lastPing));
								lastPing = System.currentTimeMillis();
								pinging = false;
								break;
							default:
								break;
							}
						}
					}
					break;
				case PRE2:
					for (Responses r : Responses.values())
					{
						if (r.getName().equals(((Response) o).getName()))
						{
							switch (r)
							{
							case RSP_PING:
								Logger.info("TLD: " + (System.currentTimeMillis() - lastPing));
								lastPing = System.currentTimeMillis();
								pinging = false;
								break;
							default:
								break;
							}
						}
					}
					break;
				case COM:
					for (Responses r : Responses.values())
					{
						if (r.getName().equals(((Response) o).getName()))
						{
							switch (r)
							{
							case RSP_PING:
								Logger.info("TLD: " + (System.currentTimeMillis() - lastPing));
								lastPing = System.currentTimeMillis();
								pinging = false;
								break;
							case RCV_MESSAGE:
								if (((MessageWrapper) (((Request) o).getBuffer())).getSource() != null
										&& ((MessageWrapper) (((Request) o).getBuffer())).getDestination() != null
										&& ((MessageWrapper) (((Request) o).getBuffer())).getId() != -1
										&& ((MessageWrapper) (((Request) o).getBuffer())).getMessage() == null
										&& ((MessageWrapper) (((Request) o).getBuffer())).received() == true
										&& ((MessageWrapper) (((Request) o).getBuffer())).receivedByDest() == true
										|| ((MessageWrapper) (((Request) o).getBuffer())).read() == true)
								{
									if (((MessageWrapper) (((Request) o).getBuffer())).receivedByDest() == true)
									{
										// check if not received by dest
									}
									if (((MessageWrapper) (((Request) o).getBuffer())).read() == true)
									{
										// check if not read by dest
									}
									Server.getInstance().queueMessageForUUID(
											((MessageWrapper) (((Request) o).getBuffer())).getDestination(),
											new MessageWrapper(((MessageWrapper) (((Request) o).getBuffer())),
													((MessageWrapper) (((Request) o).getBuffer())).receivedByDest(),
													((MessageWrapper) (((Request) o).getBuffer())).read()));
								}
								break;
							case USER_QUERY:
								break;
							default:
								break;

							}
						}
					}
					break;
				case POST:
					for (Responses r : Responses.values())
					{
						if (r.getName().equals(((Response) o).getName()))
						{
							switch (r)
							{
							case RSP_PING:
								Logger.info("TLD: " + (System.currentTimeMillis() - lastPing));
								lastPing = System.currentTimeMillis();
								pinging = false;
								break;
							default:
								break;
							}
						}
					}
					break;
				default:
					throw new RuntimeException("Networkphase could not be identified.");
				}
			} else
			{
				throw new BadPacketException();
			}
		}
		if (getInputStream().available() == 0)
		{
			queueEmpty = true;
		}
		// initiating stuff

		switch (phase)
		{
		case PRE0:
			if (networkphaseprogress.get(phase)[0])
			{
				Logger.info(s.getInetAddress().getHostAddress(), "Key exchange complete.");
				advance();
			}
			break;
		case PRE1:
			if (networkphaseprogress.get(phase)[0] == true && networkphaseprogress.get(phase)[1] == true)
			{
				advance();
			}
			break;
		case PRE2:
			if (networkphaseprogress.get(phase)[0] == true)
			{
				advance();
			} else if (networkphaseprogress.get(phase)[1] == true)
			{
				// TODO
			}
			break;
		case COM:
			// TODO
			if (u != null)
			{
				MessageWrapper m;
				// if ((m = Server.getInstance().messageDueForUUID(u.getUuid()))
				// != null)
				// {
				// send(new Request(Requests.TRSMT_MESSAGE.getName(), new
				// MessageWrapper(m.getMessage(), m.getSource(),
				// m.getDestination(), true, false, m.getId())));
				// }
			}
			break;
		case POST:
			// actually i dont think this phase is strictly necessary but well
			// at least i dont know what to do with it
			break;
		default:
			throw new RuntimeException("Networkphase could not be identified.");
		}
	}

	private void ping()
	{
		try
		{
			send(new Request(Requests.REQST_PING.getName(), new ByteArrayWrapper(ping)));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void advance()
	{
		switch (phase)
		{
		case PRE0:
			Logger.info("Advancing to pre1");
			networkphaseprogress.remove(phase);
			networkphaseprogress.put(NetworkPhases.PRE1, new boolean[10]);
			phase = NetworkPhases.PRE1;
			for (int i = 0; i < networkphaseprogress.get(phase).length; i++)
			{
				networkphaseprogress.get(phase)[i] = false;
			}
			break;
		case PRE1:
			Logger.info("Advancing to pre2");
			networkphaseprogress.remove(phase);
			networkphaseprogress.put(NetworkPhases.PRE2, new boolean[2]);
			phase = NetworkPhases.PRE2;
			for (int i = 0; i < networkphaseprogress.get(phase).length; i++)
			{
				networkphaseprogress.get(phase)[i] = false;
			}
			break;
		case PRE2:
			Logger.info("Advancing to com");
			networkphaseprogress.remove(phase);
			networkphaseprogress.put(NetworkPhases.COM, new boolean[2]);
			phase = NetworkPhases.COM;
			for (int i = 0; i < networkphaseprogress.get(phase).length; i++)
			{
				networkphaseprogress.get(phase)[i] = false;
			}
			break;
		case COM:
			break;
		case POST:
			// terminate this handler
			break;
		default:
			break;
		}
	}

	public DataInputStream getInputStream()
	{
		return in;
	}

	public DataOutputStream getOutputStream()
	{
		return out;
	}

	public void send(Request r) throws Exception
	{
		if (phase == NetworkPhases.COM)
		{
			String[] sa = getStrings(r.getBuffer());
			String s = null;
			if (sa.length == 1)
			{
				s = "Req;" + r.getName();
				s = s + ";" + sa[0];
			} else if (sa.length > 1)
			{
				s = "Req;" + r.getName();
				for (int i = 0; i < sa.length; i++)
				{
					s = s + ";" + sa[i];
				}
			}
			Logger.info("Sending " + s);
			byte[] b0 = encrypt(key, s.getBytes("UTF8"));
			out.writeInt(b0.length);
			out.write(b0);

		} else if (phase == NetworkPhases.PRE2 || phase == NetworkPhases.COM)
		{
			String[] sa = getStrings(r.getBuffer());
			String s = null;
			if (sa.length == 1)
			{
				s = "Req;" + r.getName();
				s = s + ";" + sa[0];
			} else if (sa.length > 1)
			{
				s = "Req;" + r.getName();
				for (int i = 0; i < sa.length; i++)
				{
					s = s + ";" + sa[i];
				}
			}
			Logger.info("Sending " + s);
			byte[] b0 = encrypt(key, s.getBytes("UTF8"));
			out.writeInt(b0.length);
			out.write(b0);

		} else if (phase == NetworkPhases.PRE1)
		{
			if (networkphaseprogress.get(phase)[0])
			{
				String[] sa = getStrings(r.getBuffer());
				String s = null;
				if (sa.length == 1)
				{
					s = "Req;" + r.getName();
					s = s + ";" + sa[0];
				} else if (sa.length > 1)
				{
					s = "Req;" + r.getName();
					for (int i = 0; i < sa.length; i++)
					{
						s = s + ";" + sa[i];
					}
				}
				Logger.info("Sending " + s);
				byte[] b0 = encrypt(key, s.getBytes("UTF8"));
				out.writeInt(b0.length);
				out.write(b0);
			} else
			{
				String[] sa = getStrings(r.getBuffer());
				String s = null;
				if (sa.length == 1)
				{
					s = "Req;" + r.getName();
					s = s + ";" + sa[0];
				} else if (sa.length > 1)
				{
					s = "Req;" + r.getName();
					for (int i = 0; i < sa.length; i++)
					{
						s = s + ";" + sa[i];
					}
				}
				Logger.info("Sending " + s);
				byte[] b0 = s.getBytes("UTF8");
				out.writeInt(b0.length);
				out.write(b0);
			}
		} else if (phase == NetworkPhases.PRE0)
		{
			String[] sa = getStrings(r.getBuffer());
			String s = null;
			if (sa.length == 1)
			{
				s = "Req;" + r.getName();
				s = s + ";" + sa[0];
			} else if (sa.length > 1)
			{
				s = "Req;" + r.getName();
				for (int i = 0; i < sa.length; i++)
				{
					s = s + ";" + sa[i];
				}
			}
			Logger.info("Sending " + s);
			byte[] b0 = s.getBytes("UTF8");
			out.writeInt(b0.length);
			out.write(b0);

		} else
		{
			Logger.info("Unknown phase.");
		}
		out.flush();
	}

	public void send(Response r) throws Exception
	{
		if (phase == NetworkPhases.COM)
		{
			String[] sa = getStrings(r.getBuffer());
			String s = null;
			if (sa.length == 1)
			{
				s = "Res;" + r.getName();
				s = s + ";" + sa[0];
			} else if (sa.length > 1)
			{
				s = "Res;" + r.getName();
				for (int i = 0; i < sa.length; i++)
				{
					s = s + ";" + sa[i];
				}
			}
			Logger.info("Sending " + s);
			byte[] b0 = encrypt(key, s.getBytes("UTF8"));
			out.writeInt(b0.length);
			out.write(b0);

		} else if (phase == NetworkPhases.PRE2 || phase == NetworkPhases.COM)
		{
			String[] sa = getStrings(r.getBuffer());
			String s = null;
			if (sa.length == 1)
			{
				s = "Res;" + r.getName();
				s = s + ";" + sa[0];
			} else if (sa.length > 1)
			{
				s = "Res;" + r.getName();
				for (int i = 0; i < sa.length; i++)
				{
					s = s + ";" + sa[i];
				}
			}
			Logger.info("Sending " + s);
			byte[] b0 = encrypt(key, s.getBytes("UTF8"));
			out.writeInt(b0.length);
			out.write(b0);

		} else if (phase == NetworkPhases.PRE1)
		{
			if (networkphaseprogress.get(phase)[0])
			{
				String[] sa = getStrings(r.getBuffer());
				String s = null;
				if (sa.length == 1)
				{
					s = "Res;" + r.getName();
					s = s + ";" + sa[0];
				} else if (sa.length > 1)
				{
					s = "Res;" + r.getName();
					for (int i = 0; i < sa.length; i++)
					{
						s = s + ";" + sa[i];
					}
				}
				Logger.info("Sending " + s);
				byte[] b0 = encrypt(key, s.getBytes("UTF8"));
				out.writeInt(b0.length);
				out.write(b0);
			} else
			{
				String[] sa = getStrings(r.getBuffer());
				String s = null;
				if (sa.length == 1)
				{
					s = "Res;" + r.getName();
					s = s + ";" + sa[0];
				} else if (sa.length > 1)
				{
					s = "Res;" + r.getName();
					for (int i = 0; i < sa.length; i++)
					{
						s = s + ";" + sa[i];
					}
				}
				Logger.info("Sending " + s);
				byte[] b0 = s.getBytes("UTF8");
				out.writeInt(b0.length);
				out.write(b0);
			}
		} else if (phase == NetworkPhases.PRE0)
		{
			String[] sa = getStrings(r.getBuffer());
			String s = null;
			if (sa.length == 1)
			{
				s = "Res;" + r.getName();
				s = s + ";" + sa[0];
			} else if (sa.length > 1)
			{
				s = "Res;" + r.getName();
				for (int i = 0; i < sa.length; i++)
				{
					s = s + ";" + sa[i];
				}
			}
			Logger.info("Sending " + s);
			byte[] b0 = s.getBytes("UTF8");
			out.writeInt(b0.length);
			out.write(b0);

		} else
		{
			Logger.info("Unknown phase.");
		}
		out.flush();
	}

	private String[] getStrings(Object o) throws UnsupportedEncodingException
	{
		Logger.info(o.getClass().getName());
		if (o instanceof Wrapper)
		{
			String[] s = ((networking.types.Wrapper) o).getStrings();
			for (int i = 0; i < s.length; i++)
			{
				s[i] = s[i].replace(";", "U+003B");
			}
			return s;
		}
		if (o instanceof Key)
		{
			if (o instanceof PublicKey)
			{
				return new String[]
				{ decodePublicKey((PublicKey) o) };
			} else if (o instanceof PrivateKey)
			{
				return new String[]
				{ decodePrivateKey((PrivateKey) o) };
			}
		}
		if (o instanceof String)
		{
			return new String[]
			{ ((String) o).replace(";", "U+003B") };
		}
		if (o instanceof Boolean)
		{
			return new String[]
			{ ((boolean) o) ? "true" : "false" };
		}
		throw new RuntimeException("Could not convert Object to string");
	}

	private ByteArrayOutputStream bOut;
	private ObjectOutputStream os;

	@SuppressWarnings("unused")
	@Deprecated
	private byte[] serialize(Object o) throws IOException
	{
		if (bOut != null && os != null)
		{
			os.writeObject(o);
			os.reset();
			return bOut.toByteArray();
		} else
		{
			bOut = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bOut);
			os.writeObject(o);
			os.reset();
			return bOut.toByteArray();
		}
	}

	public static byte[] encrypt(PublicKey publicKey, byte[] msg) throws NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(msg);
	}

	public static byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(encrypted);
	}

	public static byte[] encrypt(SecretKey key, byte[] msg) throws NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeyException
	{
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(msg);
	}

	public static byte[] decrypt(SecretKey key, byte[] encrypted) throws NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException
	{
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(encrypted);
	}

	/**
	 * Deserializes incoming byte sequence.
	 * 
	 * @param b
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws BadPacketException
	 */
	private Object deserialize(byte[] b) throws UnsupportedEncodingException, BadPacketException
	{
		String s = new String(b, "UTF8");
		Logger.info(s);
		String[] temp = s.split(";");
		String[] info = new String[]
		{ temp[0], temp[1] };
		String[] data = new String[temp.length - 2];

		for (int i = 2; i < temp.length; i++)
		{
			data[i - 2] = temp[i];
		}

		for (int i = 0; i < data.length; i++)
		{
			if (data[i].equals("null"))
			{
				data[i] = null;
			} /**
				 * else if (data[i].contains("U+003B")) { Logger.info("Replacing
				 * ; character"); data[i] = data[i].replace("U+003B", ";"); }
				 */
		}

		if (info[0].equals("Req"))
		{
			for (Requests r : Requests.values())
			{
				if (r.getName().equals(info[1]))
				{
					Logger.info(info[1]);
					if (r.getType().getSuperclass() != null)
					{
						if (r.getType().getSuperclass().equals(Wrapper.class))
						{
							try
							{
								return new Request(info[1],
										Wrapper.getWrapper((Class<? extends Wrapper>) r.getType(), data));
							} catch (Throwable t)
							{
								Logger.error(t);
							}
						}
					} else if (r.getType().equals(PublicKey.class))
					{
						try
						{
							return new Request(info[1], loadPublicKey(data[0]));
						} catch (GeneralSecurityException e)
						{
							e.printStackTrace();
						}
					} else
					{
						String stemp = null;
						for (String c : data)
						{
							stemp += c;
						}
						return new Request(info[1], stemp);
					}
				}
			}
			throw new BadPacketException("Package malfunctional.");
		} else if (info[0].equals("Res"))
		{
			for (Responses r : Responses.values())
			{
				if (r.getName().equals(info[1]))
				{
					if (r.getType().getSuperclass() != null)
					{
						if (r.getType().getSuperclass().equals(Wrapper.class))
						{
							return new Response(info[1],
									Wrapper.getWrapper((Class<? extends Wrapper>) r.getType(), data));
						}
					} else if (r.getType().equals(PublicKey.class))
					{
						try
						{
							return new Response(info[1], loadPublicKey(data[0]));
						} catch (GeneralSecurityException e)
						{
							e.printStackTrace();
						}
					} else
					{
						String stemp = null;
						for (String c : data)
						{
							stemp += c;
						}
						return new Request(info[1], stemp);
					}
				}
			}
			throw new BadPacketException("Package not properly built");
		} else
		{
			throw new BadPacketException("Type of package not declared");
		}

	}

	@SuppressWarnings("unused")
	private PrivateKey loadPrivateKey(String key) throws GeneralSecurityException, UnsupportedEncodingException
	{
		return KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key.getBytes("UTF8"))));
	}

	/**
	 * Decodes Base64 string to a RSA public key
	 * 
	 * @param key
	 * @return
	 * @throws GeneralSecurityException
	 * @throws UnsupportedEncodingException
	 */
	public static PublicKey loadPublicKey(String key) throws GeneralSecurityException, UnsupportedEncodingException
	{
		return KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key.getBytes("UTF8"))));
	}

	/**
	 * Encodes a RSA public key to a Base64 string
	 * 
	 * @param key
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String decodePublicKey(PublicKey key) throws UnsupportedEncodingException
	{
		return new String(Base64.getEncoder().encode(key.getEncoded()), "UTF8");
	}

	/**
	 * Encodes a RSA private key to a Base64 String
	 * 
	 * @param key
	 * @return
	 */
	public static String decodePrivateKey(PrivateKey key)
	{
		return new String(new PKCS8EncodedKeySpec(key.getEncoded()).getEncoded());
	}

	public Socket getSocket()
	{
		return s;
	}
}
