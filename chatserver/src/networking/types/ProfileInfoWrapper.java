package networking.types;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Base64;

import javax.imageio.ImageIO;

import main.exceptions.EmailFormationException;
import main.types.Email;
import main.types.User;
import networking.logger.Logger;

/**
 * Used to hold all information of an user.
 * @author Bussard30
 *
 */
public class ProfileInfoWrapper extends Wrapper
{
	private User user;

	public ProfileInfoWrapper()
	{
		// TODO Auto-generated constructor stub
	}
	public ProfileInfoWrapper(String username, String status, BufferedImage bi)
	{
		user = new User(null, username, null, status, bi, null, null);
	}
	/**
	 * Creates CredentialsWrapper object with String obtained by {@link #getStrings()}
	 * @param s
	 */
	public ProfileInfoWrapper(String[] s)
	{
		Logger.info(s[4]);
			try
			{
				user = new User(s[2] != null ? new Email(s[2]) : null, s[0], s[1], s[3],
						s[4] != null ? ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(s[4]))) : null,
						s[5] != null && s[5] == "null" ? new Date(Long.valueOf(s[5])) : null, s[6] != null ? s[6].getBytes() : null);
			} catch (NumberFormatException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EmailFormationException e)
			{
				try
				{
					user = new User(null, s[0], s[1], s[3],
							s[4] != null ? ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(s[4]))) : null,
							s[5] != null ? new Date(Long.valueOf(s[4])) : null, s[6] != null ? s[6].getBytes() : null);
				} catch (NumberFormatException | IOException e1)
				{
					try
					{
						user = new User(null, s[0], s[1], s[3],
								s[4] != null ? ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(s[4]))) : null,
								null, s[6] != null ? s[6].getBytes() : null);
					} catch (IOException e2)
					{
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	/**
	 * Images are being converted to Base64 strings
	 * @return String array required for {@link #CredentialsWrapper(String[])}
	 */
	@Override
	public String[] getStrings()
	{
		String string = null;
		if (user.getProfilepic() != null)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try
			{
				ImageIO.write(user.getProfilepic(), "png", baos);
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

			string = Base64.getEncoder().encodeToString(baos.toByteArray());
			try
			{
				baos.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new String[]
		{ user.getUsername() != null ? user.getUsername() : "null",
				user.getPassword() != null ? user.getPassword() : "null",
				user.getEmail() != null ? user.getEmail().getLocal() + "@" + user.getEmail().getDomain() + "."
						+ user.getEmail().getTLD() : "null",
				user.getStatus() != null ? user.getStatus() : "null",
				user.getProfilepic() != null ? string : "null",
				user.getDate() != null ? String.valueOf(user.getDate().getTime()) : "null",
				user.getUuid() != null ? new String(user.getUuid()) : "null" };
	}

	public User getUser()
	{
		return user;
	}
}
