package com.msa.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public class QueryManager{

	private QueryBuilder queryBuilder;
	private QueryProcessor queryProcessor;

	
	public QueryManager(){
		this.queryBuilder = new QueryBuilder();
		this.queryProcessor = new QueryProcessor();
	}
	

	
	public String[] getDatasetsNames() {
		Map<Integer, List<String>> map = this.queryProcessor.processQuery(this.queryBuilder.getDatasetsNamesQuery());
		
		Set<String> withoutRepetition = new HashSet<String>();
		for(List<String> row : map.values()){
			withoutRepetition.add(row.get(0));
		}
		
		withoutRepetition.remove("fileName");
		String[] toReturn = new String[withoutRepetition.size()];
		int i=0;
		for(String s : withoutRepetition){
			toReturn[i] = s;
			i++;
		}
		
		return toReturn;
	}



	public Map<Integer, List<String>> executeQuery(	JComboBox<String> dataset_choice_menu,
								JComboBox<String> execution_modes_menu,
								JComboBox<String> queries_menu,
								JCheckBox order_by_checkBox,
								JComboBox<String> order_by_menu,
								JComboBox<String> order_mode_menu) {

			return this.queryProcessor.processQuery(this.queryBuilder.buildQuery(queries_menu, order_by_checkBox, order_by_menu, order_mode_menu),
																		  dataset_choice_menu, execution_modes_menu);

		
	}
	
	
	public String[] getResultsColumnsFromQuery(String query){
		return this.queryBuilder.getResultsColumnsFromQuery(query);
	}
	
	
	public String[] getAvailableQueries(){
		return this.queryBuilder.getAvailableQueries();
	}


	public String[] getResultsColumnsFromQueryName(String queryName) {
		return this.queryBuilder.getResultsColumnsFromQueryName(queryName);
	}
	
	
}
