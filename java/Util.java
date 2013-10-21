//
//  Util.java
//  
//
//  Created by Mehdi Manshadi on 4/1/11.
//

public class Util {
	
	public static String pair( String s1, String s2)
	{
		return "<"+s1+","+s2+">";
	}
	
	public static String spair( String s1, String s2) // sorted pair. the first string is less than the second
	{
		if (s1.compareTo(s2)<=0)
			return "<"+s1+","+s2+">";
		else
			return "<"+s2+","+s1+">";
	}

	public static String spair( String s) // sorted pair. the first string is less than the second
	{
		String s1 = unpair( s, 1);
		String s2 = unpair( s, 2);
		
		if (s1.compareTo(s2)<=0)
			return s;
		else
			return "<"+s2+","+s1+">";
	}

	public static String unpair( String s, int i)
	{
		if (!s.matches("<.*,.*>"))
			return null;
		
		if (i==1)
			return s.substring( 1, s.indexOf(','));
		else
			if (i==2)
				return s.substring( s.indexOf(',')+1, s.indexOf('>') );
		
		return null;
		
	}

	public static String ipair( String s) // inverts the pair
	{
		if (!s.matches("<.*,.*>"))
			return null;
		
		return pair( s.substring( s.indexOf(',')+1, s.indexOf('>')), s.substring( 1, s.indexOf(',')) );
	}
}
