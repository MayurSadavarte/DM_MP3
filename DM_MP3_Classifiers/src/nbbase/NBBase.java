package nbbase;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

public class NBBase {
	HashMap<Integer, HashMap<Integer, Vector<Integer>>> perAttrMaps = new HashMap<Integer, HashMap<Integer, Vector<Integer>>>();
	public Vector<Vector<Integer>> trainingTuples=null;
	public Vector<String> trainingClasses=null;
	public Vector<Vector<Integer>> testingTuples=null;
	public Vector<String> testingClasses=null;
	//public Vector<Vector<Integer>> outputTuples=null;
	public Vector<String> outputClasses=null;
	public HashMap<Vector<Integer>, String> trainingMap=null;
	public HashMap<Vector<Integer>, String> inputMap = null;
	public HashMap<Vector<Integer>, String> outputMap = null;
	HashMap<String, Float> results = null;
	Vector<Long> clsStats=null;
	Vector<Double> measures= null;
	long tr_totalTuples=0;
	long tr_class0tuples=0;					//"-1" - N
	long tr_class1tuples=0;					//"+1" - P

	public NBBase(Vector<Vector<Integer>> trtuples, Vector<String> trcls, Vector<Vector<Integer>> tetuples, Vector<String> tecls) {
		trainingTuples = trtuples;
		trainingClasses = trcls;
		testingTuples = tetuples;
		testingClasses = tecls;
	}
	
	private void RemoveZeroError() {
		//for (HashMap<Integer, Vector<Integer>> tmap: perAttrMaps.values())
		//	System.out.println(tmap);
		
		for (HashMap<Integer, Vector<Integer>> tmap: perAttrMaps.values()) {
		    for (Vector<Integer> tint: tmap.values()) {
		    	if (tint.get(0) == 0) {
		    		for (Vector<Integer> tvect: tmap.values()) {
		    			Integer tempint = tvect.get(0);
		    			tvect.set(0, tempint+1);
		    		}
		    	}
		    	if (tint.get(1) == 0) {
		    		for (Vector<Integer> tvect: tmap.values()) {
		    			Integer tempint = tvect.get(1);
		    			tvect.set(1, tempint+1);
		    		}
		    	}
		    }
		}
		
		//for (HashMap<Integer, Vector<Integer>> tmap: perAttrMaps.values())
		//	System.out.println(tmap);
		
	}
	
	
	
	private void NBTrain() {
		int j=0;
		for(Vector<Integer> AttrList: trainingTuples) {
			tr_totalTuples++;
			String cls = trainingClasses.get(j);
			if(cls.equalsIgnoreCase("+1"))
				tr_class1tuples++;
			else
				tr_class0tuples++;
				
			Integer perclassval=null;
				
			for (int i=0; i<AttrList.size(); i++) {
				Integer inti = new Integer(i+1);
				Integer inttoken = AttrList.get(i);
				if (cls.equalsIgnoreCase("+1")) {
					//System.out.println("HERE");
					if(perAttrMaps.containsKey(inti)) {
						HashMap<Integer, Vector<Integer>> tmap = perAttrMaps.get(inti);
						if(tmap.containsKey(inttoken)) {
							perclassval = tmap.get(inttoken).get(1);
							tmap.get(inttoken).set(1, perclassval+1);
						}
						else {
							Vector<Integer> newEntry = new Vector<Integer>();
							newEntry.add(new Integer(0));
							newEntry.add(new Integer(1));
							tmap.put(inttoken, newEntry);
						}
					}
					else {
						HashMap<Integer, Vector<Integer>> tmap = new HashMap<Integer, Vector<Integer>>();
						Vector<Integer> newEntry = new Vector<Integer>();
						newEntry.add(new Integer(0));
						newEntry.add(new Integer(1));
						tmap.put(inttoken, newEntry);
						perAttrMaps.put(inti, tmap);
					}
				}
				else {
					//System.out.println("THERE");
					if(perAttrMaps.containsKey(inti)) {
						HashMap<Integer, Vector<Integer>> tmap = perAttrMaps.get(inti);
						if(tmap.containsKey(inttoken)) {
							perclassval = tmap.get(inttoken).get(0);
							tmap.get(inttoken).set(0, perclassval+1);
						}
						else {
							Vector<Integer> newEntry = new Vector<Integer>();
							newEntry.add(new Integer(1));
							newEntry.add(new Integer(0));
							tmap.put(inttoken, newEntry);
						}
					}
					else {
						HashMap<Integer, Vector<Integer>> tmap = new HashMap<Integer, Vector<Integer>>();
						Vector<Integer> newEntry = new Vector<Integer>();
						newEntry.add(new Integer(1));
						newEntry.add(new Integer(0));
						tmap.put(inttoken, newEntry);
						perAttrMaps.put(inti, tmap);
					}
				}
			}
			j++;
			//System.out.println(cls + AttrList.toString());				
		
		}
		
		//System.out.println(perAttrMaps);
		RemoveZeroError();
			
			//FetchStats(perAttrMaps);
	}
	
	
	private double Calculate_Conditional_Probfor0(Vector<Integer> AttrList) {
		HashMap<Integer, Vector<Integer>> perValMap = new HashMap<Integer, Vector<Integer>>();
		double condProb = 1.0;
		
		for (int i=0; i<AttrList.size(); i++) {
			Integer intkey = new Integer(i+1);
			perValMap = perAttrMaps.get(intkey);
			long cumuCnt = 0;
			long attrCnt = 0;
			for(Vector<Integer>valVect: perValMap.values()) {
				cumuCnt += valVect.elementAt(0);
			}
			//for(Integer tkey: perValMap.keySet()) {
			//	System.out.println(tkey);
			//}
			//System.out.println("AttrList val: "+AttrList.get(i));
			if (perValMap.get(AttrList.get(i)) != null)
				attrCnt = perValMap.get(AttrList.get(i)).elementAt(0);
			else {
				System.out.println("AttrNo - "+intkey);
				System.out.println("perValMap - "+perValMap);
				System.out.println("ReqVal - "+AttrList.get(i));
				
				Vector<Integer> newEntry = new Vector<Integer>();
				newEntry.add(1);
				newEntry.add(1);
				perValMap.put(AttrList.get(i), newEntry);
				for (Vector<Integer> tvect: perValMap.values()) {
	    			Integer tempint0 = tvect.get(0);
	    			Integer tempint1 = tvect.get(1);
	    			tvect.set(0, tempint0+1);
	    			tvect.set(1, tempint1+1);
	    		}
				attrCnt = perValMap.get(AttrList.get(i)).elementAt(0);
			}
			condProb = condProb * ((double)attrCnt/(double)cumuCnt);
		}
		return condProb;
	}
	
	private double Calculate_Conditional_Probfor1(Vector<Integer> AttrList) {
		HashMap<Integer, Vector<Integer>> perValMap = new HashMap<Integer, Vector<Integer>>();
		double condProb = 1.0;
		
		for (int i=0; i<AttrList.size(); i++) {
			Integer intkey = new Integer(i+1);
			perValMap = perAttrMaps.get(intkey);
			long cumuCnt = 0;
			long attrCnt = 0;
			for(Vector<Integer>valVect: perValMap.values()) {
				cumuCnt += valVect.elementAt(1);
			}
			attrCnt = perValMap.get(AttrList.get(i)).elementAt(1);
			
			condProb = condProb * ((double)attrCnt/(double)cumuCnt);
		}
		return condProb;
	}
	
	
	private Vector<Float> FetchClass(Vector<Integer> AttrList) {
		Vector<Float> cond_vect=new Vector<Float>();
		double c0Probability = ((double)tr_class0tuples/(double)tr_totalTuples);
		double c1Probability = ((double)tr_class1tuples/(double)tr_totalTuples);
		//System.out.println("NaiveBayes: c0Probability - "+c0Probability+" c1Probability - "+c1Probability);
		
		double c0CondProb = Calculate_Conditional_Probfor0(AttrList);
		double c1CondProb = Calculate_Conditional_Probfor1(AttrList);
		//System.out.println("NaiveBayes: c0CondProb - "+c0CondProb+" c1CondProb - "+c1CondProb);
		//System.out.println("Intermediate Vals - "+" "+c0Probability+" "+c1Probability+" "+c0CondProb+" "+c1CondProb);
		cond_vect.add(new Float(c0CondProb*c0Probability));
		cond_vect.add(new Float(c1CondProb*c1Probability));
		return(cond_vect);
	}
	
	
	public void NBTest() {
		//HashMap<Vector<Integer>, String> resultMap = new HashMap<Vector<Integer>, String>();
		//outputTuples = new Vector<Vector<Integer>>();
		Vector<Float> clsop = new Vector<Float>();
		outputClasses = new Vector<String>();
		
		for(Vector<Integer> AttrList: testingTuples) {
			//System.out.println(AttrList+" test input tuple");
			clsop = FetchClass(AttrList);
			//System.out.println("float output vector - "+clsop);
				
			String outputCls=null;
			if((clsop.elementAt(0).compareTo(clsop.elementAt(1)) > 0)) {
				outputCls = "-1";
			}
			else if ((clsop.elementAt(0).compareTo(clsop.elementAt(1)) == 0)) {
				System.out.println("NaiveBayes: NBTest, CondProb are same!! can this happen?? - "+clsop);
				outputCls = "+1";
			}
			else 
				outputCls = "+1";
			
			//resultMap.put(AttrList, outputCls);
			//outputTuples.add(AttrList);
			outputClasses.add(outputCls);
			
			//System.out.println(outputCls + AttrList.toString());
		}
			
		//outputMap = resultMap;
	}
	
	

	public void FetchStats()
	{
		//stats we are supposed to fetch first are
		//P, N, TP, TN, FP, FN
		//so that based on these stats we can calculate results
		long TP=0, TN=0, FP=0, FN=0, P=0, N=0;
		clsStats = new Vector<Long>();
		
		
		for(int i=0; i<testingTuples.size(); i++) {
			String clsop = outputClasses.get(i);
			
			//if (!outputTuples.get(i).equals(testingTuples.get(i)))
			//	System.out.println("NaivaBayes: some error in matching of tuples");
			
			if(!clsop.equalsIgnoreCase(testingClasses.get(i))) {
				System.out.println("Problematic Tuples - "+testingTuples.get(i)+"-"+testingClasses.get(i)+" "+"-"+outputClasses.get(i));
				if (clsop.equalsIgnoreCase("+1"))
					{FP++; N++;}
				else if(testingClasses.get(i).equalsIgnoreCase("+1"))
					{FN++; P++;}
				else
					{TN++; N++;}
			}
			else if(clsop.equalsIgnoreCase("+1"))
				{TP++; P++;}
			else
				{TN++; N++;}
		}
		
		
		clsStats.add(new Long(TP));
		clsStats.add(new Long(FN));
		clsStats.add(new Long(FP));
		clsStats.add(new Long(TN));
		clsStats.add(new Long(P));
		clsStats.add(new Long(N));
		
		System.out.println(clsStats);
		return;
		
	}
	

	private void CalculateMeasures() {
		measures = new Vector<Double> ();
		
		measures.add(new Double((clsStats.elementAt(0).doubleValue()+clsStats.elementAt(3).doubleValue())/(clsStats.elementAt(4).doubleValue()+ clsStats.elementAt(5).doubleValue())));
		measures.add(new Double((clsStats.elementAt(2).doubleValue()+clsStats.elementAt(1).doubleValue())/(clsStats.elementAt(4).doubleValue()+ clsStats.elementAt(5).doubleValue())));
		measures.add(new Double(clsStats.elementAt(0).doubleValue()/clsStats.elementAt(4).doubleValue()));
		measures.add(new Double(clsStats.elementAt(3).doubleValue()/clsStats.elementAt(5).doubleValue()));
		measures.add(new Double(clsStats.elementAt(0).doubleValue()/(clsStats.elementAt(0).doubleValue()+ clsStats.elementAt(2).doubleValue())));
		measures.add(new Double(2*measures.get(4)*measures.get(2)/(measures.get(4)+measures.get(2))));
		double b=0.5;
		measures.add(new Double((1+b*b)*measures.get(4)*measures.get(2)/(b*b*measures.get(4)+measures.get(2))));
		b=2;
		measures.add(new Double((1+b*b)*measures.get(4)*measures.get(2)/(b*b*measures.get(4)+measures.get(2))));
	
		System.out.println(measures);
	}
	
	public void NBClassify() {
		
		System.out.println("NaiveBayes: Starting Training!!");
		NBTrain();
		System.out.println("NaviveBayes: Starting Test!!");
		NBTest();
		
		System.out.println("NaivaBayes: Fetching Stats!!");
		FetchStats();
		
		System.out.println("NaiveBayes: Calculating Accuracy Measures");
		CalculateMeasures();
	}

}
