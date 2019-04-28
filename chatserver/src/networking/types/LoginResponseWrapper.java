package networking.types;

public class LoginResponseWrapper extends Wrapper
{
	private boolean loggedIn;
	private String token;

	public LoginResponseWrapper(boolean loggedIn, String token)
	{
		this.loggedIn = loggedIn;
		this.token = token;
	}

	public LoginResponseWrapper(String[] s)
	{
		loggedIn = s[0].equals("true") ? true : false;
		token = s[1];
	}

	@Override
	public String[] getStrings()
	{
		return new String[]{loggedIn ? "true" : "false", token};
	}
}
