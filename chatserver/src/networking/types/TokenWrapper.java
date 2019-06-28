package networking.types;

/**
 * Used to hold a token for the keep logged in feature
 * @author Bussard30
 *
 */
public class TokenWrapper extends Wrapper
{
	private String token;

	public TokenWrapper(String token)
	{
		this.token = token;
	}

	/**
	 * Creates CredentialsWrapper object with a obtained by
	 * {@link #getStrings()}
	 * 
	 * @param s
	 */
	public TokenWrapper(String[] s)
	{
		token = s[0];
	}

	/**
	 * @return String array required for {@link #CredentialsWrapper(String[])}
	 */
	@Override
	public String[] getStrings()
	{
		return new String[]
		{ token };
	}

}
