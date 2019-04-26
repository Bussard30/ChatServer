package networking.types;

import java.io.Serializable;

public class CredentialsWrapper extends Wrapper implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -676427917948317648L;
	private String username, password;
	private boolean token;

	public CredentialsWrapper(String username, String password, boolean token)
	{
		this.username = username;
		this.password = password;
		this.token = token;
	}

	public CredentialsWrapper(String[] s)
	{
		username = s[0];
		password = s[1];
		token = s[2].equals("true") ? true : false;
	}

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

	@Override
	public String[] getStrings()
	{
		return new String[]
		{ username, password, token ? "true" : "false" };
	}

}
