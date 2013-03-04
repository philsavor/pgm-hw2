/*
 * Author:Xiangyu Shen
 * This file implement the Class GraphicalModel.
 */

package pgm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphicalModel {
	/*
	 * Represent a Bayesian network or Markov network
	 */
	 private int type;  //0:BN 1:MN
     private int numOfVariables;
     private List<Factor> factors = new ArrayList<Factor>();
     private List<Integer> evidences = new ArrayList<Integer>();
     private List<Integer> order = new ArrayList<Integer>();  
     
     public static void main(String[] args){
    	 //arg0:*.uai   arg1:*.uai.evid
    	 if(args.length == 2){
    		 GraphicalModel gm = new GraphicalModel();
    		 //set factors and evidences
    		 gm.read(args[0], args[1]);
    		 gm.instantiate();
    		 gm.computeOrder();
    		 gm.eliminate();  //compute final result
    		 gm.output();
    	 }else{
    		 System.out.println("Please input two arguments!");
    		 System.exit(1);
    	 }
     }
     
     
     /*
      * read
      */
     private void read(String gmFileName, String evidFileName){
        List<String> gmData = new ArrayList<String>();
 		
 		//deal with the gm file in UAI format
 		BufferedReader br = null; 
 		try {
 			String sCurrentLine;
 			br = new BufferedReader(new FileReader(gmFileName));
             
 			while ((sCurrentLine = br.readLine()) != null) {
 				if(sCurrentLine.trim().length() > 0)
 					gmData.add(sCurrentLine);
 			}

 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (br != null)br.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 		
 		//from UAI format to factor objects
 		for(int i = 0 ; i < gmData.size(); i++){
 			String currentLine = gmData.get(i);
 			
 			if(i == 0){
 				if(currentLine.equals("MARKOV")){
 					this.type = 1;  //0: BN  1: MN
 					System.out.println("MARKOV NETWORK!");
 				}
 				else if(currentLine.equals("BAYES")){
 					this.type = 0;
 					System.out.println("BAYESIAN NETWORK!");
 				}
 			}else{
 				//Markov Network
 				//if(this.type == 1){
 					if(i == 1){
 						this.numOfVariables = Integer.parseInt(currentLine);
 						
 						//deal with evidence file
 				    	List<String> evidData = new ArrayList<String>();
 				   		
 				   		//deal with the evidence file in UAI format
 				   		BufferedReader brEvid = null; 
 				   		try {
 				   			String sCurrentLine;
 				   			brEvid = new BufferedReader(new FileReader(evidFileName));
 				               
 				   			while ((sCurrentLine = brEvid.readLine()) != null) {
 				   				if(sCurrentLine.trim().length() > 0)
 				   					evidData.add(sCurrentLine);
 				   			}

 				   		} catch (IOException e) {
 				   			e.printStackTrace();
 				   		} finally {
 				   			try {
 				   				if (brEvid != null) brEvid.close();
 				   			} catch (IOException ex) {
 				   				ex.printStackTrace();
 				   			}
 				   		}
 				   		
 				   		for(int j = 0 ; j < this.numOfVariables; j++){
 				   			this.evidences.add(-1);
 				   		}
 				   		//set evidence
 				   		int numEvid = Integer.parseInt(evidData.get(0));
 				   		for(int j = 1 ; j <= numEvid ; j++){
 				   			String Line = evidData.get(j);
 				   			String values[] = Line.split("\\s+");
 				   			
 				   			int index;
 				   			int value;
 				   			if(values[0].equals("")){
 				   			    index = Integer.parseInt(values[1]);
				   			    value = Integer.parseInt(values[2]);
 				   			}
 				   			else{
 				   			    index = Integer.parseInt(values[0]);
 				   			    value = Integer.parseInt(values[1]);
 				   			}	
 				   			
 				   			this.evidences.set(index, value);
 				   		}
 				   		
 						//get next line
 						currentLine = gmData.get(++i);
 						
 						//create variables
 						List<Variable> vars = new ArrayList<Variable>();
 						String domainSizes[] = currentLine.split("\\s+");
 						for(int j = 0 ; j < domainSizes.length ; j++){
 							//new variable
 							Variable v = new Variable();
 							v.setSize(Integer.parseInt(domainSizes[j]));
 							v.setIndex(j);
 							//not equal to -1
 							if(!this.evidences.get(j).equals(-1)){
 								v.setEvidence();	
 							}
 							v.setEvidValue(this.evidences.get(j));
 							
 							vars.add(v);
 						}
 						
 						//get next line
 						currentLine = gmData.get(++i);
 						
 						//get number of factors
 						int numOfFactors = Integer.parseInt(currentLine);
 						
 						//put variables into each factor
 						for(int j = 0 ; j < numOfFactors ; j++){
 							//get next line
 	 						currentLine = gmData.get(++i);
 	 						
 	 						//creat factor
 	 						Factor fac = new Factor();
 	 						
 	 						String varsInFactor[] = currentLine.split("\\s+");
 	 						int num = Integer.parseInt(varsInFactor[0]);
 	 						for(int k = 1 ; k <= num ; k++){
 	 							int index = Integer.parseInt(varsInFactor[k]);
 	 							
 	 							fac.addVariable(vars.get(index));
 	 						}
 	 						
 	 						this.factors.add(fac);
 						}
 						
 						//put value of talbe into each factor
                        for(int j = 0 ; j < numOfFactors ; j++){
                        	//get next line
 	 						currentLine = gmData.get(++i);
 	 						int num = Integer.parseInt(currentLine);
 	 						
 	 						for(int k = 0 ; k < num ; ){
 	 						    //get next line
 	 	 						currentLine = gmData.get(++i);
 	 	 						String valuesOfTable[] = currentLine.split("\\s+");
 	 							
 	 							for(int l = 0 ; l < valuesOfTable.length; l++){
 	 								if(valuesOfTable[l].equals("")){
 	 									continue;
 	 								}
 	 								Double value = Double.parseDouble(valuesOfTable[l]);
 	 								this.factors.get(j).addValueToTable(value);
 	 								k++;
 	 							}
 	 							
 	 						}
 						}
 					}
 				//}
 				
 				
 				//Bayeisan Network
 				//else if(this.type == 0){
 					
 				//}
 				
 				System.out.println("Finish reading!!!");
 				
 			}
 		}
     }
     
     /*
      *instantiate evidence
      */
     private void instantiate(){
    	 //1:MN 0:BN
    	 if(this.type == 1){
    		 System.out.println("MN: instantiate...");
    		 
    		//for each facotr
        	 for(int i = 0 ; i < this.factors.size(); i++){
        		 Factor fa = this.factors.get(i);
        		 
        		 //variables e.g. 3 4 5
        		 List<Integer> vars = new ArrayList<Integer>();
        		 //size for each variable e.g. 2(3) 2(4) 3(5)
        		 List<Integer> sizes = new ArrayList<Integer>();
        		 
        		 int numOfVars = fa.getNumOfVariables();
        		 for(int j = 0 ; j < numOfVars ; j++){
        			 vars.add(fa.getVariable(j).getIndex());
        			 sizes.add(fa.getVariable(j).getSize());
        		 }
        		 
        		 //test!!!!!!!!
        		 //System.out.println(vars);
        		 //System.out.println(sizes);
        		 
        		 //to mark which row in table should be removed
        		 List<Boolean> removeMarks = new ArrayList<Boolean>();
        		 for(int j = 0 ; j < fa.getNumOfValuesInTable(); j++){
        			 removeMarks.add(false);
        		 }
        		 
        		 //scan each value in table
        		 for(int j = 0 ; j < fa.getNumOfValuesInTable(); j++){
        			 //
        			 //compute positions
        			 //
        			 //e.g. 2(3) 2(4) 3(5)
        			 //e.g. 4 -> 0 1 1
        			 List<Integer> positions = new ArrayList<Integer>();
        			 int position = 0;
        			 int sum = fa.getNumOfValuesInTable();
        			 int unitSize = 0;
        			 int size = 1;
        			 int relativeJ = j;
           			 for(int k = 0 ; k < vars.size(); k++){
           				 //unitSize
        				 size = sizes.get(k) * size;
        				 //relative j = old relative j - old unit size * old position
        				 relativeJ = relativeJ - unitSize * position;
        				 //new unitSize
        				 unitSize = sum / size;
        			     //position
        				 position = relativeJ / unitSize;
        				 
        				 positions.add(position);
        			 }
           			 
           			 //test!!!
//                   System.out.println(positions);
           			 
           			 //
           			 //mark which one should be removed
           			 //
           			 for(int k = 0 ; k < vars.size(); k++){
           				 //e.g. index:3  pos:0
           				 int index = vars.get(k);
           				 int pos = positions.get(k);
           				 
           				 //not evidence
           				 int evidValue = this.evidences.get(index).intValue();
           				 if(evidValue != -1 && evidValue != pos){
           					 removeMarks.set(j, true);
           					 break;
           				 }
           			 }
           			 
        		 }
        		 
        		 //test!!!
//        		 System.out.println(removeMarks);
//        		 fa.showTableValues();
        		 
        		 //
        		 //remove marked row
        		 //
        		 for(int j = 0 ; j < removeMarks.size(); j++){
        			 if(removeMarks.get(j) == true){
        				 fa.removeValueInTable(j);
        				 removeMarks.remove(j--);
        			 }
        		 }
        		 
        		 //test!!!
//        		 System.out.println(removeMarks);
//        		 fa.showTableValues();
//        		 this.factors.get(i).showTableValues();
        	 }

    	 }
    	 //for BN
    	 else{
    		 System.out.println("BN: instantiate...");
    		 
    		 //for BN : factor is variables, the last one is the variable, others are father nodes
    		 for(int i = 0 ; i < this.factors.size(); i++){
    			 Factor fac = this.factors.get(i);
    			 
    			 //no father nodes
//    			 if(fac.getNumOfVariables() == 1) continue;
    			 
    			 //variables e.g. 3 4 5
        		 List<Integer> vars = new ArrayList<Integer>();
        		 //size for each variable e.g. 2(3) 2(4) 3(5)
        		 List<Integer> sizes = new ArrayList<Integer>();
        		 
        		 int numOfVars = fac.getNumOfVariables();
        		 for(int j = 0 ; j < numOfVars ; j++){
        			 vars.add(fac.getVariable(j).getIndex());
        			 sizes.add(fac.getVariable(j).getSize());
        		 }
        		 
        		 //test!!!!!!!!
//        		 System.out.println(vars);
//        		 System.out.println(sizes);
        		 
        		 //to mark which row in table should be removed
        		 List<Boolean> removeMarks = new ArrayList<Boolean>();
        		 for(int j = 0 ; j < fac.getNumOfValuesInTable(); j++){
        			 removeMarks.add(false);
        		 }
        		 
        		 //scan each value in table
        		 for(int j = 0 ; j < fac.getNumOfValuesInTable(); j++){
        			 //
        			 //compute positions
        			 //
        			 //e.g. 2(3) 2(4) 3(5)
        			 //e.g. 4 -> 0 1 1
        			 List<Integer> positions = new ArrayList<Integer>();
        			 int position = 0;
        			 int sum = fac.getNumOfValuesInTable();
        			 int unitSize = 0;
        			 int size = 1;
        			 int relativeJ = j;
           			 for(int k = 0 ; k < vars.size(); k++){
           				 //unitSize
        				 size = sizes.get(k) * size;
        				 //relative j = old relative j - old unit size * old position
        				 relativeJ = relativeJ - unitSize * position;
        				 //new unitSize
        				 unitSize = sum / size;
        			     //position
        				 position = relativeJ / unitSize;
        				 
        				 positions.add(position);
        			 }
           			 
           			 //test!!!
//           	     System.out.println(positions);
           			 
           			 //
           			 //mark which one should be removed
           			 //
           			 for(int k = 0 ; k < vars.size(); k++){
           				 //e.g. index:3  pos:0
           				 int index = vars.get(k);
           				 int pos = positions.get(k);
           				 
           				 //not evidence
           				 int evidValue = this.evidences.get(index).intValue();
           				 if(evidValue != -1 && evidValue != pos){
           					 removeMarks.set(j, true);
           					 break;
           				 }
           			 }
           			 
        		 }
        		 
        		 //test!!!
//        		 System.out.println(removeMarks);
//        		 fac.showTableValues();
        		 
        		 //
        		 //remove marked row
        		 //
        		 for(int j = 0 ; j < removeMarks.size(); j++){
        			 if(removeMarks.get(j) == true){
        				 fac.removeValueInTable(j);
        				 removeMarks.remove(j--);
        			 }
        		 }
        		 
        		 //test!!!
//        		 System.out.println(removeMarks);
//        		 fac.showTableValues();
//        		 this.factors.get(i).showTableValues();
        		 
        		 
    		 }
    	 }
      }
     
     /*
      *compute order and put the result to this.order
      */
     private void computeOrder(){
    	 if(this.type == 1){
    		 System.out.println("MN: compute order...");
    		 //count of edges for each node
        	 List<Integer> counts = new ArrayList<Integer>();
        	 for(int i = 0 ; i < this.numOfVariables ; i++){
        		 counts.add(0);
        	 }
        	 
        	 //compute counts
        	 for(int i = 0 ; i < this.factors.size() ; i++){
        		 //countTemp is added to each count of node
        		 int countTemp = this.factors.get(i).getNumOfVariables() - 1;
        		 
        		 //add countTemp to count of node
        		 for(int j = 0 ; j < this.factors.get(i).getNumOfVariables(); j++){
        			 int index = this.factors.get(i).getVariable(j).getIndex();
        			 int value = counts.get(index).intValue() + countTemp;
        			 
        			 counts.set(index, value);
        		 }
        	 }
        	 
        	 //initialize order
        	 for(int i = 0 ; i < counts.size() ; i++){
        		 this.order.add(i);
        	 }
        	 
        	 //compute order
        	 for(int i = 0 ; i < counts.size() ; i++){
        		int min = counts.get(i).intValue();
        		int position = i;
        		for(int j = i + 1 ; j < counts.size(); j++){
        			if (counts.get(j).intValue() < min){
        				min = counts.get(j).intValue();
        				position = j;
        			}
        		}
        		
        		counts.set(position, counts.get(i).intValue());
        		counts.set(i, min);
        		
        		int temp = order.get(position).intValue();
        		order.set(position, order.get(i).intValue());
        		order.set(i, temp);
        	 }
        	 
        	 //remove evidence
        	 
        	 //test!!!
//        	 System.out.println(this.order);
        	 
        	 for(int i = 0 ; i < this.order.size(); i++){
        		 //evidence
        		 if(this.evidences.get(this.order.get(i)) != -1){
        			 this.order.remove(i--);
        		 }
        	 }
        	 
        	 //test!!!
//        	 System.out.println(this.order);
    	 }
    	 
    	 //for BN
    	 else{
    		 System.out.println("BN: compute order...");
    		 
    		 //test!!!
//    		 System.out.println(this.numOfVariables);
    		 
    		 //each node in order for each loop
    		 for(int i = 0 ; i < this.factors.size(); i++){
    			 
    			 //compute degree
    			 for(int j = 0 ; j < this.factors.size(); j++){
    				 Factor f = this.factors.get(j);
    				 
    				 if(this.order.contains(j)) continue;
    				 
    				 if(f.getNumOfVariables() == 1 ){
    					 this.order.add(j);
    					 break;
    				 }
    				 else{
    					 int count = 0;
    					 for(int k = 0 ; k < f.getNumOfVariables()-1 ; k++){
    						 int index = f.getVariable(k).getIndex();
    						 
    						 if( ! this.order.contains(index) ) 
    							 count++;
    					 }
    					 
    					 if(count == 0){
    						 this.order.add(j);
        					 break;
    					 }
    				 }
    			 }
    			 
    			 
    		 }
    		 
    		 //test!!!
//    		 System.out.println(this.order);
//    		 System.out.println(this.order.size());
    		 
    		 //delete useless nodes in order
    		 List<Integer> keyNodes = new ArrayList<Integer>();
    		 for(int i = 0 ; i < this.evidences.size(); i++){
    			 if(this.evidences.get(i) != -1)
    				 keyNodes.add(i);
    		 }
    		    			     			 
    		 for(int j = 0 ; j < keyNodes.size(); j++){
				 Factor f = this.factors.get(keyNodes.get(j));
				 
				 for(int k = 0 ; k < f.getNumOfVariables(); k++){
					 int index = f.getVariable(k).getIndex();
					 if(!keyNodes.contains(index)){
						 keyNodes.add(index);
					 }
							 
				 }
			 }	 
    		 
    		 //test!!!
//    		 System.out.println(keyNodes);
    			 
    		 for(int i = 0 ; i < this.order.size(); i++){
    			 if(! keyNodes.contains(this.order.get(i))){
    				 this.order.remove(i);
    				 i--;
    			 }
    		 }
    		 
    		 //test!!!
//    		 System.out.println(this.order);
    		 
    	 }	 
    	 
     }
     
     /*
      * product
      */
     Factor product(Factor fac1, Factor fac2){
    	 if(this.type == 1){
    		 List<Variable> fac1Vars = fac1.getAllVariables();
        	 List<Double> fac1Table = fac1.getAllValuesInTables();
        	 
        	 List<Variable> fac2Vars = fac2.getAllVariables();
        	 List<Double> fac2Table = fac2.getAllValuesInTables();
        	 
        	 //set variables
        	 List<Variable> resVars = new ArrayList<Variable>();
        	 for(int i = 0 ; i < fac1Vars.size(); i++){
        		 resVars.add(fac1.getVariable(i));
        	 }
        	 for(int i = 0 ; i < fac2Vars.size(); i++){
        		 //judge if shown on fac1
        		 Variable var = fac2Vars.get(i);
        		 
        		 int j = 0;
        		 for(j = 0 ; j < fac1Vars.size(); j++){
        			 //shown before
        			 if(var.getIndex() == fac1Vars.get(j).getIndex())
        				 break;
        		 }
        		 //not shown before
        		 if(j == fac1Vars.size())
        			 resVars.add(var);
        	 }
        	 
        	 //set table
        	 List<Double> resTable = new ArrayList<Double>();
        	 for(int i = 0 ; i < fac1Table.size(); i++){
        		 //
    			 //compute positions1 of factor 1
    			 //
    			 //e.g. 2(3) 2(4) 3(5)
    			 //e.g. 4 -> 0 1 1
    			 List<Integer> positions1 = new ArrayList<Integer>();
    			 int position = 0;
    			 int sum = fac1Table.size();
    			 int unitSize = 0;
    			 int size = 1;
    			 int relativeI = i;
       			 for(int j = 0 ; j < fac1Vars.size(); j++){
       				 
       				 if(fac1Vars.get(j).getEvidence() == true){
       					 positions1.add(fac1Vars.get(j).getEvidValue());
       				 }
       					 
       				 else{
       				     //to compute new unitSize
       					 size = fac1Vars.get(j).getSize() * size;
       					 //relative j = old relative j - old unit size * old position
       					 relativeI = relativeI - unitSize * position;
       					 //new unitSize
       					 unitSize = sum / size;
       				     //position
       					 position = relativeI / unitSize;
       					 
       					 positions1.add(position);
       				 } 
    			 }
       			 
       			 for(int j = 0 ; j < fac2Table.size(); j++){
       				 //
       				 //compute positions2 of factor 2
       				 //
       				 //e.g. 2(3) 2(4) 3(5)
       				 //e.g. 4 -> 0 1 1
       				 List<Integer> positions2 = new ArrayList<Integer>();
       				 int position2 = 0;
       				 int sum2 = fac2Table.size();
       				 int unitSize2 = 0;
       				 int size2 = 1;
       				 int relativeJ2 = j;
       	   			 for(int k = 0 ; k < fac2Vars.size(); k++){
       	   				 
       	   				 if(fac2Vars.get(k).getEvidence() == true){
       	   					 positions2.add(fac2Vars.get(k).getEvidValue());
       	   				 }
       	   					 
       	   				 else{
       	   				     //to compute new unitSize
       	   					 size2 = fac2Vars.get(k).getSize() * size2;
       	   					 //relative j = old relative j - old unit size * old position
       	   					 relativeJ2 = relativeJ2 - unitSize2 * position2;
       	   					 //new unitSize
       	   					 unitSize2 = sum2 / size2;
       	   				     //position
       	   					 position2 = relativeJ2 / unitSize2;
       	   					 
       	   					 positions2.add(position2);
       	   				 } 
       				 }
       	   			 
       	   			 int k = 0;
       	   			 for( k = 0 ; k < fac1Vars.size(); k++){
       	   				 int index = fac1Vars.get(k).getIndex();
       	   				 int pos = positions1.get(k);
       	   				 
       	   				 int l = 0;
       	   				 for( l = 0 ; l < fac2Vars.size(); l++){
       	   				     int index2 = fac2Vars.get(l).getIndex();
      	   				     int pos2 = positions2.get(l);
      	   				     
      	   				     if(index == index2 && pos != pos2)
      	   				    	 break;
       	   				 }
       	   				 
       	   				 if(l != fac2Vars.size())
       	   					 break;
       	   			 }
       	   			 if(k == fac1Vars.size()){
       	   				 Double newValue = fac1Table.get(i) * fac2Table.get(j);
       	   				 resTable.add(newValue);
       	   			 }
       			 } 
       			 
        	 }
        	 
        	 //return result
        	 Factor resultFactor = new Factor(resVars, resTable);
        	 
             return resultFactor;
    	 }
    	 
    	 //for BN
    	 else{
    		//return result
        	 Factor resultFactor = null ;
        	 
             return resultFactor;
    	 }
    	 
    	 
     }
     
     /*
      * sumOut
      */
     Factor sumOut(Factor inFac, int sumIndex){
    	     	 
    	 List<Variable> vars = inFac.getAllVariables();
    	 List<Double> table = inFac.getAllValuesInTables();
    	 
    	 //get size of variable to be removed
    	 int i = 0;
    	 for( i = 0 ; i < vars.size(); i++){
    		 if(vars.get(i).getIndex() == sumIndex)
    		      break;
    	 }
    	 int sizeOfVarRemoved = vars.get(i).getSize();
    	 int newTableSize = table.size() / sizeOfVarRemoved;
    	 
    	 //initialize new table
    	 List<Double> newTable = new ArrayList<Double>();
    	 for(int j = 0 ; j < newTableSize; j++){
    		 newTable.add(0.0);
    	 }
    	 
    	 for(int j = 0 ; j < table.size(); j++){
    		     //
				 //compute positions of factor
				 //
				 //e.g. 2(3) 2(4) 3(5)
				 //e.g. 4 -> 0 1 1
				 List<Integer> positions = new ArrayList<Integer>();
				 int position = 0;
				 int sum = table.size();
				 int unitSize = 0;
				 int size = 1;
				 int relativeJ = j;
	   			 for(int k = 0 ; k < vars.size(); k++){
	   				 
	   				 if(vars.get(k).getEvidence() == true){
	   					 positions.add(vars.get(k).getEvidValue());
	   				 }
	   					 
	   				 else{
	   				     //to compute new unitSize
	   					 size = vars.get(k).getSize() * size;
	   					 //relative j = old relative j - old unit size * old position
	   					 relativeJ = relativeJ - unitSize * position;
	   					 //new unitSize
	   					 unitSize = sum / size;
	   				     //position
	   					 position = relativeJ / unitSize;
	   					 
	   					 positions.add(position);
	   				 } 
				 }
	   			 
	   			 //compute new index
	   			 //e.g. 2(3) 2(4)(removed) 3(5)
	   			 //e.g. 1 1 0 -> 1 0 -> 5
	   			 int index = 0;
	   			 int oldSize = 1;
	   			 int currentSize = 1;
	   			 for(int k = vars.size() -1; k >= 0 ; k--){
	   				 if(k == i ) continue;
	   				 oldSize *= currentSize;
	   				 
	   				 if(vars.get(k).getEvidence() == true){
	   					 currentSize = 1;
	   				 }
	   				 else
	   				     currentSize = vars.get(k).getSize();
	   				 
	   				 index += positions.get(k) * oldSize;
	   				 
	   			 }
	   			 
	   			 Double newValue = newTable.get(index) + table.get(j);
	   			 newTable.set(index, newValue);
	   			 
    	 }
    	 
    	 vars.remove(i);
    	 
    	 Factor resFac = new Factor(vars, newTable);
    	 
    	 return resFac;
    	 
     }
     
     /*
      * eliminat
      */
     private void eliminate(){
    	 if(this.type == 1){
    		 System.out.println("MN: eliminate...");
    		 
    		 for(int i = 0 ; i < this.order.size(); i++){
        		 int currentIndex = this.order.get(i);
        		 
        		 //eliminate each node
        		 Factor tempFac = new Factor();
        		 //get fist factor which reach requirement
        		 int j = 0;
        		 for( j = 0 ; j < this.factors.size(); j++){
        			 tempFac = this.factors.get(j);
        			 int k = 0;
        			 for( k = 0 ; k < tempFac.getNumOfVariables(); k++){
        				 int index = tempFac.getVariable(k).getIndex();
        				 
        				 if(index == currentIndex) break;
        			 }
        			 
        			 if(k != tempFac.getNumOfVariables()) break;
        		 }
        		 
        		 int firstIndex = j;
        		 
        		 for( j++ ; j < this.factors.size(); j++){
        			 Factor fac = this.factors.get(j);
        			 int k = 0;
        			 for( k = 0 ; k < fac.getNumOfVariables(); k++){
        				 int index = fac.getVariable(k).getIndex();
        				 
        				 if(index == currentIndex) break;
        			 }
        			 // exist the related variable
        			 if(k != fac.getNumOfVariables()){
        				 tempFac = product(tempFac, fac);
        				 this.factors.remove(j--);
        			 }
        		 }
        		 
        		 this.factors.remove(firstIndex);
        		    		 
        		 Factor sumOutFac = new Factor();
        		 //sum-out
        		 sumOutFac = sumOut(tempFac, currentIndex);
        		 this.factors.add(sumOutFac);
        		 
        	 }
    	 }
    	 //for BN
    	 else{
    		 System.out.println("BN: eliminate...");
    		 
    		 for(int i = 0 ; i < this.order.size(); i++){
    			 
    			 
    			 Factor f = this.factors.get(this.order.get(i));
    			 
//    			 if(f.getNumOfVariables() == 1) continue;
    			 
    			 List<Variable> vars = f.getAllVariables();
            	 List<Double> table = f.getAllValuesInTables();
    			 
            	//test
//    			 System.out.println(this.order.get(i));
            	 
    			 //compute each row
    			 for(int j = 0 ; j < table.size(); j++){
       				 //
       				 //compute positions of factor
       				 //
       				 //e.g. 2(3) 2(4) 3(5)
       				 //e.g. 4 -> 0 1 1
       				 List<Integer> positions = new ArrayList<Integer>();
       				 int position = 0;
       				 int sum = table.size();
       				 int unitSize = 0;
       				 int size = 1;
       				 int relativeJ = j;
       	   			 for(int k = 0 ; k < vars.size(); k++){
       	   				 
       	   				 if(vars.get(k).getEvidence() == true){
       	   					 positions.add(vars.get(k).getEvidValue());
       	   				 }
       	   					 
       	   				 else{
       	   				     //to compute new unitSize
       	   					 size = vars.get(k).getSize() * size;
       	   					 //relative j = old relative j - old unit size * old position
       	   					 relativeJ = relativeJ - unitSize * position;
       	   					 //new unitSize
       	   					 unitSize = sum / size;
       	   				     //position
       	   					 position = relativeJ / unitSize;
       	   					 
       	   					 positions.add(position);
       	   				 } 
       				 }
       	   			 //test
//       	   			 System.out.println(positions);
       	   			 
       	   			 //compute new value
       	   			 Double newValue = table.get(j);
       	   			 for(int k = 0 ; k < positions.size() -1; k++){
       	   				 int pos = positions.get(k);
       	   				 int index = f.getVariable(k).getIndex();
       	   				 
       	   				 Double tempValue = new Double(1);
       	   				 //evidence
       	   				 if(this.evidences.get(index) != -1)
       	   					 tempValue = 1.0;
       	   				 else{
       	   					 tempValue = this.factors.get(index).getValueFromTable(pos);
       	   				 }
       	   			     //test
//           	   		     System.out.println(tempValue);
       	   				 newValue *= tempValue;
       	   			 }
       	   			 //test
//       	   		     System.out.println(newValue);
       	   			 
       	   			 table.set(j, newValue);
       			 } 
    			 
    			 /////////////////////////combine values in table
    			 //evidence
    			 if(this.evidences.get(this.order.get(i)) != -1){
    				 Double resultValue = new Double(0);
    				 for(int j = 0 ; j < table.size(); j++){
    					 resultValue += table.get(j);
    					 table.remove(j--);
    				 }
    				 table.add(resultValue);
    				 
    			 }
    			 //non-evidence
    			 else{
    				 int num = f.getVariable(f.getNumOfVariables() -1).getSize();
    				 int oldTableSize = table.size();
    				 for(int j = 0 ; j < num ; j++){
    					 
    					 Double resultValue = new Double(0);
    					 for(int k = 0 ; k < oldTableSize; k++){
    						 //
               				 //compute positions of factor
               				 //
               				 //e.g. 2(3) 2(4) 3(5)
               				 //e.g. 4 -> 0 1 1
               				 List<Integer> positions = new ArrayList<Integer>();
               				 int position = 0;
               				 int sum = oldTableSize;
               				 int unitSize = 0;
               				 int size = 1;
               				 int relativeK = k;
               	   			 for(int l = 0 ; l < vars.size(); l++){
               	   				 
               	   				 if(vars.get(l).getEvidence() == true){
               	   					 positions.add(vars.get(l).getEvidValue());
               	   				 }
               	   					 
               	   				 else{
               	   				     //to compute new unitSize
               	   					 size = vars.get(l).getSize() * size;
               	   					 //relative j = old relative j - old unit size * old position
               	   					 relativeK = relativeK - unitSize * position;
               	   					 //new unitSize
               	   					 unitSize = sum / size;
               	   				     //position
               	   					 position = relativeK / unitSize;
               	   					 
               	   					 positions.add(position);
               	   				 } 
               				 }
    						 
               	   			 //test
//               	   			 if(positions.get(0) == 12 && positions.get(1) == 0 && table.get(k) == 1.0 && j == 47){
//               	   			      System.out.println(positions);
//               	   			      System.out.println(table.get(k));
//               	   			 }
               	   				
               	   			 //sum-out
               	   			 if(positions.get(vars.size() -1) == j){
               	   				 resultValue += table.get(k);
               	   			 }
    					 }
    					 table.add(resultValue);
    				 }

					 //test!!!
//					 System.out.println(table);
    				 //delete useless value in table
    				 for(int j = 0 ; j < oldTableSize ; j++){
    					 table.remove(0);
    				 }
    			 }
    			 //////////////////////////////////////////////combine
    			 //test!!!
//    			 System.out.println(table);
    		 }
    		 
    		 
    	 }
    	 
     }
     
     /*
      * output
      */
     private void output(){
    	 //MN
    	 if(this.type == 1){
    		 System.out.println("-------Result--------");
    		 System.out.println("The probability of evidence is: " + this.factors.get(0).getValueFromTable(0));
    	 }
    	 //BN
    	 else{
    		 System.out.println("-------Result--------");
    		 
    		 Double result = new Double(1);
    		 for(int i = 0 ; i < this.factors.size(); i++){
    			 //evidence
    			 if(this.evidences.get(i) !=-1 ){
    				 result *= this.factors.get(i).getValueFromTable(0);
    			 }
    		 }
    		 
    		 System.out.println("The probability of evidence is: " + result);

    	 }
    	 
     }
}
