package classification;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class NaiveBayes {
	HashMap<Integer, HashMap<Integer, Vector<Integer>>> perAttrMaps = new HashMap<Integer, HashMap<Integer, Vector<Integer>>>();
	private File tr_file = null;
	private File te_file = null;
	long tr_totalTuples=0;
	long tr_class0tuples=0;
	long tr_class1tuples=0;

	public NaiveBayes(String training_file, String test_file) {
		tr_file = new File(training_file);
		te_file = new File(test_file);
		
	}
	
	private HashMap<Integer, HashMap<Integer, Vector<Integer>>> RemoveZeroError(HashMap<Integer, HashMap<Integer, Vector<Integer>>> perAttrMaps) {
		for (HashMap<Integer, Vector<Integer>> tmap: perAttrMaps.values()) {
		    for (Vector<Integer> tint: tmap.values()) {
		    	if (tint.get(0) == 0) {
		    		for (Vector<Integer> tvect: tmap.values()) {
		    			Integer tempint = tvect.get(0);
		    			tvect.set(0, tempint+1);
		    		}
		    		
		    	}
		    	else if (tint.get(1) == 0) {
		    		for (Vector<Integer> tvect: tmap.values()) {
		    			Integer tempint = tvect.get(1);
		    			tvect.set(1, tempint+1);
		    		}
		    	}
		    }
		}
		
		return perAttrMaps;
		
	}
	
	
	private void FetchStats(HashMap<Integer, HashMap<Integer, Vector<Integer>>> perAttrMaps)
	{
		
		
		return;
		
	}
	
	
	private void NBTrain() {
		FileReader tr_reader=null;
		HashMap<Vector<Integer>, String> tuplesMap = new HashMap<Vector<Integer>, String>();
		
		//HashMap<Integer, Vector<Integer>> perValMap = new HashMap<Integer, Vector<Integer>>();
		
		
		
		try {
			tr_reader = new FileReader(tr_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			BufferedReader tr_buffer = new BufferedReader(tr_reader);
			String tr_line;
			
			while ((tr_line = tr_buffer.readLine()) != null) {
				// process the line.
				tr_totalTuples++;
				String[] tokens = tr_line.split("\t");
			
				Vector<Integer> AttrList=null;
				String cls = tokens[0];
				if(cls.equalsIgnoreCase("-1"))
					tr_class0tuples++;
				else
					tr_class1tuples++;
				
				Integer perclassval=null;
				
				for (int i=1; i<tokens.length; i++) {
					Integer inti = new Integer(i);
					Integer inttoken = new Integer(tokens[i]);
					if(AttrList == null) {
						AttrList = new Vector<Integer>(inttoken);
					}
					AttrList.add(inttoken);
					
					if (cls.equalsIgnoreCase("+1")) {
						
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
							HashMap<Integer, Vector<Integer>> tmap = new HashMap();
							Vector<Integer> newEntry = new Vector<Integer>();
							newEntry.add(new Integer(0));
							newEntry.add(new Integer(1));
							tmap.put(inttoken, newEntry);
							perAttrMaps.put(inti, tmap);
						}
					}
					else {
						
						if(perAttrMaps.containsKey(inti)) {
							HashMap<Integer, Vector<Integer>> tmap = perAttrMaps.get(inti);
							if(tmap.containsKey(inttoken)) {
								perclassval = tmap.get(inttoken).get(0);
								tmap.get(inttoken).set(1, perclassval+1);
							}
							else {
								Vector<Integer> newEntry = new Vector<Integer>();
								newEntry.add(new Integer(1));
								newEntry.add(new Integer(0));
								tmap.put(inttoken, newEntry);
							}
						}
						else {
							HashMap<Integer, Vector<Integer>> tmap = new HashMap();
							Vector<Integer> newEntry = new Vector<Integer>();
							newEntry.add(new Integer(1));
							newEntry.add(new Integer(0));
							tmap.put(inttoken, newEntry);
							perAttrMaps.put(inti, tmap);
						}
						
					}
				}
				tuplesMap.put(AttrList, cls);
				System.out.println(cls + AttrList.toString());				
				
			}
			tr_buffer.close();
			
			perAttrMaps = RemoveZeroError(perAttrMaps);
			
			FetchStats(perAttrMaps);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	private double Calculate_Conditional_Probfor0(Vector<Integer> AttrList) {
		HashMap<Integer, Vector<Integer>> perValMap = new HashMap<Integer, Vector<Integer>>();
		double condProb = 1.0;
		
		for (int i=0; i<AttrList.size(); i++) {
			Integer intkey = new Integer(i+1);
			perValMap = perAttrMaps.get(intkey);
			//if(perValMap == null) {
			//	System.out.println("WTF!!");
			//}
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
		FileReader te_reader=null;
		HashMap<Vector<Integer>, String> tuplesMap = new HashMap<Vector<Integer>, String>();
		HashMap<Vector<Integer>, String> outputMap = new HashMap<Vector<Integer>, String>();
		//HashMap<Integer, Vector<Integer>> perValMap = new HashMap<Integer, Vector<Integer>>();
		Vector<Float> clsop = new Vector<Float>();
		
		try {
			te_reader = new FileReader(te_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			BufferedReader te_buffer = new BufferedReader(te_reader);
			String te_line;
			
			while ((te_line = te_buffer.readLine()) != null) {
				// process the line.
				String[] tokens = te_line.split("\t");
				String cls = tokens[0];
				
				Vector<Integer> AttrList=null;
				
				for (int i=1; i<tokens.length; i++) {
					Integer inti = new Integer(i);
					Integer inttoken = new Integer(tokens[i]);
					if(AttrList == null) {
						AttrList = new Vector<Integer>(inttoken);
					}
					AttrList.add(inttoken);
				}
				tuplesMap.put(AttrList, cls);
				
				clsop = FetchClass(AttrList);
				
				//System.out.println("float output vector - "+clsop);
				
				String outputCls=null;
				if(clsop.elementAt(0) > clsop.elementAt(1)) {
					outputCls = "-1";
				}
				else {
					outputCls = "+1";
				}
				outputMap.put(AttrList, outputCls);
				
				System.out.println(outputCls + AttrList.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void NBClassify() {
		
		System.out.println("Starting Training!!");
		NBTrain();
		System.out.println("Starting Test!!");
		NBTest();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NaiveBayes NBClassifier = new NaiveBayes(args[0], args[1]);
		NBClassifier.NBClassify();
	}

}
