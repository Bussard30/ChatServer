package networking.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public abstract class Wrapper
{
	public abstract String[] getStrings();

	public static Wrapper getWrapper(Class<? extends Wrapper> wrapper, String[] s)
	{
		try
		{
			for(Constructor<?> c : wrapper.getDeclaredConstructors())
			{
				if(c.getParameterTypes()[0].equals(String[].class) && c.getParameterTypes().length == 1)
				{
					for(Parameter p : c.getParameters())
					{
						System.out.println(p.getName());
					}
					return (Wrapper) c.newInstance((Object)s);
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
