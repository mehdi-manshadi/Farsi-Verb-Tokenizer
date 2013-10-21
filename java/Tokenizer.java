/** Tokenizer.java
 * 
 *  @author Mehdi Manshadi 
 *  @date June 2013
 *  
 */

import java.io.IOException;
import java.util.LinkedList;


public class Tokenizer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length<1)
		{
			System.err.println("Usage: java Tokenizer farsi-verb-morhology.xml");
			return;
		}
				
		Grammar g = new Grammar();
		g.readXML(args[0]);
		LinkedList<Constituent> wordsPatternList = g.run();
		LinkedList<Pair<String, String>> lsTransductions = g.compileTransductions(wordsPatternList);

		for(Pair <String, String> p: lsTransductions)
			System.out.println(p.getFirst()+"\t"+p.getSecond());
		
	}
	
}
