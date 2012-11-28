package nbbase;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

public class NBBase {
	HashMap<Integer, HashMap<Integer, Vector<Integer>>> perAttrMaps = new HashMap<Integer, HashMap<Integer, Vector<Integer>>>();
	public HashMap<Vector<Integer>, String> trainingMap=null;
	public HashMap<Vector<Integer>, String> inputMap = null;
	public HashMap<Vector<Integer>, String> outputMap = null;
	HashMap<String, Float> results = null;
	Vector<Long> clsStats= new Vector<Long>();
	long tr_totalTuples=0;
	long tr_class0tuples=0;					//"-1" - N
	long tr_class1tuples=0;					//"+1" - P

	public NBBase(HashMap<Vector<Integer>, String> training_map, HashMap<Vector<Integer>, String> test_map) {
		trainingMap = training_map;
		inputMap = test_map;
		
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
	
	
	private void FetchStats()
	{
		//stats we are supposed to fetch first are
		//P, N, TP, TN, FP, FN
		//so that based on these stats we can calculate results
		long TP=0, TN=0, FP=0, FN=0;
		
		for(Vector<Integer> attrList: outputMap.keySet()) {
			if(!outputMap.get(attrList).equalsIgnoreCase(inputMap.get(attrList))) {
				if (outputMap.get(attrList).equalsIgnoreCase("+1"))
					FP++;
				else if(inputMap.get(attrList).equalsIgnoreCase("+1"))
					FN++;
				else
					TN++;
			}
			else if(outputMap.get(attrList).equalsIgnoreCase("+1"))
				TP++;
			else
				TN++;
		}
		
		
		clsStats.add(new Long(TP));
		clsStats.add(new Long(TN));
		clsStats.add(new Long(FP));
		clsStats.add(new Long(FN));
		
		System.out.println(clsStats);
		return;
		
	}
	
	
	
	private void CalculateResults() {
		return;
	}
	
	
	private void NBTrain() {
		
		for(Vector<Integer> AttrList: trainingMap.keySet()) {
			tr_totalTuples++;
			String cls = trainingMap.get(AttrList);
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
			System.out.println(cls + AttrList.toString());				
		
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
			attrCnt = perValMap.get(AttrList.get(i)).elementAt(0);
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
		
		double c0CondProb = Calculate_Conditional_Probfor0(AttrList);
		double c1CondProb = Calculate_Conditional_Probfor1(AttrList);
		
		//System.out.println("Intermediate Vals - "+" "+c0Probability+" "+c1Probability+" "+c0CondProb+" "+c1CondProb);
		cond_vect.add(new Float(c0CondProb*c0Probability));
		cond_vect.add(new Float(c1CondProb*c1Probability));
		return(cond_vect);
	}
	
	
	private void NBTest() {
		HashMap<Vector<Integer>, String> resultMap = new HashMap<Vector<Integer>, String>();
		
		Vector<Float> clsop = new Vector<Float>();

		for(Vector<Integer> AttrList: inputMap.keySet()) {
			//System.out.println(AttrList+" test input tuple");
			clsop = FetchClass(AttrList);
			//System.out.println("float output vector - "+clsop);
				
			String outputCls=null;
			if(clsop.elementAt(0) > clsop.elementAt(1)) {
				outputCls = "-1";
			}
			else {
				outputCls = "+1";
			}
			resultMap.put(AttrList, outputCls);
			
			System.out.println(outputCls + AttrList.toString());
		}
			
		outputMap = resultMap;
	}
	
	
	
	
	public void NBClassify() {
		
		System.out.println("Starting Training!!");
		NBTrain();
		System.out.println("Starting Test!!");
		NBTest();
		System.out.println("Fetching Stats!!");
		FetchStats();
		System.out.println("Calculating Results!!");
		CalculateResults();
	}

}
