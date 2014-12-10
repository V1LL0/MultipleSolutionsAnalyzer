package com.msa.controller;

import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

import com.msa.persistence.Dao;

public class QueryProcessor {
	
	private Dao dao;
	
	public QueryProcessor(){
		this.dao = new Dao();
	}

	public Map<Integer, List<String>> processQuery(String query, JComboBox<String> dataset_choice_menu, JComboBox<String> execution_modes_menu) {
		return this.dao.retrieve(query, dataset_choice_menu, execution_modes_menu);
	}

	public Map<Integer, List<String>> processQuery(String query) {
		return this.dao.retrieve(query, null, null);
	}
	

}
