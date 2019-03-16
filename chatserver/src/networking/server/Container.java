package networking.server;

import java.util.ArrayList;
import java.util.List;

public class Container<T> {
	   private final Class<T> tclass;
	   private final List<T> list = new ArrayList<T>();
	   private T t;

	   Container(Class<T> tclass) {
	     this.tclass = tclass;
	   }

	   Class<T> getElementClass() {
	     return tclass;
	   }

	   void add(T t) {
	      list.add(tclass.cast(t));
	      this.t = tclass.cast(t);
	   }
	   
	   T get(Class<?> class0)
	   {
		   for(T type : list)
		   {
			   if(type.getClass() == class0)
			   {
				   return type;
			   }
		   }
		   return null;
	   }
}
