package networking.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Vector;

import javax.imageio.ImageIO;

import main.types.User;

public class UserVectorWrapper extends Wrapper	
{
	private Vector<User> users;

	public UserVectorWrapper(Vector<User> users)
	{
		this.users = users;
	}

	public UserVectorWrapper(String[] s)
	{
		users = new Vector<>();
		for (int i = 0; i < s.length; i += 2)
		{
			try
			{
				users.add(new User(
						null,
						s[i + 0],
						null,
						null,
						s[i + 1] != null ? ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(s[4]))) : null,
						null,
						null));
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public String[] getStrings()
	{
		Vector<String> strings = new Vector<>();
		for (User user : users)
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
			strings.add((user.getUsername() != null) ? user.getUsername() : "null");
			strings.add(user.getProfilepic() != null ? string : "null");
		}
		return (String[]) strings.toArray();
	}
	
	public Vector<User> getUsers()
	{
		return users;
	}
}
