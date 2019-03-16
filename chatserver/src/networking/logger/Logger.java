package networking.logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * TODO new logger system required;
 * @author Unknown
 *
 */
public class Logger
{
	private static SimpleDateFormat sdf;
	 
	static
	{
		sdf = new SimpleDateFormat("HH:mm:ss:SSS");
	}
	
	public static void info(String info)
	{
		System.out.println("[" + getTime() + "]" + "[INFO]" + info);
	}

	public static void info(String source, String info)
	{
		print("INFO", source, info);
	}

	protected static String getTime()
	{
		return sdf.format(Calendar.getInstance().getTime());
	}

	public static void error(Exception error)
	{
		print("ERROR", error.getMessage());
	}

	public static void error(String source, Throwable error)
	{
		print("ERROR", source, error.getMessage());
	}

	private static void print(String prefix, String s)
	{
		System.out.println("[" + getTime() + "]" + "[" + prefix + "]" + s);
	}

	private static void print(String prefix, String source, String s)
	{
		System.out.println("[" + getTime() + "]" + "[" + prefix + "]" + "[" + source + "]" + s);
	}
}
