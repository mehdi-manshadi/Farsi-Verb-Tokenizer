/** Grammar.java
 * 
 *  @author Mehdi Manshadi 
 *  @date June 2013
 *  
 */

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class Grammar {

	public static String ZS = "\u200C";
	public static String NS = "\u00A0";
    private static final String RULES_TAG = "RULES";
	private static final String RULE_TAG = "RULE";
	private static final String MACROS_TAG = "MACROS";
	private static final String CONSTANTS_TAG = "CONSTANTS";
	private static final String DEFAULTS_TAG = "DEFAULTS";
	private static final String FOOT_FEATURES_TAG = "FOOT-FEATURES";
	private static final String HEAD_FEATURES_TAG = "HEAD-FEATURES";
	private static final String WORDS_TAG = "WORDS";
	private static final String CATEGORIES_TAG = "CATEGORIES";
	private static final String FEATURES_TAG = "FEATURES";
	private static final String VB = "VB";
	private static final String VBD = "VBD";
	public static Hashtable<String, String> htTransform;
	private Hashtable<FeaturStructure, String> htConstants;
    private Hashtable<String, TreeMap<String, String>> htDefaultValues;
	private HashSet <String> hsFootFeatures;
    private Hashtable<String, FeaturStructure> htMacros;
    private LinkedList<MorphologyRule> rules;
	private LinkedList <FeaturStructure> wordsFeatureStructure;
	private Hashtable<String, LinkedList <String>> htCatFeatureList;
	private Hashtable<String, HashSet <String>> htCatFeatureHash;
	private Hashtable<String, LinkedList<String>> htFeaturesValueList;
	private HashSet<String> hsHeadFeatures;
	public FeaturStructure fsVB;
	public FeaturStructure fsVBD;
	public String strVB;
	public String strVBD;
	public LinkedList<MorphologyRule> lsSortedRules;
	private int rulesCount;
	private int groupCount;
	public Hashtable<Integer, Integer> lsSortedRulesId2Rank;
    
	public Grammar() { groupCount = 1; }
	
	public Hashtable<String, HashSet<String>> getHtCatFeatureHash() {
		return htCatFeatureHash;
	}

    public Hashtable<String, TreeMap<String, String>> getHtDefaultValues() {
		return htDefaultValues;
	}

	public HashSet<String> getHsFootFeatures() {
		return hsFootFeatures;
	}

	public HashSet<String> getHsHeadFeatures() {
		return hsHeadFeatures;
	}

	public Hashtable<String, FeaturStructure> getHtMacros() {
		return htMacros;
	}

	public Hashtable<String, LinkedList<String>> getHtCatFeatureList() {
		return htCatFeatureList;
	}

	public Hashtable<String, LinkedList<String>> getHtFeaturesValueList() {
		return htFeaturesValueList;
	}

	public void readXML(String _in){

    	try {

           DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
           DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
           Document doc = docBuilder.parse (new File(_in));

           // normalize text representation
           doc.getDocumentElement().normalize();
            
           Element root =  doc.getDocumentElement();


           // List of features and their values
           htFeaturesValueList = new Hashtable<String, LinkedList<String>>();
           
           Element eFeaturesValueList =  (Element)root.getElementsByTagName(FEATURES_TAG).item(0);
           String [] aFeaturesValueList = ((Node)(NodeList)(eFeaturesValueList.getChildNodes()).item(0)).getNodeValue().trim().split("\\n");

           for(String line: aFeaturesValueList)
           {
        	   line = normalize(line);
        	   if (line.isEmpty())
        		   continue;
        	   
        	   int i = line.indexOf(':');
        	   if (i<=0)
        	   {
        		   System.err.println("Incorrect feature value-list: "+line);
        		   continue;
        	   }
        	   String feature = line.substring(0, i).trim();
        	   if (htFeaturesValueList.containsKey(feature))
        	   {
        		   System.err.println("Duplicate feature value-list: "+line);
        		   continue;
        	   }
        	   String strValueList = line.substring(i+1).trim();
        	   if (strValueList.charAt(0)!='{' || strValueList.charAt(strValueList.length()-1)!='}')
        	   {
        		   System.err.println("Incorrect format for feature value-list: "+line);
        		   continue;
        	   }
         		   
        	   LinkedList<String> listValue = new LinkedList<String>();
        	   String [] aValueList = strValueList.substring(1, strValueList.length()-1).split("(,|\\s)+");
        	   for(String v: aValueList)
        		   listValue.add(v);
         	   
           	   if (listValue.size()<=0)
        	   {
        		   System.err.println("No value for feature "+feature+" in line: "+ line);
        		   continue;
        	   }

           	   htFeaturesValueList.put(feature, listValue);
           }

           // List of features for each cat
           htCatFeatureList = new Hashtable<String, LinkedList<String>>();
           htCatFeatureHash = new Hashtable<String, HashSet<String>>();
           
           Element eCatFeatureList =  (Element)root.getElementsByTagName(CATEGORIES_TAG).item(0);
           String [] aCatFeatureList = ((Node)(NodeList)(eCatFeatureList.getChildNodes()).item(0)).getNodeValue().trim().split("\\n");

           for(String line: aCatFeatureList)
           {
        	   line = normalize(line);
        	   if (line.isEmpty())
        		   continue;
        	   
        	   int i = line.indexOf(':');
        	   if (i<=0)
        	   {
        		   if (!line.matches("\\w+"))
        			   System.err.println("Incorrect cat feature-list: "+line);
        		   continue;
        	   }
        	   String cat = line.substring(0, i).trim();
        	   if (htCatFeatureList.containsKey(cat))
        	   {
        		   System.err.println("Duplicate cat feature-list: "+line);
        		   continue;
        	   }
        	   String strFeatureList = line.substring(i+1).trim();
        	   if (strFeatureList.charAt(0)=='{')
        			   if (strFeatureList.charAt(strFeatureList.length()-1)!='}')
        			   {
        				   System.err.println("Incorrect format for cat feature-list: "+line);
        				   continue;
        			   }
        			   else
        				   strFeatureList = strFeatureList.substring(1, strFeatureList.length()-1);
         		   
        	   LinkedList<String> lsFeature = new LinkedList<String>();
        	   String [] aFeatureList = strFeatureList.split("(,|\\s)+");
        	   for(String f: aFeatureList)
        		   if (!htFeaturesValueList.containsKey(f))
            	   {
            		   System.err.println("Feature "+f+" does not exist @ line: "+ line);
            		   continue;
            	   }
        		   else
        			   lsFeature.add(f);
         	   
               
        	   htCatFeatureList.put(cat, lsFeature);
        	   HashSet<String> hsFeature = new HashSet<String>(lsFeature);
        	   htCatFeatureHash.put(cat, hsFeature);
        	   
           }

           // Feature Structure of words, that is grammar's start symbol
           wordsFeatureStructure = new LinkedList<FeaturStructure>();
           
           Element wordStructuresElement =  (Element)root.getElementsByTagName(WORDS_TAG).item(0);
           String [] wordsFeatureStructureList = ((Node)(NodeList)(wordStructuresElement.getChildNodes()).item(0)).getNodeValue().trim().split("\\n");

           for(String line: wordsFeatureStructureList)
           {
        	   line = normalize(line);
        	   if (line.isEmpty())
        		   continue;
        	   
        	   FeaturStructure wordFS = new FeaturStructure(this);
        	   if (!wordFS.readFromString(normalize(line)))
        	   {
        		   System.err.println("Incorrect feature structure for word: "+line);
        		   continue;
        	   }
        	   
        	   wordsFeatureStructure.add(wordFS);
           }
           assert (wordsFeatureStructure.size()>0);
           
           // foot features
           Element footFeatureElement =  (Element)root.getElementsByTagName(FOOT_FEATURES_TAG).item(0);
           String footFeatureList = ((Node)(NodeList)(footFeatureElement.getChildNodes()).item(0)).getNodeValue();
           hsFootFeatures = new HashSet<String>();
           hsFootFeatures.addAll(Arrays.asList(footFeatureList.trim().split("\\n")));  //TODO Feature names need to be verified
           
           // head features
           Element headFeatureElement =  (Element)root.getElementsByTagName(HEAD_FEATURES_TAG).item(0);
           String headFeatureList = ((Node)(NodeList)(headFeatureElement.getChildNodes()).item(0)).getNodeValue();
           hsHeadFeatures = new HashSet<String>();
           hsHeadFeatures.addAll(Arrays.asList(headFeatureList.trim().split("\\n")));  //TODO Feature names need to be verified
           
          // Default values 
           htDefaultValues = new Hashtable<String, TreeMap<String, String>>();

           Element defaultValueElement =  (Element)root.getElementsByTagName(DEFAULTS_TAG).item(0);
           String [] defaultValueList = ((Node)(NodeList)(defaultValueElement.getChildNodes()).item(0)).getNodeValue().trim().split("\\n");
           
           for(String line: defaultValueList)
           {
        	   line = normalize(line);
        	   if (line.isEmpty())
        		   continue;
        	   
        	   int i = line.indexOf(':');
        	   String cat = line.substring(0, i);	//TODO cat needs to be verified
        	   TreeMap<String, String> tmDefaultAV = htDefaultValues.get(cat);
        	   if (tmDefaultAV == null)
        	   {
        		 tmDefaultAV  = new TreeMap<String, String>();
        		 htDefaultValues.put(cat, tmDefaultAV); 
        	   }
        	   
        	   String [] av = FeaturStructure.readAVFromString(line.substring(i+1));
        	   if (av==null || av.length!=2)
        	   {
        		   System.err.println("Error in default values: "+line);
        		   continue;
        	   }
        	   
        	   tmDefaultAV.put(av[0], av[1]); // TODO Feature and its value need to be verified
            }
           
           
           // reading constants
           htConstants = new Hashtable<FeaturStructure, String>();

           htConstants.put(new FeaturStructure(this, "ZS"), ZS);
           htConstants.put(new FeaturStructure(this, "NS"), NS);
           
           Element constantsElement =  (Element)root.getElementsByTagName(CONSTANTS_TAG).item(0);
           String [] constantList = ((Node)(NodeList)(constantsElement.getChildNodes()).item(0)).getNodeValue().trim().split("\\n");

           for(String line: constantList)
           {
        	   line = normalize(line);
        	   if (line.isEmpty())
        		   continue;
        	   
        	   Pair<FeaturStructure, String> constant = readConstantFromString(line);
        	   htConstants.put(constant.getFirst(), constant.getSecond());  // TODO cat, features and values should have been verified

        	   if (constant.getFirst().toString().equals(VB))
        	   {
        		   fsVB = constant.getFirst();
        		   strVB = constant.getSecond();
        	   }
        	   else
        	   if (constant.getFirst().toString().equals(VBD))
        	   {
        		   fsVBD = constant.getFirst();
        		   strVBD = constant.getSecond();
        	   }
           }
           
           
           // reading macros
           htMacros = new Hashtable<String, FeaturStructure>();
           Element macroElement =  (Element)root.getElementsByTagName(MACROS_TAG).item(0);
           String [] macroList = ((Node)(NodeList)(macroElement.getChildNodes()).item(0)).getNodeValue().trim().split("\\n");

           for(String line: macroList)
           {
        	   line = normalize(line);
        	   if (line.isEmpty())
        		   continue;
        	   
        	   Pair<String, FeaturStructure> macro = readMacroFromString(line);
        	   htMacros.put(macro.getFirst(), macro.getSecond()); //TODO cat, features and values should have been verified
           }
           
          
           // reading rules
           rules = new LinkedList<MorphologyRule>();
           
           NodeList ruleElements =  ((Element)root.getElementsByTagName(RULES_TAG).item(0)).getElementsByTagName(RULE_TAG);
           for(int i=0; i<ruleElements.getLength() ; i++){
               Element rElement = (Element)ruleElements.item(i);
               String rString = ((Node)(NodeList)(rElement.getChildNodes()).item(0)).getNodeValue().trim();
               MorphologyRule r = new MorphologyRule(this, i); //TODO replacing 'i' with the actual ID.
               
               if (!r.readFromString(rString))
               {
            	   System.err.println("Error in rule "+rElement.getAttribute("id"));
            	   continue;
               }
               
               rules.add(r);
           }
           
           rulesCount = rules.size();

        }catch (SAXParseException err) {
        System.out.println ("** Parsing error" + ", line " 
             + err.getLineNumber () + ", uri " + err.getSystemId ());
        System.out.println(" " + err.getMessage ());

        }catch (SAXException e) {
        Exception x = e.getException ();
        ((x == null) ? e : x).printStackTrace ();

        }catch (Throwable t) {
        t.printStackTrace ();
        }
    }//end of readXML

	private Pair<String, FeaturStructure> readMacroFromString(String line) {
		String [] av =  line.split(":=");
		if (av.length!=2)
			return null;
		
		av[0] = av[0].trim();

		FeaturStructure fs =  new FeaturStructure(this);
		fs.readFromString(av[1]); // TODO verify validness
		
		return new Pair<String, FeaturStructure> (av[0], fs);
	}

	private Pair<FeaturStructure, String> readConstantFromString(String line) {
		String [] av =  line.split(":=");
		if (av.length!=2)
			return null;
		
		FeaturStructure fs =  new FeaturStructure(this);
		fs.readFromString(av[0]); // TODO verify validness
		av[1] = av[1].trim();
		if (av[1].charAt(0)!='"' || av[1].charAt(av[1].length()-1)!='"')
		{
			System.err.print("Constant is not warpped within double quotes @ line: "+ line);
			return null;
		}
		
		String val = av[1].substring(1, av[1].length()-1); // strip quotations
		val = substituteConstants(val);

		return new Pair<FeaturStructure, String> (fs, val);
	}

	public String substituteConstants(String s)
	{
		int i = s.indexOf('$'), k = 0;
		while (i>=0)
		{
			if (i+1<s.length() && s.charAt(i+1)=='{')
			{
				int j = s.indexOf('}', i+1);
				if (j<0)
				{
					System.err.print("'}' was not found where expected.");
					return null;
				}
				
				// while (s.substring(j, j+1).matches("\\w")) j++;
				FeaturStructure cnstnt = new FeaturStructure(this);
				cnstnt.readFromString(s.substring(i+2,j));
				
				if (!htConstants.containsKey(cnstnt))
				{
					System.err.print("Constant "+cnstnt+ " was not found.");
					return null;
				}
				String val = htConstants.get(cnstnt);
				String cnstntVar = s.substring(i,j+1); // "\\Q"+s.substring(i,j+1)+"\\E";
				s = s.replace(cnstntVar, val);
			}
			else
				k = i+1;
			
			i = s.indexOf('$', k); // k is used only to take care of the case,
							// where cnstnt contains '$' signs which do not introduce a variable. 
							// we don't want to loop over these '$' signs for ever! TODO check!
		}
		
		return s;
	}
	
	private String normalize(String line) {
		int i = line.indexOf('#');
		if (i>=0)
			line = line.substring(0, i);
		return line.trim();
	}

	public String getConstant(String cnst)
	{
		FeaturStructure fs = new FeaturStructure(this);
		fs.readFromString(cnst);
		fs.setDefaults(htDefaultValues);
		return htConstants.get(fs);
	}
	
	public String getConstant(FeaturStructure cnst)
	{
		return htConstants.get(cnst);
	}
	
	public LinkedList <Constituent> run()
	{
		// initializing agenda
		Hashtable <FeaturStructure, Constituent> agenda = new Hashtable<FeaturStructure, Constituent>();
		for(Entry<FeaturStructure, String> e: htConstants.entrySet())
		{
			FeaturStructure fs = e.getKey();
			fs.substituteMacro(); // substituting macros
			fs.setDefaults(htDefaultValues); // instantiating default features
			
			agenda.put(fs, new Constituent(this, e.getValue()));
		}
			
		// initializing chart
		LinkedList <ActiveArc> chart = new LinkedList<ActiveArc>();
		for(MorphologyRule r: rules)
			chart.add(new ActiveArc(r));

		// running the parser
		int n;
		do
		{
			n = agenda.size();
			LinkedList <ActiveArc> new_chart = new LinkedList<ActiveArc>();
			for(ActiveArc arc: chart)
				new_chart.addAll(arc.extend(agenda));
			chart = new_chart;
		} while (agenda.size()>n); // while new constituents are built
			
		// collecting the words
		LinkedList <Constituent> lsConstituents = new LinkedList<Constituent>();
		for(Entry<FeaturStructure, Constituent> e: agenda.entrySet())
			if (e.getKey().isWord(wordsFeatureStructure))
				lsConstituents.add(e.getValue());
		
		return lsConstituents;
	}
	
	public LinkedList<Pair<String, String>> compileTransductions(LinkedList<Constituent> lsConstituents)
	{
		/* Sorting rules from biggest to smallest (in terms of size of RHS) */
		lsSortedRules = new LinkedList<MorphologyRule>(rules);
		Collections.sort(lsSortedRules, Collections.reverseOrder()); // Sorting descendingly 
		lsSortedRulesId2Rank = new Hashtable<Integer, Integer>(rules.size());
		int rank = 0;
		for(MorphologyRule r: lsSortedRules)
			lsSortedRulesId2Rank.put(r.getRuleID(), rank++);
		
		LinkedList<Pair<String, String>> lsTempTransductions = Constituent.buildPatterns(this, new HashSet<Constituent>(lsConstituents));
		LinkedList<Pair<String, String>> lsFinalTransductions = new LinkedList<Pair<String,String>>();
		
		for(Pair <String, String> p: lsTempTransductions)
			lsFinalTransductions.add(
					Constituent.reIndexGroups(p.getFirst(), p.getSecond()));
		
		return lsFinalTransductions;
	}

	public Integer getRuleIdByRank(int rank) {
		MorphologyRule r = getRulebyRank(rank); 

		if (r==null)
			return null;
		
		return r.getRuleID();
	}

	public MorphologyRule getRulebyRank(int rank) {
		if (lsSortedRules==null)
			return null;
		
		return lsSortedRules.get(rank); // TODO it is O(n) should be reduced to constant-time.
	}
	
	public Integer getRuleRankByID(int id)
	{
		return lsSortedRulesId2Rank.get(id);
	}
	
	public int getRulesCount() {
		return rulesCount;
	}

	public int nextGroupCount() {
		return groupCount++;
	}

	public String nextGroupIndex() {
		return "\\"+String.valueOf(nextGroupCount());
	}
}

