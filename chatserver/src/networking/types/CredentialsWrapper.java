package networking.types;

import java.io.Serializable;

/**
 * Used to hold the username and password. Also informs server about whether clients wants a token.
 * @author Bussard30
 *
 */
public class CredentialsWrapper extends Wrapper implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -676427917948317648L;
	private String username, password;
	private boolean token;

	/**
	 * Creates CredentialsWrapper object.
	 * @param username
	 * @param password
	 * @param token
	 */
	public CredentialsWrapper(String username, String password, boolean token)
	{
		this.username = username;
		this.password = password;
		this.token = token;
	}

	/**
	 * Creates CredentialsWrapper object with String obtained by {@link #getStrings()}
	 * @param s
	 */
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
	
	/**
	 * @return String array required for {@link #CredentialsWrapper(String[])}
	 */
	@Override
	public String[] getStrings()
	{
		return new String[]
		{ username, password, token ? "true" : "false" };
	}

}
