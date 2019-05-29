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

public class ProfileInfoWrapper extends Wrapper
{
	private User user;

	public ProfileInfoWrapper()
	{
		// TODO Auto-generated constructor stub
	}

	public ProfileInfoWrapper(String[] s)
	{
		try
		{
			user = new User(new Email(s[2]), s[0], s[1], s[3], ImageIO.read(new ByteArrayInputStream(s[3].getBytes())),
					new Date(Long.valueOf(s[4])), s[5].getBytes());
		} catch (NumberFormatException | EmailFormationException | IOException e)
		{
			e.printStackTrace();
		}
	}

	public ProfileInfoWrapper(String username, String status, BufferedImage bi)
	{
		user = new User(null, username, null, status, bi, null, null);
	}

	@Override
	public String[] getStrings()
	{
		String string = null;
		if(user.getProfilepic() != null)
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
				user.getStatus() != null ? user.getStatus() : "null", user.getProfilepic() != null ? string : "null",
				user.getDate() != null ? String.valueOf(user.getDate().getTime()) : "null",
				user.getUuid() != null ? new String(user.getUuid()) : "null" };
	}

	public User getUser()
	{
		return user;
	}
}
