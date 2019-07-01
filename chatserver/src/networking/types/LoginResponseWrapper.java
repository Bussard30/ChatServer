package networking.types;

/**
 * Used to hold the token and whether the client has successfully logged in or not.
 * @author Bussard30
 *
 */
public class LoginResponseWrapper extends Wrapper
{
	private boolean loggedIn;
	private String token;

	/**
	 * Creates LoginResponseWrapper object.
	 * @param loggedIn
	 * @param token
	 */
	public LoginResponseWrapper(boolean loggedIn, String token)
	{
		this.loggedIn = loggedIn;
		this.token = token;
	}

	/**
	 * Creates CredentialsWrapper object with String obtained by {@link #getStrings()}
	 * @param s
	 */
	public LoginResponseWrapper(String[] s)
	{
		loggedIn = s[0].equals("true") ? true : false;
		token = s[1];
	}
	
	/**
	 * @return String array required for {@link #CredentialsWrapper(String[])}
	 */
	@Override
	public String[] getStrings()
	{
		return new String[]{loggedIn ? "true" : "false", token};
	}
	
	/**
	 * Returns if login attempt was successful
	 * @return
	 */
	public boolean isLoggedIn()
	{
		return loggedIn;
	}
}
