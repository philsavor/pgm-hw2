package pgm;

import java.util.ArrayList;
import java.util.List;

public class Factor {
     private List<Variable> variables = new ArrayList<Variable>();
     private List<Double> table = new ArrayList<Double>(); 
     
     public Factor(List<Variable> vars , List<Double> tab){
    	 this.variables = vars;
    	 this.table = tab;
     }
     
     public Factor(){
    	 this.variables = new ArrayList<Variable>();
    	 this.table = new ArrayList<Double>();
     }
     
     //add a variable
     public void addVariable(Variable v){
    	 this.variables.add(v);
     }
     
     //get variable
     public Variable getVariable(int index){
    	 return this.variables.get(index);
     }
     
     //get all variables
     public List<Variable> getAllVariables(){
    	 return this.variables;
     }
     
     //get # variables
     public int getNumOfVariables(){
    	 return this.variables.size();
     }
     
     //add value to table
     public void addValueToTable(Double value){
    	 this.table.add(value);
     }
     
     //get # values in table
     public int getNumOfValuesInTable(){
    	 return this.table.size();
     }
     
     //get value from table
     public Double getValueFromTable(int index){
    	 return this.table.get(index);
     }
     
     //remove value in table : instantiate
     public void removeValueInTable(int index){
    	 this.table.remove(index);
     }
     
     //get all values in table
     public List<Double> getAllValuesInTables(){
    	 return this.table;
     }
     
     //for test
     public  void showTableValues(){
    	 System.out.println(this.table);
     }
}
