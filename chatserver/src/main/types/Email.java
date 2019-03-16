package main.types;

import main.exceptions.EmailFormationException;

public class Email
{
	private String local, domain, tld;

	/**
	 * Verified to be working.
	 * @param email
	 * @throws EmailFormationException
	 */
	public Email(String email) throws EmailFormationException
	{
		String[] s0 = email.split("@");
		if(s0.length > 2)
		{
			throw new EmailFormationException("More than one at");
		}
		else if(s0.length < 2)
		{
			throw new EmailFormationException("No at");
		}
		else if (s0[0].contains("."))
		{
			throw new EmailFormationException("Local contains dot");
		}
		
		System.out.println(s0[0]);
		System.out.println(s0[1]);
		String[] s1 = s0[1].split("\\.");
		System.out.println(s1.length);
		System.out.println(s1[0]);
		System.out.println(s1[1]);
		if(s1.length > 2)
		{
			throw new EmailFormationException("More than one dot");
		}
		else if(s1.length < 2)
		{
			throw new EmailFormationException("No dot");
		}
		else if(s0[0].length() > 63)
		{
			throw new EmailFormationException("Local too short");
		}
		else if(s0[0].length() < 1)
		{
			throw new EmailFormationException("Local too short");
		}
		else if(s1[0].length() > 255)
		{
			throw new EmailFormationException("Domain too long");
		}
		else if(s1[0].length() < 1)
		{
			throw new EmailFormationException("Domain not long enough");
		}
		else if(s1[1].length() > 63)
		{
			throw new EmailFormationException("TLD too long");
		}
		else if(s1[1].length() < 2)
		{
			throw new EmailFormationException("TLD not long enough");
		}
		
		
		local = s0[0];
		domain = s1[0];
		tld = s1[1];
	}

	public Email(String local, String domain, String tld)
	{
		this.local = local;
		this.domain = domain;
		this.tld = tld;
	}
	
	public String getLocal()
	{
		return local;
	}
	
	public String getDomain()
	{
		return domain;
	}
	
	public String getTLD()
	{
		return tld;
	}
}
