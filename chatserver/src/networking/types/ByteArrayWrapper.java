package networking.types;

import java.util.Base64;

/**
 * Holds an byte array
 * @author Bussard30
 */
public class ByteArrayWrapper extends Wrapper
{
	private byte[] bytes;

	public ByteArrayWrapper(byte[] bytes)
	{
		this.bytes = bytes;
	}

	/**
	 * Creates ByteArrayWrapper object with String obtained by
	 * {@link #getStrings()}
	 * 
	 * @param s
	 */
	public ByteArrayWrapper(String[] s)
	{
		if (s.length != 1)
			throw new RuntimeException("Too many parameters(" + s.length + ")");
		bytes = Base64.getDecoder().decode(s[0]);
	}

	/**
	 * @return String array required for {@link #ByteArrayWrapper(String[])}
	 */
	@Override
	public String[] getStrings()
	{
		return new String[]
		{ Base64.getEncoder().encodeToString(bytes) };
	}
	
	public byte[] getBytes()
	{
		return bytes;
	}
}
