package pgm;

public class Variable {
     private int size;  //domain size
     private boolean isEvidence;
     private int evidValue;
     private int index;  // the index of variable
     
     public Variable(){
    	 this.isEvidence = false;
     }
     
     //evidence value
     public void setEvidValue(int value){
    	 this.evidValue = value;
     }
     
     public int getEvidValue(){
    	 return this.evidValue;
     }
     
     //size
     public void setSize(int size){
    	 this.size = size;
     }
     
     public int getSize(){
    	 return this.size;
     }
     
     //isEvidence
     public void setEvidence(){
    	 this.isEvidence = true;
     }
     
     public boolean getEvidence(){
    	 return this.isEvidence;
     }
     
     //index
     public void setIndex(int index){
    	 this.index = index;
     }
     
     public int getIndex(){
    	 return this.index;
     }
}
