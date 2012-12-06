package classification;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import nbbase.NBBase;
import nbbase.fileParser;

public class NBAdaboost {
	private Vector<NBBase> Ensemble = null;
	private HashMap<NBBase, Double> NBWeights = new HashMap<NBBase, Double>();
	private int ensemble_size = 4;
	HashMap<Vector<Integer>, String> trainingMap=null;
	HashMap<Vector<Integer>, String> inputMap = null;
	HashMap<Vector<Integer>, Double> tupleWeights = null;
	
	
	public Vector<Vector<Integer>> trainingTuples=null;
	public Vector<String> trainingClasses=null;
	public Vector<Vector<Integer>> testingTuples=null;
	public Vector<String> testingClasses=null;
	//public Vector<Vector<Integer>> outputTuples=null;
	public Vector<String> outputClasses=null;
	public Vector<Vector<Integer>> rTrTuples=null;
	public Vector<String> rTrClasses=null;
	
	Vector<Long> clsStats= null;
	Vector<Double> measures= null;
	
	public NBAdaboost(Vector<Vector<Integer>> trtuples, Vector<String> trclasses, Vector<Vector<Integer>> tetuples, Vector<String> teclasses) {
		trainingTuples = trtuples;
		trainingClasses = trclasses;
		testingTuples = tetuples;
		testingClasses = teclasses;
		Ensemble = new Vector<NBBase>();
	}
	
	private void initializeWeights() {
		tupleWeights = new HashMap<Vector<Integer>, Double>();
		
		for(Vector<Integer> AttrList: trainingTuples) {
			tupleWeights.put(AttrList, new Double((double)1.0/(double)trainingTuples.size()));
		}
	}
	
	private Vector<Integer> getRandomWeightedTuple(double random) {
		//System.out.println("Adaboost: getRandomWeightedTuple");
		
		//Vector<Float> Weights = new Vector<Float>(tupleWeights.values());
		Vector<Vector<Integer>> AttrList = new Vector<Vector<Integer>>(tupleWeights.keySet());
		
		Collections.sort(AttrList, new Comparator<Vector<Integer>>(){
			public int compare(Vector<Integer> firstVal, Vector<Integer> secondVal){
				//String firstkey = (String)first;
				//String secondkey = (String)second;
				if(tupleWeights.get(firstVal) > tupleWeights.get(secondVal))
					return -1;
				else if(tupleWeights.get(firstVal).equals(tupleWeights.get(secondVal)))
					return 0;
				else
					return +1;
			}
		});
		
		//System.out.println(tupleWeights);
		
		Double comWt=new Double(0.0);
		Vector<Integer> AttrVal = null;
		for(Vector<Integer> tAttrVal: tupleWeights.keySet()) {
			comWt = comWt + tupleWeights.get(tAttrVal);
			if(comWt > new Double(random))
				return tAttrVal;
			AttrVal = tAttrVal;
		}
		return AttrVal;
	}
	
	
	private void fetchNewTrainingTestingSets() {
		
		long totalTuples=trainingTuples.size();
		long newTrTuples= (long)((double)totalTuples * 1);
		//long newTrTuples = totalTuples;
		
		//HashMap<Vector<Integer>, String> newTrMap = new HashMap<Vector<Integer>, String>();
		//System.out.println(tupleWeights);
		//System.out.println("Adaboost: fetchNewTrainingTestingSets");
		
		Vector<Vector<Integer>> TrTuples=new Vector<Vector<Integer>>();
		Vector<Integer>tAttrList=new Vector<Integer>();
		Vector<String> TrClsList=new Vector<String>();
	
		//System.out.println(tupleWeights.values());
		int i=0;
		while(true) {
			double random = Math.random();
			tAttrList = getRandomWeightedTuple(random);
			//if (TrTuples.contains(tAttrList))
			//	continue;
			TrTuples.add(tAttrList);
			TrClsList.add(trainingClasses.get(trainingTuples.indexOf(tAttrList)));
			i++;
			if(i == newTrTuples)
				break;
		}
		
		rTrTuples = TrTuples;
		rTrClasses = TrClsList;
		
		//System.out.println(tupleWeights);
		//System.out.println(TrTuples);
		//System.out.println(TrClsList);
	}
	
	
	private void updateWeights(NBBase newNBBase) {
		//System.out.println("Adaboost: updateWeights ################################################");
		Double error=new Double(0.0);
		//System.out.println(tupleWeights);
		
		int i=0;
		Double ttwt = new Double(0.0);
		for(Vector<Integer> AttrList: newNBBase.testingTuples) {
			
			if (!newNBBase.testingClasses.elementAt(i).equalsIgnoreCase(newNBBase.outputClasses.elementAt(i))) {
				error = error + tupleWeights.get(AttrList);
			}
			ttwt = ttwt + tupleWeights.get(AttrList);;
			i++;
		}
		
		//System.out.println("updateWeights - error1- "+error);
		error = error/ttwt;
		
		if (error.equals(new Double(0.0))) {
			//System.out.println("The Error for this NBBase seems to be zero, hence will restructure the error - "+error);
			error = error + new Double(0.01);
		}
		
		//System.out.println("updateWeights - error2- "+error);
		
		
		if (error < new Double(0.5)) {
			Ensemble.add(newNBBase);

			NBWeights.put(newNBBase, new Double(Math.log((new Double(1.0) - error)/error)));

			i=0;
			for(Vector<Integer> AttrList: newNBBase.testingTuples) {
				if (newNBBase.testingClasses.elementAt(i).equalsIgnoreCase(newNBBase.outputClasses.elementAt(i))) {
					Double uwt = tupleWeights.get(AttrList);
					uwt = uwt * (error/(new Double(1.0) - error));
					tupleWeights.put(AttrList, uwt);
				}	
				i++;
			}

			Double totalWeight=new Double(0.0);
			for(Vector<Integer> AttrList: tupleWeights.keySet()) {
				totalWeight = totalWeight + tupleWeights.get(AttrList);
			}

			//System.out.println(tupleWeights);
			//System.out.println("updateweights - totalweight- "+totalWeight);

			for(Vector<Integer> AttrList: tupleWeights.keySet()) {
				Double uwt = tupleWeights.get(AttrList);
				uwt = uwt / totalWeight;
				tupleWeights.put(AttrList, uwt);
			}

			//System.out.println(tupleWeights);
		}
	}
	
	
	
	
	private void trainensemble() {
	
		while(Ensemble.size()<ensemble_size) {
			//System.out.println("Ensemble.size - "+Ensemble.size()+" ensemble_size - "+ensemble_size);
			fetchNewTrainingTestingSets();
			//NBBase newNBBase = new NBBase(rTrTuples, rTrClasses, testingTuples, testingClasses);
			NBBase newNBBase = new NBBase(rTrTuples, rTrClasses, rTrTuples, rTrClasses);
			newNBBase.NBClassify();
			updateWeights(newNBBase);
		}

	}
	
	
	private void testensemble() {
		for(NBBase testingBase: Ensemble) {
			testingBase.testingTuples = testingTuples;
			testingBase.testingClasses = testingClasses;
			
			testingBase.NBTest();
			//testingBase.FetchStats();
		}
		ReconcileEnsemble();
	}
	
	
	
	
	private void ReconcileEnsemble() {
		//System.out.println("Adaboost: ReconcileEnsemble in Adaboost");
		int i=0;
		outputClasses = new Vector<String>();
		for(Vector<Integer> AttrList: testingTuples) {
			Double weight0cls=new Double(0.0);
			Double weight1cls=new Double(0.0);
				
			for(NBBase testingBase: Ensemble) {
				if(testingBase.outputClasses.elementAt(i).equalsIgnoreCase("-1")) {
					weight0cls = weight0cls + NBWeights.get(testingBase);
				}
				else if(testingBase.outputClasses.elementAt(i).equalsIgnoreCase("+1")) {
					weight1cls = weight1cls + NBWeights.get(testingBase);
				}
			}
			if (weight0cls == weight1cls) {
				//System.out.println("Weird case - weight0cls - "+weight0cls+", weight1cls - "+weight1cls);
				System.exit(-1);
			}
			else if(weight0cls > weight1cls) {
				outputClasses.insertElementAt("-1", i);
			}
			else {
				outputClasses.insertElementAt("+1", i);
			}
			i++;
		}
		//System.out.println("Adaboost: reconcilation done, here are the results - ");
		//System.out.println(outputClasses);
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
				//System.out.println("Problematic Tuples - "+testingTuples.get(i)+"-"+testingClasses.get(i)+" "+"-"+outputClasses.get(i));
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
		
		//System.out.println(clsStats);
		System.out.println(TP);
		System.out.println(FN);
		System.out.println(FP);
		System.out.println(TN);
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
	
		//System.out.println(measures);
	}
	
	private void EnsembleClassify() {
		initializeWeights();
		
		//System.out.println("Adaboost: Training AdaBoost");
		trainensemble();
		
		//System.out.println("Adaboost: Testing Adaboost");
		testensemble();
		
		//System.out.println("Adaboost: Fetching Adaboost Stats");
		FetchStats();
		
		//System.out.println("Adaboost: Calculating Accuracy Measures");
		CalculateMeasures();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fileParser fileparserTR = new fileParser(args[0]);
		fileParser fileparserTE = new fileParser(args[1]);
		HashMap<Vector<Integer>, String> inputTRMap = fileparserTR.getTuples();
		HashMap<Vector<Integer>, String> inputTEMap = fileparserTE.getTuples();
		
		Vector<Vector<Integer>> trtuples = new Vector<Vector<Integer>>(inputTRMap.keySet());
		Vector<String> trclasses = new Vector<String>(inputTRMap.values());
		Vector<Vector<Integer>> tetuples = new Vector<Vector<Integer>>(inputTEMap.keySet());
		Vector<String> teclasses = new Vector<String>(inputTEMap.values());
		
		//System.out.println(tetuples);
		//System.out.println(teclasses);
		NBAdaboost NBAdabooster = new NBAdaboost(trtuples, trclasses, tetuples, teclasses);
		
		NBAdabooster.EnsembleClassify();
	}

}
