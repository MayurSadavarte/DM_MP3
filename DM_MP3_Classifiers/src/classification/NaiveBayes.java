package classification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import nbbase.NBBase;
import nbbase.fileParser;

public class NaiveBayes {
	private NBBase nb_classifier=null;
	
	
	public NaiveBayes(Vector<Vector<Integer>> trtuples, Vector<String> trcls, Vector<Vector<Integer>> tetuples, Vector<String> tecls) {
		nb_classifier = new NBBase(trtuples, trcls, tetuples, tecls);
	}
	
	private void NBClassify() {
		nb_classifier.NBClassify();

		//System.out.println("NaivaBayes: Fetching Stats!!");
		nb_classifier.FetchStats();
				
				//System.out.println("NaiveBayes: Calculating Accuracy Measures");
		nb_classifier.CalculateMeasures();
	}
	
	/*
	private static HashMap<Vector<Integer>, String> getTuples(String fileName) {
		HashMap<Vector<Integer>, String> tuplesMap = new HashMap<Vector<Integer>, String>();
		FileReader file_reader=null;
		
		try {
			file_reader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			BufferedReader f_buffer = new BufferedReader(file_reader);
			String f_line;
			
			while ((f_line = f_buffer.readLine()) != null) {
				// process the line.
				String[] tokens = f_line.split("\t");
				
				Vector<Integer> AttrList=null;
				String cls = tokens[0];
					
				Integer perclassval=null;
				
				for (int i=1; i<tokens.length; i++) {
					Integer inti = new Integer(i);
					Integer inttoken = new Integer(tokens[i]);
					if(AttrList == null) {
						AttrList = new Vector<Integer>(inttoken);
					}
					AttrList.add(inttoken);
				}
				tuplesMap.put(AttrList, cls);
			}
			f_buffer.close();
			
			//FetchStats(perAttrMaps);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tuplesMap;
	}
	*/
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fileParser fileparserTR = new fileParser(args[0]);
		fileParser fileparserTE = new fileParser(args[1]);
		HashMap<Vector<Integer>, String> inputTRMap = fileparserTR.getTuples();
		HashMap<Vector<Integer>, String> inputTEMap = fileparserTE.getTuples();
		Vector<Vector<Integer>> trtuples=new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> tetuples=new Vector<Vector<Integer>>();
		Vector<String> trcls=new Vector<String>();
		Vector<String> tecls=new Vector<String>();
		for (Vector<Integer> AttrVal: inputTRMap.keySet()) {
			trtuples.add(AttrVal);
			trcls.add(inputTRMap.get(AttrVal));
		}
		for (Vector<Integer> AttrVal: inputTEMap.keySet()) {
			tetuples.add(AttrVal);
			tecls.add(inputTEMap.get(AttrVal));
		}
		
		NaiveBayes NBClassifier = new NaiveBayes(trtuples, trcls, tetuples, tecls);
		NBClassifier.NBClassify();
	}

}
