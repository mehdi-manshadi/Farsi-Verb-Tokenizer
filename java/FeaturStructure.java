/** FeaturStructure.java
 * 
 *  @author Mehdi Manshadi 
 *  @date June 2013
 *  
 */

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;


public class FeaturStructure {

	static final String CAT = "cat";
	Grammar g;
	String cat;

	TreeMap<String, String> avMatrix;
	
	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
		if (!cat.matches("\\w+"))
			System.err.println("Warning! cat contains non-word characters.");
	}

	public FeaturStructure(Grammar g) {
		this.g = g;
		avMatrix = new TreeMap<String, String>();
	}
	
	public boolean readFromString(String s)
	{
		s = s.trim();
		int i = s.indexOf('[');
		if (i<=0)
			if (!s.matches("\\w+"))
				return false;
			else
			{
				cat = s;
				return true;
			}
				
		if (s.charAt(s.length()-1)!=']')
			return false;
		
		cat = s.substring(0, i);
		String [] featurList = s.substring(i+1, s.length()-1).split("(,|\\s)+");
		for(String feature : featurList)
		{
			if (feature.isEmpty())
				continue;
			
			String [] av =  readAVFromString(feature);
			if (av==null)
				return false;
			
			
			if (!setValue(av[0].trim(), av[1].trim()))
				return false;
		}
		return true;
	}
	
	public FeaturStructure(Grammar g, String cat) {
		avMatrix = new TreeMap<String, String>();
		this.cat = cat; 
		this.g = g;
	}
	
	public FeaturStructure(FeaturStructure fs) {
		g = fs.g;
		cat = fs.cat;
		avMatrix = new TreeMap<String, String>(fs.avMatrix);
	}

	String getValue(String feature)
	{
		return avMatrix.get(feature);
	}
	
	boolean setValue(String feature, String value)
	{
		if (getValue(feature)!=null)
			return false;
		
		avMatrix.put(feature, value);
		return true;
	}
	
	boolean forceValue(String feature, String value)
	{
		avMatrix.put(feature, value);
		return true;
	}
	
	public void setDefaults(Hashtable<String, TreeMap<String, String>> default_values)
	{
		TreeMap <String, String> htDefaultValues = default_values.get(cat);
		if (htDefaultValues==null)
			return;
		
		for(Entry<String, String> feature: htDefaultValues.entrySet())
			setValue(feature.getKey(), feature.getValue());
	}
	
	@Override
	public String toString()
	{
		String s=cat;
		for(Entry<String, String> feature: avMatrix.entrySet())
			s += Util.pair(feature.getKey(), feature.getValue());
		return s;
	}

	public boolean isWord(LinkedList<FeaturStructure> wordsFeatureStructure) {
		boolean match = false;
		for(FeaturStructure fs : wordsFeatureStructure)
		{
			match = false;
			if (cat.equals(fs.getCat()))
			{
				match = true;
				for(String f: fs.getAttributeList())
					if (!avMatrix.get(f).equals(fs.getValue(f)))
					{
						match = false;
						break;
					}
			}
			
			if (match)
				break;
		}
						
		return match;
	}

	public Set<String> getAttributeList() {
		return avMatrix.keySet();
	}

	public static String[] readAVFromString(String feature) {
		if (feature.indexOf('=')<0)
			feature = feature.replace("+", "=+").replace("-", "=-");  //TODO Doesn't work if attribute name contains '-'. To be fixed.
		String [] av =  feature.split("=");
		if (av.length!=2)
			return null;
		
		av[0] = av[0].trim();
		av[1] = av[1].trim();
		
		return av;
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	public void substituteMacro() {
		FeaturStructure macroFS = g.getHtMacros().get(getCat());
		if (macroFS!=null)
		{
			setCat(macroFS.getCat());
			for(String att: macroFS.getAttributeList())
				setValue(att, macroFS.getValue(att));
		}
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof FeaturStructure)
		{
			FeaturStructure fs = (FeaturStructure) o;
			return toString().equals(fs.toString());
		}
		
		return false;
	}
}
