/** Pair.java
 * 
 *  @author Mehdi Manshadi 
 *  @date June 2013
 * 
 * Core was taken from:
 * 		http://www.factsandpeople.com/facts-mainmenu-5/8-java/10-java-pair-class?start=1
 *
 * @param <A>
 * @param <B>
 * 
 */

public class Pair<A, B> {
	 
	  public A fst;
	  public B snd;
	 
	  public Pair(A fst, B snd) {
	    this.fst = fst;
	    this.snd = snd;
	  }
	 
	  public A getFirst() { return fst; }
	  public B getSecond() { return snd; }
	 
	  public static String toString(String fst, String snd, String seperator)
	  {
		  return "<"+fst+seperator+snd+">";
	  }
	  
	  public static String getFirst(String p, String seperator) 
	  {
		  if (!p.startsWith("<") || !p.endsWith(">"))
			  return null;
		  
		  String [] s = p.substring(1, p.length()-1).split("\\Q"+seperator+"\\E", -1);
		  
		  if (s.length!=2)
			  return null;
		  
		  return s[0]; 
	  }
	  
	  public static String getSecond(String p, String seperator) 
	  { 
		  if (!p.startsWith("<") || !p.endsWith(">"))
			  return null;
		  
		  String [] s = p.substring(1, p.length()-1).split("\\Q"+seperator+"\\E", -1);
		  
		  if (s.length!=2)
			  return null;
		  
		  return s[1]; 
	  }
	 
	  public void setFirst(A v) { fst = v; }
	  public void setSecond(B v) { snd = v; }
	 
	  public String toString() {
	    return "<" + fst + "," + snd + ">";
	  }
	 
	  private static boolean equals(Object x, Object y) {
	    return (x == null && y == null) || (x != null && x.equals(y));
	  }
	 
	  public boolean equals(Pair<A, B> p) {
		  return	equals(fst, p.fst) && equals(snd, p.snd);
	  }
	  
	  public boolean isEmpty()
	  {
		  return toString().length()>0;
	  }
	 
	  public int hashCode() {
	    if (fst == null) return (snd == null) ? 0 : snd.hashCode() + 1;
	    else if (snd == null) return fst.hashCode() + 2;
	    else return fst.hashCode() * 17 + snd.hashCode();
	  }
	 
	  public static <A,B> Pair<A,B> of(A a, B b) {
	    return new Pair<A,B>(a,b);
	  }
}
