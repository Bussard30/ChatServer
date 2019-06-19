package networking.types;

public class SearchUserWrapper extends Wrapper
{
	private String name;

	public SearchUserWrapper(String name)
	{
		this.name = name;
	}
	
	public SearchUserWrapper(String[] s)
	{
		if (s.length == 1)
			name = s[0];
	}

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
