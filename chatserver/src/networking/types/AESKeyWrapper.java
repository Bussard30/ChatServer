package networking.types;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Used to hold an AESKey
 * 
 * @author Bussard30
 *
 */
public class AESKeyWrapper extends Wrapper
{
	private SecretKey key;

	/**
	 * Creates AESKeyWrapper Object using <b> key </b>
	 * 
	 * @param key
	 */
	public AESKeyWrapper(SecretKey key)
	{
		this.key = key;
	}

	/**
	 * Creates AESKeyWrapper Object using strings gathered from
	 * {@link #getStrings}
	 * 
	 * @param s
	 */
	public AESKeyWrapper(String[] s)
	{
		if (s.length != 1)
			throw new RuntimeException("Too many parameters(" + s.length + ")");
		key = new SecretKeySpec(Base64.getDecoder().decode(s[0]), "AES");

	}

	/**
	 * @return String array required for {@link #CredentialsWrapper(String[])}
	 */
	@Override
	public String[] getStrings()
	{
		return new String[]
		{ Base64.getEncoder().encodeToString(key.getEncoded()) };
	}

	/**
	 * 
	 * @return AES key required for encryption and decryption in server/client
	 *         communication
	 */
	public SecretKey getKey()
	{
		return key;
	}
}
