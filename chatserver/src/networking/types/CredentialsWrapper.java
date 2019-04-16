package networking.types;

import java.io.Serializable;

public class CredentialsWrapper implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -676427917948317648L;
	private String username, password;
	private boolean token;
	public String getPassword()
	{
		return password;
	}

	public boolean wantsToken()
	{
		return token;
	}

	public String getUsername()
	{
		return username;
	}

	public CredentialsWrapper(String username, String password, boolean token)
	{
		this.username = username;
		this.password = password;
		this.token = token;
	}
	
}
