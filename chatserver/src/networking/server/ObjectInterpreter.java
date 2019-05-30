package networking.server;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import networking.types.CredentialsWrapper;
import networking.types.ProtocolWrapper;

//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//
@SuppressWarnings("unused")
public class ObjectInterpreter
{
	public Object interpreteString(Requests r, String... strings)
	{
		switch (r)
		{
		case TRSMT_CREDS:
			if (strings.length == 2)
			{
				return new CredentialsWrapper(strings[0], strings[1], strings[2] == "true");

			} else
			{
				throw new RuntimeException();
			}

		case TRSMT_RSAKEY:
			if (strings.length == 1)
			{
				try
				{
					return (RSAPublicKey) KeyFactory.getInstance("RSA")
							.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(strings[0])));
				} catch (InvalidKeySpecException | NoSuchAlgorithmException e)
				{
					e.printStackTrace();
				}

			} else
			{
				throw new RuntimeException();
			}

			throw new RuntimeException();
		case TRSMT_PROTOCOL:
			if (strings.length == 2)
			{
				return new ProtocolWrapper(strings[0], strings[1]);

			} else
			{
				throw new RuntimeException();
			}

		case TRSMT_TOKEN:
			// TODO
			if (strings.length == 1)
			{
				return strings[0];

			} else
			{
				throw new RuntimeException();
			}

		default:
			throw new RuntimeException();
		}
	}

	public Object interpreteString(Responses r, String... strings)
	{
		switch (r)
		{
		case RSP_CREDS:
			if (strings.length == 1)
			{
				return strings[0];
			} else
			{
				throw new RuntimeException();
			}
		case RSP_RSAKEY:
			try
			{
				if (strings.length == 1)
				{
					return (RSAPublicKey) KeyFactory.getInstance("RSA")
							.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(strings[0])));

				} else
				{
					throw new RuntimeException();
				}
			} catch (InvalidKeySpecException | NoSuchAlgorithmException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw new RuntimeException();
		case RSP_PROTOCOL:
			if (strings.length == 2)
			{
				return new ProtocolWrapper(strings[0], strings[1]);
			} else
			{
				throw new RuntimeException();
			}

		case RSP_TOKEN:
			if (strings.length == 1)
			{
				return strings[0];
			} else
			{
				throw new RuntimeException();
			}
		default:
			throw new RuntimeException();
		}
	}
}
