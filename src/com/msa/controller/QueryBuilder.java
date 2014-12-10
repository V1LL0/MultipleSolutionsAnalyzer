package com.msa.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public class QueryBuilder {


	private Map<String, String> queries = new HashMap<String, String>();

	/**
	 * The query builder contains a MAP<String, String>.
	 * This Map contains as first String, the name of the query and, as the second String, the query itself.
	 * The query must have only one question mark, to indicate the idRun that will be calculated.
	 * Also, the query must have in the first line (before the first \n) only the row containing the SELECT
	 * and the names of the columns that will be presented in output.
	 * Finally, the query must not finish with ";" but with a "\n"
	 * 
	 */
	public QueryBuilder(){
		this.queries.put("list_of_Attributes", "SELECT Attribute.name, counterFoundTimes\n" +
			"from Attribute, FoundAttributesSet, FoundAttributesSet_Attribute, Run\n" +
			"WHERE Attribute.idAttribute IN (\n" + 
			"	SELECT Literal.idAttribute FROM Literal WHERE idLiteralsSet IN (\n" + 
			"		SELECT LiteralsSet.idLiteralsSet FROM LiteralsSet WHERE idRule IN(\n" + 
			"			SELECT Rule.idRule FROM Rule, Class WHERE Rule.idClass = Class.idClass\n" + 
			"		)\n" + 
			"	)\n" + 
			")\n" + 
			"AND Attribute.idAttribute = FoundAttributesSet_Attribute.idAttribute\n" + 
			"AND FoundAttributesSet.idFoundAttributesSet = FoundAttributesSet_Attribute.idFoundAttributesSet\n" +
			"AND Run.idRun = FoundAttributesSet.idRun\n" +
			"AND Run.idRun = ?\n" +
			"GROUP BY Attribute.idAttribute");
		
		this.queries.put("list_of_conjunctions", "SELECT ruleFragment, correctlyClassifiedInstances*100/(correctlyClassifiedInstances+incorrectlyClassifiedInstances) as correctlyClassifiedPercentage, " + 
				"incorrectlyClassifiedInstances*100/(correctlyClassifiedInstances+incorrectlyClassifiedInstances) as incorrectlyClassifiedPercentage\n" + 
				"FROM LiteralsSet, Rule, Experiment\n" +
				"WHERE LiteralsSet.idRule = Rule.idRule\n" +
				"AND Rule.idExperiment = Experiment.idExperiment\n" +
				"AND Experiment.idRun = ?\n");
		
		this.queries.put("list_of_disjunctions", "SELECT rule, ExperimentResultsPerClass.fMeasureValue, " + 
				"correctlyClassifiedInstances/(correctlyClassifiedInstances+incorrectlyClassifiedInstances) AS accuracy \n" + 
				"FROM Rule, Class, ExperimentResults, ExperimentResultsPerClass, Experiment\n" + 
				"WHERE Rule.idClass = Class.idClass\n" +
				"AND ExperimentResults.idExperiment = Rule.idExperiment\n" + 
				"AND ExperimentResultsPerClass.idExperiment = Rule.idExperiment\n" + 
				"AND ExperimentResultsPerClass.idClass = Rule.idClass\n" + 
				"AND ExperimentResults.idExperiment = Experiment.idExperiment\n" + 
				"AND Experiment.idRun = ? \n");
		
		this.queries.put("couples_of_attributes_found", "SELECT AttributeCopy1.name, AttributeCopy2.name, FoundAttributesSet.counterFoundTimes\n" + 
				"FROM Attribute AS AttributeCopy1, Attribute AS AttributeCopy2,\n" + 
				"	 FoundAttributesSet_Attribute AS FASA1,\n" + 
				"	 FoundAttributesSet_Attribute AS FASA2,\n" + 
				"	 FoundAttributesSet\n" + 
				"WHERE FASA1.idFoundAttributesSet = FASA2.idFoundAttributesSet\n" + 
				"AND FASA1.idFoundAttributesSet = FoundAttributesSet.idFoundAttributesSet\n" + 
				"AND FASA1.idAttribute > FASA2.idAttribute\n" + 
				"AND AttributeCopy1.idAttribute = FASA1.idAttribute\n" + 
				"AND AttributeCopy2.idAttribute = FASA2.idAttribute\n" + 
				"AND FoundAttributesSet.idRun = ?\n" + 
				"AND FASA1.idFoundAttributesSet IN (\n" + 
				"	SELECT DISTINCT idFoundAttributesSet\n" + 
				"	FROM FoundAttributesSet_Attribute\n" + 
				"	GROUP BY idFoundAttributesSet\n" + 
				"	HAVING count(*)=2\n" + 
				")\n");
		
		this.queries.put("literals_statistics", "SELECT Attribute.name, Literal.operator, MIN(value), MAX(value), AVG(value), count(*) as counter, VARIANCE(value) as standard_deviation, ( AVG(value) - VARIANCE(value) )/AVG(value) as relevance\n" + 
												"FROM Literal, Attribute,\n" + 
												"Experiment, Rule, LiteralsSet\n" + 
												"WHERE Literal.idAttribute = Attribute.idAttribute\n" + 
												"AND Literal.idLiteralsSet = LiteralsSet.idLiteralsSet\n" + 
												"AND LiteralsSet.idRule = Rule.idRule\n" + 
												"AND Rule.idExperiment = Experiment.idExperiment\n" + 
												"AND Experiment.idRun = ?\n" + 
												"GROUP BY Attribute.name, Literal.operator");

	}	


	public String getDatasetsNamesQuery() {
		return "SELECT fileName FROM Run;";
	}

	public String buildQuery(JComboBox<String> queries_menu, JCheckBox order_by_checkBox,
			JComboBox<String> order_by_menu, JComboBox<String> order_mode_menu) {

		// GET THE RIGHT QUERY FROM THE QUERIES MAP
		String queryToExecute = this.queries.get( (String) queries_menu.getSelectedItem() );

		String order_by_string = "";
		if(order_by_checkBox.isSelected()){
			order_by_string += "\nORDER BY ";
			order_by_string += (String) order_by_menu.getSelectedItem() + " ";
			order_by_string += (String) order_mode_menu.getSelectedItem()+";";			
		}else{
			order_by_string += ";";
		}

		queryToExecute += order_by_string;

		return queryToExecute;
	}


	public String[] getResultsColumnsFromQuery(String query){
		String[] toReturn = null;

		String firstRow = query.split("\n")[0];
		String[] tempColumns = firstRow.split("\\s*,\\s*");

		tempColumns[0] = tempColumns[0].replace("SELECT ", "");

		toReturn = new String[tempColumns.length];
		for(int i=0; i<tempColumns.length; i++){
			if(tempColumns[i].contains(" as "))
				toReturn[i] = tempColumns[i].split(" as ")[1];
			else if(tempColumns[i].contains(" AS "))
				toReturn[i] = tempColumns[i].split(" AS ")[1];
			else
				toReturn[i] = tempColumns[i];
		}

		return toReturn;
	}

	public String[] getResultsColumnsFromQueryName(String queryName){
		return getResultsColumnsFromQuery(this.queries.get(queryName));
	}
	

	public String[] getAvailableQueries() {
		Set<String> queriesSet = this.queries.keySet();
		String[] toReturn = new String[queriesSet.size()];
		
		int i=0;
		for(Object s : queriesSet.toArray()){
			toReturn[i] = (String) s;
			i++;
		}
		
		return toReturn;
		
		
	}





}
