package networking.server;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.GeneralSecurityException;
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

import javax.crypto.Cipher;

import datastorage.main.DSManager;
import main.main.Main;
import main.types.User;
import networking.exceptions.BadPacketException;
import networking.logger.Logger;
import networking.types.CredentialsWrapper;
import networking.types.LoginResponseWrapper;
import networking.types.MessageWrapper;
import networking.types.ProfileInfoWrapper;
import networking.types.ProtocolWrapper;
import networking.types.Request;
import networking.types.Response;
import networking.types.Wrapper;

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

	private User u;

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
									Logger.info("Invalid user credentials.");
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
							case TRSMT_MESSAGE:
								if (((Request) o).getBuffer() instanceof MessageWrapper)
								{
									if (((MessageWrapper) (((Request) o).getBuffer())).getSource() != null
											&& ((MessageWrapper) (((Request) o).getBuffer())).getDestination() != null
											&& ((MessageWrapper) (((Request) o).getBuffer())).getId() != -1
											&& ((MessageWrapper) (((Request) o).getBuffer())).getMessage() != null)
									{
										Server.getInstance()
												.queueMessageForUUID(((MessageWrapper) (((Request) o).getBuffer())));
										send(new Response(Responses.RCV_MESSAGE.getName(), new MessageWrapper(
												((MessageWrapper) (((Request) o).getBuffer())).getMessage(),
												((MessageWrapper) (((Request) o).getBuffer())).getSource(),
												((MessageWrapper) (((Request) o).getBuffer())).getDestination(), true,
												false, ((MessageWrapper) (((Request) o).getBuffer())).getId())));
									}
								}
								break;
							case REQST_DATA:
								send(new Response(Responses.RSP_DATA.getName(),
										new ProfileInfoWrapper(u.getUsername(), u.getStatus(), u.getProfilepic())));
								break;
							default:
								break;
							}
						}
					}
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
				if ((m = Server.getInstance().messageDueForUUID(u.getUuid())) != null)
				{
					send(new Request(Requests.TRSMT_MESSAGE.getName(), new MessageWrapper(m.getMessage(), m.getSource(),
							m.getDestination(), true, false, m.getId())));
				}
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
		if (phase != NetworkPhases.PRE0)
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
			byte[] b0 = encrypt(pub1, s.getBytes("UTF8"));
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
		out.flush();
	}

	public void send(Response r) throws Exception
	{
		if (phase != NetworkPhases.PRE0)
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
			byte[] b0 = encrypt(pub1, s.getBytes("UTF8"));
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
		out.flush();
	}

	private String[] getStrings(Object o) throws UnsupportedEncodingException
	{

		if (o instanceof Wrapper)
		{
			return ((networking.types.Wrapper) o).getStrings();
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
			{ (String) o };
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
							return new Request(info[1],
									Wrapper.getWrapper((Class<? extends Wrapper>) r.getType(), data));
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
			throw new BadPacketException("Package malfunctional");
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

	private PrivateKey loadPrivateKey(String key) throws GeneralSecurityException, UnsupportedEncodingException
	{
		return KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key.getBytes("UTF8"))));
	}

	public static PublicKey loadPublicKey(String key) throws GeneralSecurityException, UnsupportedEncodingException
	{
		return KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key.getBytes("UTF8"))));
	}

	public static String decodePublicKey(PublicKey key) throws UnsupportedEncodingException
	{
		return new String(Base64.getEncoder().encode(key.getEncoded()), "UTF8");
	}

	public static String decodePrivateKey(PrivateKey key)
	{
		return new String(new PKCS8EncodedKeySpec(key.getEncoded()).getEncoded());
	}

	public Socket getSocket()
	{
		return s;
	}
}
