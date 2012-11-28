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
	
	
	public NaiveBayes(HashMap<Vector<Integer>, String> training_map, HashMap<Vector<Integer>, String> test_map) {
		nb_classifier = new NBBase(training_map, test_map);
	}
	
	private void NBClassify() {
		nb_classifier.NBClassify();
	}
	
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fileParser fileparserTR = new fileParser(args[0]);
		fileParser fileparserTE = new fileParser(args[1]);
		HashMap<Vector<Integer>, String> inputTRMap = fileparserTR.getTuples();
		HashMap<Vector<Integer>, String> inputTEMap = fileparserTE.getTuples();
		NaiveBayes NBClassifier = new NaiveBayes(inputTRMap, inputTEMap);
		NBClassifier.NBClassify();
	}

}
