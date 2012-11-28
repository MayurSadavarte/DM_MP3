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
	
	
	private Vector<HashMap<Vector<Integer>, String>> fetchNewTrainingTestingSets() {
		Vector<HashMap<Vector<Integer>, String>> tupleVector=new Vector<HashMap<Vector<Integer>, String>>();
		HashMap<Vector<Integer>, String> newTrMap = new HashMap<Vector<Integer>, String>();
		
		//System.out.println(tupleWeights);
		
		Vector<Integer> tAttrList=null;
		for(int i=0; i<trainingMap.size(); i++) {
			double random = Math.random();
			tAttrList = getRandomWeightedTuple(random);
			newTrMap.put(tAttrList, trainingMap.get(tAttrList));
		}
		
		System.out.println(tupleWeights);
		tupleVector.add(newTrMap);
		tupleVector.add(newTrMap);
		
		return tupleVector;
	}
	
	
	private void updateTupleWeights(NBBase newNBBase) {
		Float error=new Float(0.0);
		System.out.println(tupleWeights);
		
		for(Vector<Integer> AttrList: newNBBase.inputMap.keySet()) {
			if (!newNBBase.inputMap.get(AttrList).equalsIgnoreCase(newNBBase.outputMap.get(AttrList))) {
				error = error + tupleWeights.get(AttrList);
			}
		}
		
		//System.out.println(error);
		
		Float totalWeight=new Float(0.0);
		for(Vector<Integer> AttrList: newNBBase.inputMap.keySet()) {
			if (newNBBase.inputMap.get(AttrList).equalsIgnoreCase(newNBBase.outputMap.get(AttrList))) {
				Float uwt = tupleWeights.get(AttrList);
				uwt = uwt * (error/(new Float(1.0) - error));
				tupleWeights.put(AttrList, uwt);
			}	
		}
		
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
			
			Vector<HashMap<Vector<Integer>, String>> tupleVector = fetchNewTrainingTestingSets();
			NBBase newNBBase = new NBBase(tupleVector.elementAt(0), tupleVector.elementAt(1));
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
