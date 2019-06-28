package networking.types;

/**
 * Used to hold a string for autocomplete queries.
 * @author Bussard30
 * @see UserVectorWrapper
 */
public class SearchUserWrapper extends Wrapper
{
	private String name;

	public SearchUserWrapper(String name)
	{
		this.name = name;
	}
	
	/**
	 * Creates CredentialsWrapper object with String obtained by {@link #getStrings()}
	 * @param s
	 */
	public SearchUserWrapper(String[] s)
	{
		if (s.length == 1)
			name = s[0];
	}

	/**
	 * @return String array required for {@link #CredentialsWrapper(String[])}
	 */
	@Override
	public String[] getStrings()
	{
		return new String[]
		{ name };
	}
	
	public String getName()
	{
		return name;
	}

}
