package networking.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

/**
 * Abstract superclass for wrapper<br>
 * Due to not being able to declare a abstract constructor, an constructor containing only the type of String[] is required.<br>
 * @author Bussard30
 *
 */
public abstract class Wrapper
{
	/**
	 * @return String array required for Constructor(String[])
	 */
	public abstract String[] getStrings();

	/**
	 * Returns wrapper object.<br>
	 * Checks every constructor until it finds one containing only the String[] type<br>
	 * and then returns the object aquired by the constructor, which is given <b>s</b>
	 * @param wrapper
	 * @param s
	 * @return
	 */
	public static Wrapper getWrapper(Class<? extends Wrapper> wrapper, String[] s)
	{
		try
		{
			for(Constructor<?> c : wrapper.getDeclaredConstructors())
			{
				if(c.getParameters().length == 1)
				{
					if(c.getParameterTypes()[0].equals(String[].class))
					{
						for(Parameter p : c.getParameters())
						{
							System.out.println(p.getName());
						}
						return (Wrapper) c.newInstance((Object)s);
					}
				}
			}
		} catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
