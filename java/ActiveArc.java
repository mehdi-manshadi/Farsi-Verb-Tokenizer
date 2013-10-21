/** morphology.java
 * 
 *  @author Mehdi Manshadi 
 *  @date June 2013
 *  
 */

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;


public class ActiveArc extends MorphologyRule {

	private int rhsPosition; // current constituent on the RHS being processed
	private int fNo;  // index of current feature in catFeatureList for rhsPosition
	//private String searchText;
	//private String replaceText;
	//private int groupCount;
	private Constituent[] lsConstituents;
	
	/*public ActiveArc(Grammar g) {
		super(g);
		rhsPosition = 0;
		fNo = 0;
		//searchText = "";
		//replaceText = "";
		
		//groupCount = 0;
	}*/

	public ActiveArc(MorphologyRule r) {
		super(r);
		rhsPosition = 0;
		fNo = 0;
		//searchText = "";
		//replaceText = r.getReplaceText();
		lsConstituents = new Constituent [rhs.size()];
		
		//groupCount = 0;
	}

	public ActiveArc(ActiveArc arc) {
		super(arc);
		rhsPosition = arc.rhsPosition;
		fNo = arc.fNo;
		//searchText = arc.searchText;
		//replaceText = arc.replaceText;
		lsConstituents = Arrays.copyOf(arc.lsConstituents, rhs.size());
		
		//groupCount = arc.groupCount;
	}

	public void findText()
	{
		/*
		String fsReplaceText = c.getReplacePattern();

		//assert groupCount>0;
		for(int i=c.getGroupCount(); i>0; i--)  // updating group numbers in replacement pattern
			fsReplaceText = fsReplaceText.replace("\\"+String.valueOf(i), "\\"+String.valueOf(i+arc.groupCount));
		
		arc.searchText += c.getSearchPattern();
		arc.replaceText = arc.replaceText.replace("\\0"+String.valueOf(arc.rhsPosition+1), fsReplaceText);  
		arc.groupCount += c.getGroupCount();
		*/				
	}
	
	public LinkedList <ActiveArc> extend(Hashtable <FeaturStructure, Constituent> agenda)
	{
		/**
		 
		 *  It extends the active-arc all the way until it is completed or it hits 
		 *  an item on the RHS that cannot be found in the agenda.
		 *  If completed, the arc is added to the agenda, otherwise it is added to the chart.
		 *  The chart will be returned as the return value of the function.
		 *  Note that if a feature needs to be instantiated on the RHS, the active-arc will be replaced
		 *  with a set of new-active arcs, each corresponding to one particular assignment of values to features.
		 *  The function then will extend those new active-arcs instead. 

		 *	More on the implementation:
		 *  If this procedure is called for an object, it means that that this active-arc has
		 *  a fully-instantiated feature-structure on its RHS waiting to be fed from the agenda. 
		 *  If such feature structure is not found on the agenda, as mentioned above,
		 *  the active arc will be put in the chart and is returned.
		 *  If the procedure finds such a constituent in the agenda, 
		 *  First, it saves a pointer to this constituent and then proceeds to the next item on the RHS.
		 *  Second, it instantiates all the non-set features of this item (see below) and then searches the agenda for match. 
		 *  Following these two steps, it advances the active-arc all the way until it is completed or it hits 
		 *  an item on the RHS that cannot be found in the agenda.
		 *  A completed active-arc is added to the agenda and an incomplete one is added to the chart.
		 *  
		 *  Feature Instantiation:
		 *  Once an item on the RHS is fed, the procedure proceeds to the next item.
		 *  It instantiates all the non-set features of the item as follows.
		 *  It creates one distinct active-arc for each way of assigning values to all non-set features.    
		 *  The current active-arc will then be ignored and the process continues for the newly generated active-arcs.
		 *   
		 *  For example, let's consider the active-arc has rule with the RHS "X Y Z" with "Y" waiting to be fed, when extend() is called.
		 *  If Y is not found in the agenda, the procedure simply returns a chart containing this active arc as its single element.
		 *  In this case, no item will be added to the agenda.
		 *  Otherwise, Y is fed and the procedure proceeds to the next item, that is, Z.
		 *  Let's assume that Z has two un-set features one with two and one with three possible values. 
		 *  6 new active arcs, each corresponding to one assignment of values to featureS, will be created and the original active-arc will be ignored. 
		 *  For each of these 6 active-arcs, if Z is found in the agenda, it will be extended and since the active-arc is complete,
		 *  will be added to the agenda. Each of the 6 active-arcs whose Z is not found in the agenda is added to the chart and finally the chart is returned.
		 
		 **/
		
		LinkedList <ActiveArc> chart = new LinkedList<ActiveArc>();
		LinkedList <ActiveArc> queue = new LinkedList<ActiveArc>();
		
		queue.add(this);
		while (!queue.isEmpty())
		{
			/**
			 * This loop recursively 
			 */
			ActiveArc arc = queue.poll();
			
			FeaturStructure fs = arc.rhs.get(arc.rhsPosition);
			
			if (arc.fNo==0)
			{ /* initialize the constituent: substitute macros, set default features and spread foot features */
				
				/** Macros can only be used on RHS **/  
				fs.substituteMacro(); // TODO it makes more sense to happen off-line (that is to pre-compile it), or does it?
				//fs.setDefaults(g.getHtDefaultValues()); 	// No default features for RHS
		
				/** Head features are all the features that move from head to LHS, iff they are not already set on LHS. **/
				if (arc.head==arc.rhsPosition)
					for(String f: fs.getAttributeList())
						if ( g.getHsHeadFeatures().contains(f) && // if f is a head feature 
								g.getHtCatFeatureHash().get(arc.lhs.getCat()).contains(f) ) // if f is an attribute of lhs
							arc.lhs.setValue(f, fs.getValue(f));  // only sets the value if lhs does not already have this feature set.

				/** Foot features are all the features that move from RHS to LHS, iff they are not already set on LHS. **/
				for(String f: fs.getAttributeList())
					if ( g.getHsFootFeatures().contains(f) && // if f is a foot feature 
							g.getHtCatFeatureHash().get(arc.lhs.getCat()).contains(f) ) // if f is an attribute of lhs
						arc.lhs.setValue(f, fs.getValue(f));  // only sets the value if lhs does not already have this feature set.
			}

			String cat = fs.cat;
			LinkedList<String> lsFeatures = g.getHtCatFeatureList().get(cat); 

			// Locating the next un-set feature
			if (lsFeatures!=null)
			{
				while (arc.fNo<lsFeatures.size() && fs.getValue(lsFeatures.get(arc.fNo))!=null)
					arc.fNo++;

				if (arc.fNo<lsFeatures.size())
				{ // ranging over the values of the non-set feature
					String f = lsFeatures.get(arc.fNo);
					for(String v: g.getHtFeaturesValueList().get(f))
					{
						ActiveArc arc_new = new ActiveArc(arc);
						arc_new.rhs.get(arc_new.rhsPosition).setValue(f, v);
						arc_new.fNo++;
						// pushing up the feature if it is head/foot feature
						if ( g.getHtCatFeatureHash().get(arc_new.lhs.getCat()).contains(f) // if f belongs to the feature list of lhs
								&& (g.getHsFootFeatures().contains(f) || // if f is foot feature 
										(arc_new.head==arc_new.rhsPosition && g.getHsHeadFeatures().contains(f))) ) // if f is head feature  and this is head
							arc_new.lhs.setValue(f, v);  // only sets the value if lhs does not already have this feature set.
						queue.addLast(arc_new);
					}
					continue;
				}
			}
				
			/** 
			 * If we reach here, the current item on the RHS of arc is fully instantiated,
			 * So we need to find whether there is a constituent in the agenda matching this item or not.
			 * If not we put this arc into the chart.
			 * Otherwise,	if RHS is completed, we add this arc to the agenda,
			 *  			otherwise, we add it to the queue to extend it even more.
			 */
			
			Constituent c = agenda.get(fs); 
			if (c==null)
			{
				chart.add(new ActiveArc(arc));
				continue;
			}
			
			/* For debug
			   if (arc.rhsPosition==2 && arc.rhs.get(arc.rhsPosition).getCat().equals("V"))
			 
				arc.rhsPosition = 2;
			*/
			arc.lsConstituents[arc.rhsPosition++] = c;
			arc.fNo = 0;
			
			if (arc.rhsPosition>=arc.rhs.size())
			{ // The rule has been completed
				arc.lhs.setDefaults(g.getHtDefaultValues()); // Taking care of default features for LHS
				agenda.put(arc.lhs, new Constituent(arc)); 	// Adding new constituent to agenda
			}
			else
				queue.addLast(arc);
		}
		
		return chart;
	}

	/*private int getGroupNumber() {
		return text.split("\\(").length;
	}*/

	@Override
	public String toString()
	{
		return String.format("(%s; %d; %d; \"%s\")", super.toString(), rhsPosition, fNo/*, searchText*/);
	}

	public Constituent[] getConstituents()
	{
		return lsConstituents;
	}
	
	/*public String getSearchPattern() {
		return searchText;
	}

	public String getReplacePattern() {
		return replaceText;
	}

	public int groupCount() {
		return groupCount;
	}*/
}
