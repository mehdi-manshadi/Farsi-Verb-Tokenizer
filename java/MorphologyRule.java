/** MorphologyRule.java
 * 
 *  @author Mehdi Manshadi 
 *  @date June 2013
 *  
 */

import java.util.LinkedList;

public class MorphologyRule implements Comparable <MorphologyRule> {
	protected FeaturStructure lhs; 
	protected LinkedList <FeaturStructure> rhs;
	protected Grammar g;
	protected int head;
	private String replaceText;
	private int ruleID;
	
	public MorphologyRule(Grammar g, int ruleID) {
		this.g = g;
		this.ruleID = ruleID;
	}
	
	public MorphologyRule(MorphologyRule r) {  /* used in running mode */
		ruleID = r.ruleID;
		g = r.g;
		lhs = new FeaturStructure (r.lhs);
		rhs = new LinkedList<FeaturStructure>();
		for(FeaturStructure fs: r.rhs)
			rhs.add(new FeaturStructure (fs));
		head = r.head;
		replaceText = r.replaceText;
	}

	public boolean readFromString(String s)
	{
		int i = s.indexOf(':');  // lhs : rhs
		if (i<=0)
			return false;
		
		lhs = new FeaturStructure(g);
		lhs.readFromString(s.substring(0, i));
		
		rhs = new LinkedList<FeaturStructure>();
		
		int j = s.indexOf('>'); // TODO replace it with a constant # rhs > transformation
		if (j<0)
			j = s.length();

		String [] parts = s.substring(i+1, j).trim().split("\\.");
		if (parts.length<=0)
			return false;
		
		i=0;
		head=-1;
		for(String _fs: parts)  // fs: FeatureStructure
		{
			FeaturStructure fs = new FeaturStructure(g);
			
			_fs = _fs.trim();
			if (_fs.charAt(0)=='(' && _fs.charAt(_fs.length()-1)==')')
			{
				_fs = _fs.substring(1, _fs.length()-1);
				if (head>=0)
					System.err.println("Warning! more than one head constituent on the RHS.");
				head = i;
			}
			
			if (!fs.readFromString(_fs))
				return false;
			rhs.add(fs);
			
			i++;
		}
		
		if (head<0)
			System.err.println("Warning! no head constituent on the RHS.");
		
		if (j<s.length())
		{
			s = s.substring(j+1).trim();
			parts = s.split("\\.");  // TODO Add the following note to manual. Note! transformation cannot contain periods.
			s = "";
			for(String p: parts)
			{
				p = p.trim();
				if (p.matches("[0-9]+"))
					p = "\\"+p.replaceAll("^0+", "");
				else
					p = "${"+p+"}"; // TODO Shall we allow for quoted text?
				s += p;
			}
			replaceText = g.substituteConstants(s);
		}
		else  // default transformation: no change
		{
			replaceText = "";
			for(i=1; i<=rhs.size(); i++)
				replaceText += "\\"+String.valueOf(i);
		}
		
		if (replaceText.isEmpty())
			System.err.print("Warning! transformation string is empty.");
		
		return true;
	}
		
	@Override
	public String toString()
	{
		String s = lhs.toString()+": ";
		int i =0;
		for(FeaturStructure fs: rhs)
			s += (i==0? "":".")+(head==i++? "("+fs.toString()+")":fs.toString());
		return s;
	}

	public String getReplaceText() {
		return replaceText;
	}
	
	public int getRuleID()
	{
		return ruleID;
	}

	public int getRhsSize() {
		return rhs.size();
	}
	
	public int getRuleRank()
	{
		return g.getRuleRankByID(ruleID); //TODO lsSortedRules should be changed to Hashtable<Integer, MorphologyRule> 
	}
	
    @Override 
    public int compareTo(MorphologyRule r) {
        return this.getRuleID() - r.getRuleID() ;
    }

}
