package networking.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Holds audio for voice chat.
 * @author Bussard30
 *
 */
public class AudioWrapper extends Wrapper
{
	private byte[] data;
	public AudioWrapper(String[] s)
	{
		assert s.length == 1;
		try
		{
			ByteArrayInputStream bi = new ByteArrayInputStream(Base64.getDecoder().decode(s[0]));
			GZIPInputStream i = new GZIPInputStream(bi);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (;;) {
                int b = i.read();
                if (b == -1) {
                    break;
                } else {
                    baos.write((byte) b);
                }
            }
            //play decompressed data
            data = baos.toByteArray();
			bi.close();
			i.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public String[] getStrings()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream go;
		try
		{
			go = new GZIPOutputStream(baos);
			go.write(data);
	        go.flush();
	        go.close();
	        baos.flush();
	        baos.close();
	        return new String[]{Base64.getEncoder().encodeToString(baos.toByteArray())};
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return new String[]{};
	}

}
