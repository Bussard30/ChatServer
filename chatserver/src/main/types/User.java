package main.types;

import java.awt.image.BufferedImage;
import java.sql.Date;

public class User
{

	private Email email;
	private String username;
	private String password;
	private String status;
	private BufferedImage profilepic;
	private Date date;
	private byte[] uuid;

	public User(Email email, String username, String password, String status, BufferedImage profilepic, Date date,
			byte[] uuid)
	{
		this.email = email;
		this.username = username;
		this.password = password;
		this.setProfilepic(profilepic);
		this.date = date;
		this.uuid = uuid;
	}

	public Email getEmail()
	{
		return email;
	}

	public void setEmail(Email email)
	{
		this.email = email;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public byte[] getUuid()
	{
		return uuid;
	}

	public void setUuid(byte[] uuid)
	{
		this.uuid = uuid;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public BufferedImage getProfilepic()
	{
		return profilepic;
	}

	public void setProfilepic(BufferedImage profilepic)
	{
		this.profilepic = profilepic;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

}
