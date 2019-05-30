package networking.types;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyWrapper extends Wrapper
{
	private SecretKey key;

	public AESKeyWrapper(SecretKey key)
	{
		this.key = key;
	}

	public AESKeyWrapper(String[] s)
	{
		if (s.length == 1)
		{
			key = new SecretKeySpec(Base64.getDecoder().decode(s[0]), "AES");
		} else
		{
			throw new RuntimeException("Invalid amount of arguments");
		}
	}

	@Override
	public String[] getStrings()
	{
		return new String[]
		{ Base64.getEncoder().encodeToString(key.getEncoded()) };
	}

	public SecretKey getKey()
	{
		return key;
	}
}
