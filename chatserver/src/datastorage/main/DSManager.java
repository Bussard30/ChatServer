package datastorage.main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Vector;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.imageio.ImageIO;

import main.exceptions.EmailFormationException;
import main.types.Email;
import main.types.User;
import networking.logger.Logger;

public class DSManager
{
	private Connection connect = null;

	private static DSManager instance;

	/**
	 * TODO Requires new system with thread handling of certain tasks
	 */
	public DSManager()
	{
		if (instance != null)
		{
			throw new RuntimeException();
		} else
		{
			instance = this;
		}
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (!connection())
					;
			}

		}).start();
	}

	public boolean connection()
	{
		try
		{
			String host = "jdbc:mysql://localhost:3306/chatclient";
			String username = "root";
			String password = null;
			connect = DriverManager.getConnection(host, username, password);
			Logger.info("DSMANAGER", "Created connection.");
			return true;
		} catch (SQLException e)
		{
			Logger.info("DSMANAGER", "COULD NOT AQUIRE CONNECTION.");
			Logger.error(e);
			return false;
		}
	}
	/*
	 * @Deprecated public boolean validateUser(String username, String password)
	 * throws SQLException { PreparedStatement preparedStatement = connect
	 * .prepareStatement("select count(*) as recordcount from users where nickname = ? AND password = ?"
	 * ); preparedStatement.setString(1, username);
	 * preparedStatement.setString(2, password); ResultSet rs =
	 * preparedStatement.executeQuery(); rs.next(); return
	 * rs.getInt("recordcount") != 0; }
	 */

	public User getUser(String email, String password) throws SQLException
	{
		Logger.info("Fetching user...");
		long old = System.currentTimeMillis();

		PreparedStatement preparedStatement = connect.prepareStatement("select * from users where email = ?");
		preparedStatement.setString(1, email);
		ResultSet rs = preparedStatement.executeQuery();

		PreparedStatement preparedStatement0 = connect
				.prepareStatement("select count(*) as recordcount from users where email = ?");
		preparedStatement0.setString(1, email);
		ResultSet rs0 = preparedStatement0.executeQuery();
		rs0.next();

		if (rs0.getInt("recordcount") == 1)
		{
			rs.next();
			String passwordD = rs.getString("password");
			byte[] salt = rs.getBytes("salt");
			if (passwordD.length() < 10)
			{
				Logger.info("Generating hash for password.");
				salt = generateSalt();
				try
				{
					PreparedStatement preparedStatement1 = connect
							.prepareStatement("UPDATE users SET password = ? , salt = ? where email = ?");
					preparedStatement1.setString(1, bytetoString(getHashWithSalt(password, salt)));
					preparedStatement1.setBytes(2, salt);
					preparedStatement1.setString(3, email);
					preparedStatement1.executeUpdate();
					return getUser(email, password);
				} catch (NoSuchAlgorithmException | InvalidKeySpecException e)
				{
					e.printStackTrace();
				}

			} else
			{
				try
				{
					if (passwordD.equals(bytetoString(getHashWithSalt(password, salt))))
					{
						Logger.info("Successful comparison");
						try
						{
							Logger.info("User fetching operation took ~" + (System.currentTimeMillis() - old) + "ms");
							return new User(new Email(rs.getString("email")), rs.getString("nickname"),
									rs.getString("password"), rs.getString("description"),
									getImage(rs.getBytes("profile_pic")), rs.getDate("lastonline"),
									rs.getBytes("userid"));
						} catch (EmailFormationException e)
						{
							e.printStackTrace();
						}
					} else
					{
						Logger.info("!Successful comparison");
						Logger.info(email);
						Logger.info(passwordD);
						Logger.info(bytesToHex(salt));
						Logger.info(bytetoString(getHashWithSalt(password, salt)));
					}
				} catch (NoSuchAlgorithmException | InvalidKeySpecException e)
				{
					e.printStackTrace();
				}
			}
		}
		throw new SQLException();
	}
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	public void changeDescription(byte[] uuid, String desc)
	{
		try
		{
			PreparedStatement preparedStatement1 = connect
					.prepareStatement("UPDATE users SET description = ? where userid = ?");
			preparedStatement1.setString(1, desc);
			preparedStatement1.setBytes(2, uuid);
			preparedStatement1.executeUpdate();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] generateSalt()
	{
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[16];
		random.nextBytes(bytes);
		return bytes;
	}

	public byte[] getHashWithSalt(String input, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeySpec spec = new PBEKeySpec(input.toCharArray(), salt, 65536, 128);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		return factory.generateSecret(spec).getEncoded();
	}

	public String bytetoString(byte[] input)
	{
		return Base64.getEncoder().encodeToString(input);
	}

	public byte[] stringToByte(String input)
	{
		return Base64.getDecoder().decode(input);
	}

	public void changeName(byte[] uuid, String name)
	{
		try
		{
			PreparedStatement preparedStatement1 = connect
					.prepareStatement("UPDATE users SET name = ? where userid = ?");
			preparedStatement1.setString(1, name);
			preparedStatement1.setBytes(2, uuid);
			preparedStatement1.executeUpdate();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void changePassword(byte[] uuid, String name)
	{

	}

	public void changeProfilePic(byte[] uuid, BufferedImage bi)
	{
		Logger.info("Bytes of profile pic null");
		BufferedImage img = null;
		try
		{
			img = ImageIO.read(DSManager.class.getResource("image.jpg"));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(img, "jpg", baos);
		} catch (IOException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try
		{
			baos.flush();
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			PreparedStatement preparedStatement1 = connect
					.prepareStatement("UPDATE users SET profile_pic = ? where userid = ?");
			preparedStatement1.setBytes(1, baos.toByteArray());
			preparedStatement1.setBytes(2, uuid);
			preparedStatement1.executeUpdate();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			baos.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.info("Updated value.");
	}

	/**
	 * Inserts entry containing the id of u0 and u1 with status = 2
	 * 
	 * @param u0
	 * @param u1
	 */
	public void befriendUsers(User u0, User u1)
	{
		try
		{
			PreparedStatement pS0 = connect.prepareStatement("SELECT * FROM users where userid like ?");
			PreparedStatement pS1 = connect.prepareStatement("SELECT * FROM users where userid like ?");

			pS0.setBytes(1, u0.getUuid());
			pS1.setBytes(2, u1.getUuid());

			ResultSet rs0 = pS0.executeQuery();
			ResultSet rs1 = pS0.executeQuery();

			int i0 = rs0.getInt("id");
			int i1 = rs1.getInt("id");

			if (i0 < i1)
			{
				PreparedStatement pS2 = connect
						.prepareStatement("INSERT INTO `friends`(`userid_0`, `userid_1`, `status`) VALUES (?,?,?)");
				pS2.setInt(1, i0);
				pS2.setInt(2, i1);
				pS2.setInt(3, 2);
				pS2.executeUpdate();
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Vector<User> searchUser(String name)
	{
		PreparedStatement pS = null;
		Vector<User> v = new Vector<>();
		try
		{
			pS = connect.prepareStatement("SELECT * FROM users where nickname like ?");
			pS.setString(1, name);
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet rs = null;
		try
		{
			rs = pS.executeQuery();
			while (rs.next())
			{
				try
				{
					v.add(new User(null, rs.getString("nickname"), null, null,
							ImageIO.read(new ByteArrayInputStream(rs.getBytes("profile_pic"))), null, null));
				} catch (IOException e)
				{
					// fix user by setting default profile pic instead of bugged
					// one
				}
			}
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return v;
	}

	private User getUser(ResultSet rs)
	{
		try
		{
			return new User(new Email(rs.getString("email")), rs.getString("nickname"), rs.getString("password"),
					rs.getString("description"), ImageIO.read(new ByteArrayInputStream(rs.getBytes("profile_pic"))),
					rs.getDate("lastonline"), rs.getBytes("userid"));
		} catch (EmailFormationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException("Could not get User object of ResultSet");
	}

	/**
	 * Get user without password for security reasons
	 * @param rs
	 * @return User with password = null
	 */
	private User getUserWOPW(ResultSet rs)
	{
		try
		{
			return new User(new Email(rs.getString("email")), rs.getString("nickname"), null,
					rs.getString("description"), ImageIO.read(new ByteArrayInputStream(rs.getBytes("profile_pic"))),
					rs.getDate("lastonline"), rs.getBytes("userid"));
		} catch (EmailFormationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException("Could not get User object of ResultSet");
	}

	@SuppressWarnings("unused")
	private void writeResultSet(ResultSet rs) throws SQLException
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (rs.next())
		{
			for (int i = 1; i <= columnsNumber; i++)
			{
				if (i > 1)
					System.out.print(",  ");
				String columnValue = rs.getString(i);
				System.out.print(columnValue + " " + rsmd.getColumnName(i));
			}
			System.out.println("");
		}
	}

	public static DSManager getInstance()
	{
		return instance;
	}

	public BufferedImage getImage(byte[] bytes)
	{
		try
		{
			return ImageIO.read(new ByteArrayInputStream(bytes));
		} catch (IOException e)
		{
			throw new RuntimeException();
		}
	}
}
