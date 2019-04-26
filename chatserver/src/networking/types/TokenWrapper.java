package networking.types;

public class TokenWrapper extends Wrapper
{
	private String token;

	public TokenWrapper(String token)
	{
		this.token = token;
	}

	public TokenWrapper(String[] s)
	{
		token = s[0];
	}

	@Override
	public String[] getStrings()
	{
		return new String[]
		{ token };
	}

}
