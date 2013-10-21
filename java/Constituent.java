import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

public class Constituent implements Comparable <Constituent> {
	private Grammar g;
	private ActiveArc arc;
	private String searchPattern;
	private String replacePattern;
	private int groupCount;
	
	public Constituent(Grammar g) {
		this.g = g;
		searchPattern = null; 
		replacePattern = null;
		groupCount = 1;
	}

	public Constituent (ActiveArc arc)
	{
		this.arc = arc;
		g = arc.g;
		searchPattern = null; 
		replacePattern = null;
		groupCount = 1;//arc.groupCount();
	}

	public Constituent(Grammar g, String searchPattern, String replacePattern) {
		this.g = g;
		init(searchPattern, replacePattern);
	}

	public Constituent(Grammar g, String searchPattern) {
		this.g = g;
		init(searchPattern, null);
	}

	private void init(String searchPattern, String replacePattern)
	{
		//if (!searchPattern.matches("(\\(.*\\))"))  // TODO Allow searchPattern to be "(\\(.*\\))+"
		String groupIndex = g.nextGroupIndex();
		
		this.searchPattern = groupIndex+'('+searchPattern+')';
		
		if (replacePattern==null)
			replacePattern = groupIndex;
		else
			this.replacePattern = replacePattern;
		//this.groupCount = 1;
	}
	
	public String getSearchPattern() {
		if (searchPattern==null)
			computeTransduction();
		
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public String getReplacePattern() {
		if (replacePattern==null)
			computeTransduction();
		
		return replacePattern;
	}

	public void setReplacePattern(String replacePattern) {
		this.replacePattern = replacePattern;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public void setGroupCount(int groupCount) {
		this.groupCount = groupCount;
	}

	public static String [] splitSearchPattern(String s) {
		char [] searchPatternArray = s.toCharArray();
		LinkedList <String> splitList = new LinkedList<String>();
		int count = 0, j=0; // TODO check whether s matches "(\\(.*\\))+"
		for(int i=0; i<searchPatternArray.length; i++)
			switch (searchPatternArray[i]) {
			case '(':
				count++;
				break;
			case ')':
				count--;
				if (count==0)
				{
					splitList.add(s.substring(j+1, i));
					j = i+1;
				}
				break;
			}
		String [] splitArray = new String[splitList.size()];
		splitList.toArray(splitArray);
		return splitArray;
	}

	public void computeTransduction() {
		searchPattern = "";
		replacePattern = arc.getReplaceText();
		Constituent [] lsConstituents = arc.getConstituents(); 
		for(int j=0; j<lsConstituents.length; j++)
		{
			Constituent c = lsConstituents[j];
			searchPattern += c.getSearchPattern();
			String replaceText = c.getReplacePattern();

			for(int i=c.getGroupCount(); i>0; i--)  // updating group numbers in replacement pattern
				replaceText = replaceText.replace("\\"+String.valueOf(i), "\\"+String.valueOf(i+groupCount));
		
			replacePattern = replacePattern.replace("\\"+String.valueOf(j+1), replaceText);  //TODO lookahead should be added.
			groupCount += c.getGroupCount();
		}
	}

	/*public int getRuleID() {
		return arc.getRuleID();
	}*/

	public int getRuleRank() {
		if (arc==null)
			return g.getRulesCount();
		
		return arc.getRuleRank();
	}

	public static LinkedList<Pair<String, String>> buildPatterns(Grammar g, HashSet<Constituent> lsConstituents) {
		/**
		 * 				THIS FUNCTION ONLY WORKS IF THE CONTEXT-FREENESS OF THE GRAMMAR HOLDS.
		 * 
		 * CFG Assumption: 
		 * 				If there are patterns s and s^\prime built by rule r, s.t. s_i & s^\prime_j (i!=j) are substrings of s & s^\prime 
		 * 				matching i_th and j_th category on the RHS of r respectively,
		 * 				then there is a pattern s^\second which contains both s_i & s^\prime_j at the same time.
		 * 				Note that this requires defining one individual rule per agreement for past/present progressive.
		 *   
		 */
		LinkedList<Pair<String, String>> lsAllPatterns = new LinkedList<Pair<String,String>>(); // Referred to as {(s, r)} in the comments below.
		
		/** Partition input constituents into lists, one list per MorphologyRule **/ 
		Vector <HashSet<Constituent>> vRuleConstituents = // list-of-constituents indexed rule-index.
				new Vector<HashSet<Constituent>>(g.getRulesCount()+1, 0);
				// The last element is reserved for rule==null, that is, lexical constituents
		for(int i=0; i<=g.getRulesCount(); i++)
			vRuleConstituents.add(new HashSet<Constituent>());
		for(Constituent c: lsConstituents)
			vRuleConstituents.get(c.getRuleRank()).add(c); 
			// note c.getRuleIndex returns g.getRulesCount() for rule==null (i.e. for lexical constituents).
		
		/** 1. For each list l_r, containing input constituents built by the MorphologyRule r, 
		 * 		Recursively build the patterns for each category i on the RHS of r.
		 * 		Call it {(s_r,i, r_r,i)}.
		 *  2. Using Cartesian product, concatenate {(s_r,i, r_r,i)}s to build overall search/replace pattern 
		 *  					for input constituents built by rule r, that is, 
		 * 								{(s_r,r_r)} = {(s_r,1, r_r,1)} * ... * {(s_r,n , r_r,m)}
		 *  3. Take the union of {(s_r, r_r)} for all rule r, to built the final set of search/replace patterns {(s,r)}
		**/	

		for(int rank=0; rank<g.getRulesCount(); rank++) // for each rule r
		{
			HashSet<Constituent> lsRankConstituents   // referred to as l_r in the comments
									= vRuleConstituents.get(rank);
			
			if (lsRankConstituents.size()<=0)
				continue;
			
			MorphologyRule r = g.getRulebyRank(rank);
			int m = r.getRhsSize();
			
			/* A 2D list, one list (-of-constituents) l_r,i for each category i on the RHS of r. */
			Vector<HashSet<Constituent>> ls2dConstituents = 
						new Vector<HashSet<Constituent>>( m, 0);
			
			for(int i=0; i< m; i++)  // initialize lists
				ls2dConstituents.add( new HashSet<Constituent>()); 
				
			/** put all the sub-constituents corresponding to the category i on the RHS of r
			 * 			into one list, that is, l_r,i. 
			**/
			for(Constituent c: lsRankConstituents) // for each c in l_r, that is, each input constituent c built by r
			{				
				assert m==c.arc.getConstituents().length;
				int i=0;
				for( Constituent c_i:c.arc.getConstituents()) // for each sub_constituent c_i of c
				{
					ls2dConstituents.get(i).add(c_i); // add c_i to l_r,i 
					i++;
				}
			}

			/** Recursively build {(s_r,i, r_r,i)} from l_r,i , for each category i on the RHS of r.
			 * and then combine them with partial patterns built so far using Cartesian product, that is, 
			 * 		{(s_{r,1...i}, r_{r,1..i})} = {(s_{r,1...i-1}, r_{r,1..i-1})} * {(s_r,i s_r,i)}
			 * in which '*' is Cartesian product and product of (s_{r,1...i-1}, r_{r,1..i-1}) & (s_r,i s_r,i)
			 * is the pair s_{r,1...i}, r_{r,1..i}) s.t.
			 * 		s_{r,1...i} = s_{r,1...i-1} + s_{r,i}
			 * 		r_{r,1...i} is s_{r,1...i-1}, in which \i is replaced with r_i.
			 **/ 
			int i=1;
			LinkedList<Pair<String, String>> lsRulePatterns 	// Referred to as {(s_r, r_r)} in the comments.
							= new LinkedList<Pair<String,String>>(); 
			lsRulePatterns.add(new Pair<String, String>("", r.getReplaceText()));
			for(HashSet<Constituent> lsSubConstituents: ls2dConstituents) // for each l_r,i (r:fixed, i=1..m)
			{
				LinkedList<Pair <String, String>> lsConstituentPatterns = 
						Constituent.buildPatterns(g, lsSubConstituents); // Building set of patterns (referred to as {(s_r,i , r_r,i)} in comments)
				
				LinkedList<Pair <String, String>> lsTempPatterns = lsRulePatterns ;
				lsRulePatterns = new LinkedList<Pair<String,String>>();

				/* Cartesian product */
				for(Pair <String, String> p1: lsTempPatterns)
					for(Pair <String, String> p2: lsConstituentPatterns)
						lsRulePatterns.add( new Pair<String, String>(
										p1.getFirst()+p2.getFirst(), // s_{r,1...i} = s_{r,1...i-1}+s_r,i 
										p1.getSecond().replaceAll("\\\\"+i+"(?![0-9])", p2.getSecond().replaceAll("\\\\", "\\\\\\\\")) // \i in r_{r,1...i-1} si replaced with r_r,i 
										/* Note! We have assumed that replace patterns do not contain digits as constants. */
									));
				i++;
			}					
			
			lsAllPatterns.addAll(lsRulePatterns); // Taking the union
			
		} // for each rule r
		
		/* taking care of rank==g.getRulesCount(), that is, lexical constituents */
		LinkedList<Constituent> lsLexicalConstituents = new LinkedList<Constituent> (
						vRuleConstituents.get(g.getRulesCount()));
		Collections.sort(lsLexicalConstituents, Collections.reverseOrder());
		if (lsLexicalConstituents.size()>0)
		{
			String searchPattern = "";
			for(Constituent c: lsLexicalConstituents)
				searchPattern += '|'+c.getSearchPattern();
			String groupIndex = g.nextGroupIndex();
			searchPattern = groupIndex+'('+searchPattern.substring(1)+')';
			lsAllPatterns.add( new Pair<String, String>(searchPattern, groupIndex));
		}
		
		//searchPatternAll = '('+searchPatternAll.substring(1)+')';
				
		return lsAllPatterns;
	}

	public static Pair<String, String> reIndexGroups(String searchPatternAll,
			String replacePatternAll) {
		//TODO Make it more efficient possibly using StringBuffer
		int count = 0;
		replacePatternAll = replacePatternAll.replaceAll("\\\\", "\\\\0"); /* This is to make sure that below 
												we do not re-change an already changed group-index in the replace-pattern. 
												TODO Should be changed to something like replaceAll("\\\\([0-9]+)", "\\\\0\\1") 
												where "\\1" refers to "([0-9]+)" in the search pattern. */
		char [] charArray = searchPatternAll.toCharArray();
		for(int i=0; i<charArray.length; i++)
		{
			if (charArray[i]=='(')
				count++;
			else
			if (charArray[i]=='\\')
			{
				String ch, groupIndex = "";
				while ((ch=String.valueOf(charArray[++i])).matches("[0-9]")) 
					groupIndex += ch;

				assert groupIndex.length()>0 && charArray[i]=='(';
				
				count++;
				replacePatternAll = replacePatternAll.replaceAll("\\\\0"+groupIndex+"(?![0-9])", "\\\\"+String.valueOf(count)); //negative-lookahead: (?![0-9])
			}
		}
		
		// Removing all '\group-id(' from search-pattern
		searchPatternAll = searchPatternAll.replaceAll("\\\\[0-9]+", "");
		
		return new Pair<String, String>(searchPatternAll, replacePatternAll);
	}

    @Override 
    public int compareTo(Constituent c) {
        return this.getSearchPattern().compareTo(c.getSearchPattern()) ;
    }

    public Grammar getGrammar() {
		return g;
	}
}
