package main.types;

import java.nio.charset.StandardCharsets;

/**
 * 
 *	The encrypted password is an already decrypted password which gets send to the server when "keep logged in" is active.
 *	The client does not have a key for the password
 */
public class EncryptedPassword extends Password
{
	private byte[] bytes;
	public EncryptedPassword(byte[] bytes)
	{
		super(new String(bytes, StandardCharsets.UTF_16));
		this.bytes = bytes;
	}
	
	public String getPassword()
	{
		return new String(bytes, StandardCharsets.UTF_16);
	}
	
	public byte[] getBytes()
	{
		return bytes;
	}
}
