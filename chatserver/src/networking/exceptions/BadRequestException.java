package networking.exceptions;

import networking.logger.Logger;

public class BadRequestException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BadRequestException()
	{
		super();
		Logger.error(this);
	}

	public BadRequestException(String message)
	{
		super(message);
	}

	public BadRequestException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public BadRequestException(Throwable cause)
	{
		super(cause);
	}

	public BadRequestException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
