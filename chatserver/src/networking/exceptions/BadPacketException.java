package networking.exceptions;

import networking.logger.Logger;

public class BadPacketException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3353601339684869221L;

	public BadPacketException()
	{
		super();
		Logger.error(this);
	}

	public BadPacketException(String message)
	{
		super(message);
	}

	public BadPacketException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public BadPacketException(Throwable cause)
	{
		super(cause);
	}

	public BadPacketException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
