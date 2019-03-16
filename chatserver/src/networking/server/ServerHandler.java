package networking.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.Cipher;

import datastorage.main.DSManager;
import main.main.Main;
import networking.exceptions.BadPacketException;
import networking.logger.Logger;
import networking.types.CredentialsWrapper;
import networking.types.Protocol;
import networking.types.Request;
import networking.types.Response;

public class ServerHandler
{
	private String password;
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

	public ServerHandler(Socket s)
	{
		this.s = s;
		KeyPairGenerator kpg = null;
		try
		{
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
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
	}

	public ServerHandler(Socket s, String password)
	{
		this(s);
		this.password = password;
	}

	public ServerHandler(Socket s, DataInputStream in, DataOutputStream out)
	{
		this(s);
		this.out = out;
		this.in = in;
	}

	public ServerHandler(Socket s, DataInputStream in, DataOutputStream out, String password)
	{
		this(s, in, out);
		this.password = password;
	}

	public void run() throws Exception
	{
		if (s.isClosed())
		{
			Server.getInstance().closeHandler(this);
		}
		if (queueEmpty == true)
		{
			queueEmpty = false;
			millis = System.currentTimeMillis();
		} else
		{
			if (millis - System.currentTimeMillis() > 5000)
			{
				Server.getInstance().overloadDetected(this);
			}
		}
		byte b[] = null;
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
					// inputstream already ended
					throw new EOFException();
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
					//
				}
			}
			if (phase != NetworkPhases.PRE0)
			{
				Logger.info("Decrypting in phase " + phase.name());
				try
				{
					b = decrypt(pvt, b);
				} catch (Exception e)
				{
					Logger.info("Could not decrypt data successfully. Deserializing data without decryption...");
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
							case TRSMT_KEY:
								if (((Request) o).getBuffer() instanceof PublicKey)
								{
									Logger.info(s.getInetAddress().getHostAddress(), "Received Key.");
									pub1 = (PublicKey) ((Request) o).getBuffer();
									networkphaseprogress.get(phase)[0] = true;
									send(new Response(Responses.RSP_KEY.getName(), pub));
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
							case TRSMT_PROTOCOL:
								Logger.info("Checking protocol...");
								if (((Request) o).getBuffer() instanceof Protocol)
								{
									if (!(Main.protocol.getProtocolVersion()
											.equals(((Protocol) ((Request) o).getBuffer()).getProtocolVersion())))
									{
										send(new Response(Responses.RSP_PROTOCOL.getName(), Main.protocol,
												((Request) o).getNr()));
										// TODO DELAY because elsewise it might
										// not be delivered
										Server.getInstance().closeHandler(this);
										Logger.info("Client not up to date, closing connection.");
									} else
									{
										send(new Response(Responses.RSP_PROTOCOL.getName(), Main.protocol,
												((Request) o).getNr()));
										Logger.info("Client up to date!");
										networkphaseprogress.get(phase)[0] = true;
									}
								} else
								{
									Logger.info("Invalid data.");
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
							case TRSMT_CREDS:
								if (((Request) o).getBuffer() instanceof CredentialsWrapper)
								{
									if (DSManager.getInstance().validateUser(
											((CredentialsWrapper) ((Request) o).getBuffer()).getUsername(),
											((CredentialsWrapper) ((Request) o).getBuffer()).getPassword()))
									{
										Logger.info("Found user corresponding to the credentials !" + ((((CredentialsWrapper) ((Request) o).getBuffer()).wantsToken()) ? "TOKEN!" : "NO TOKEN!"));
										send(new Response(Responses.RSP_CREDS.getName(),
												(((CredentialsWrapper) ((Request) o).getBuffer()).wantsToken())
														? "TOKEN" : "1"));
										networkphaseprogress.get(phase)[0] = true;
									} else
									{
										Logger.info("Did not find user corresponding to the credentials !");
										send(new Response(Responses.RSP_CREDS.getName(), "ACCESS DENIED"));
									}
								} else
								{
									Logger.info("Invalid user credentials.");
									send(new Response(Responses.RSP_CREDS.getName(), "ACCESS DENIED"));
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
					break;
				case POST:

					break;
				default:
					throw new RuntimeException("Networkphase could not be identified.");
				}
			} else if (o instanceof Response)
			{
				switch (phase)
				{
				case PRE0:

					break;
				case PRE1:

					break;
				case PRE2:

					break;
				case COM:

					break;
				case POST:

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
			if (networkphaseprogress.get(phase)[0] == true)
			{
				advance();
			}
			break;
		case PRE2:
			if (networkphaseprogress.get(phase)[0] == true)
			{
				advance();
			}
			break;
		case COM:
			// int i2 = networkphaseprogress.get(NetworkPhases.COM);
			// if (i2 == 0)
			// {
			// // TODO
			// // get updates
			// // send updates
			// }
			break;
		case POST:
			// actually i dont think this phase is strictly necessary but well
			// at least i dont know what to do with it
			break;
		default:
			throw new RuntimeException("Networkphase could not be identified.");
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
		if (current == Integer.MAX_VALUE)
		{
			current = 0;
		} else
		{
			current++;
		}
		r.setNr(current);
		byte[] b = serialize(r);
		if (phase != NetworkPhases.PRE0)
		{
			byte[] b0 = encrypt(pub1, b);
			out.writeInt(b0.length);
			out.write(b0);
		} else
		{
			out.writeInt(b.length);
			out.write(b);
		}
		out.flush();
	}

	public void send(Response r) throws Exception
	{
		byte[] b = serialize(r);
		if (phase != NetworkPhases.PRE0)
		{
			byte[] b0 = encrypt(pub1, b);
			out.writeInt(b0.length);
			out.write(b0);
		} else
		{
			out.writeInt(b.length);
			out.write(b);
		}
		out.flush();
	}

	private ByteArrayOutputStream bOut;
	private ObjectOutputStream os;

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

	public static byte[] encrypt(PublicKey publicKey, byte[] msg) throws Exception
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(msg);
	}

	public static byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws Exception
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(encrypted);
	}

	private Object deserialize(byte[] b) throws IOException, ClassNotFoundException
	{
		try (ByteArrayInputStream bis = new ByteArrayInputStream(b); ObjectInputStream in = new ObjectInputStream(bis))
		{
			return in.readObject();
		}

	}

	public Socket getSocket()
	{
		return s;
	}
}
