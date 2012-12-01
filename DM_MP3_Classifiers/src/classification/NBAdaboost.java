package classification;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import nbbase.NBBase;
import nbbase.fileParser;

public class NBAdaboost {
	private Vector<NBBase> Ensemble = null;
	private HashMap<NBBase, Float> NBWeights = null;
	private int ensemble_size = 5;
	HashMap<Vector<Integer>, String> trainingMap=null;
	HashMap<Vector<Integer>, String> inputMap = null;
	HashMap<Vector<Integer>, Float> tupleWeights = null;
	
	public Vector<Vector<Integer>> trainingTuples=null;
	public Vector<String> trainingClasses=null;
	public Vector<Vector<Integer>> testingTuples=null;
	public Vector<String> testingClasses=null;
	
	
	public NBAdaboost(HashMap<Vector<Integer>, String> inputTRMap, HashMap<Vector<Integer>, String> inputTEMap) {
		trainingMap = inputTRMap;
		inputMap = inputTEMap;
		Ensemble = new Vector<NBBase>();
	}
	
	private void initializeWeights() {
		tupleWeights = new HashMap<Vector<Integer>, Float>();
		
		for(Vector<Integer> AttrList: trainingMap.keySet()) {
			tupleWeights.put(AttrList, new Float((double)1.0/(double)trainingMap.keySet().size()));
		}
	}
	
	private Vector<Integer> getRandomWeightedTuple(double random) {
		Vector<Float> Weights = new Vector<Float>(tupleWeights.values());
	
		Collections.sort(Weights, new Comparator<Float>(){
			public int compare(Float firstVal, Float secondVal){
				//String firstkey = (String)first;
				//String secondkey = (String)second;
				if(firstVal > secondVal)
					return -1;
				else
					return +1;
			}
		});
		
		//System.out.println(tupleWeights);
		
		Float comWt=new Float(0.0);
		Vector<Integer> AttrVal = null;
		for(Vector<Integer> tAttrVal: tupleWeights.keySet()) {
			comWt = comWt + tupleWeights.get(tAttrVal);
			if(comWt > new Float(random))
				return tAttrVal;
			AttrVal = tAttrVal;
		}
		return AttrVal;
	}
	
	
	private void fetchNewTrainingTestingSets() {
		
		//HashMap<Vector<Integer>, String> newTrMap = new HashMap<Vector<Integer>, String>();
		
		//System.out.println(tupleWeights);
		
		Vector<Vector<Integer>> TrTuples=new Vector<Vector<Integer>>();
		Vector<Integer>tAttrList=new Vector<Integer>();
		Vector<String> TrClsList=new Vector<String>();
		for(int i=0; i<trainingMap.size(); i++) {
			double random = Math.random();
			tAttrList = getRandomWeightedTuple(random);
			TrTuples.add(tAttrList);
			TrClsList.add(trainingMap.get(tAttrList));
		}
		
		trainingTuples=TrTuples;
		trainingClasses=TrClsList;
		
		System.out.println(tupleWeights);
		System.out.println(TrTuples);
	}
	
	
	private void updateTupleWeights(NBBase newNBBase) {
		Float error=new Float(0.0);
		System.out.println(tupleWeights);
		
		int i=0;
		for(Vector<Integer> AttrList: newNBBase.trainingTuples) {
			
			if (!newNBBase.trainingClasses.elementAt(i).equalsIgnoreCase(newNBBase.outputClasses.elementAt(i))) {
				error = error + tupleWeights.get(AttrList);
			}
			i++;
		}
		
		//System.out.println(error);
		
		i=0;
		for(Vector<Integer> AttrList: newNBBase.trainingTuples) {
			if (newNBBase.trainingClasses.elementAt(i).equalsIgnoreCase(newNBBase.outputClasses.elementAt(i))) {
				Float uwt = tupleWeights.get(AttrList);
				uwt = uwt * (error/(new Float(1.0) - error));
				tupleWeights.put(AttrList, uwt);
			}	
			i++;
		}
		
		Float totalWeight=new Float(0.0);
		for(Vector<Integer> AttrList: tupleWeights.keySet()) {
			totalWeight = totalWeight + tupleWeights.get(AttrList);
		}
		
		//System.out.println(tupleWeights);
		//System.out.println(totalWeight);
		
		for(Vector<Integer> AttrList: tupleWeights.keySet()) {
			Float uwt = tupleWeights.get(AttrList);
			uwt = uwt / totalWeight;
			tupleWeights.put(AttrList, uwt);
		}
	
		System.out.println(tupleWeights);
	}
	
	private void trainensemble() {
	
		for(int i=0; i<ensemble_size; i++) {
			
			fetchNewTrainingTestingSets();
			NBBase newNBBase = new NBBase(trainingTuples, trainingClasses, trainingTuples, trainingClasses);
			Ensemble.add(newNBBase);
			newNBBase.NBClassify();
			updateTupleWeights(newNBBase);
			
		}

	}
	
	private void EnsembleClassify() {
		initializeWeights();
		
		trainensemble();
		
		//testensemble();
		
		//fetchStats();
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
		
		
		NBAdaboost NBAdabooster = new NBAdaboost(inputTRMap, inputTEMap);
		
		NBAdabooster.EnsembleClassify();
	}

}
