package com.msa.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

public class Dao {

	private DriverLoader_Connector connector;
	private Connection conn;


	public Dao(){
		try{
			this.connector = new DriverLoader_Connector();
			this.conn = connector.getConnection();
		}catch(Exception e){
			e.printStackTrace();
		}

	}


	public int findIdClass(String className){

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			String find = "select * from Class where name=?";
			stmt = conn.prepareStatement(find);
			stmt.setString(1, className);
			rs = stmt.executeQuery();

			if(rs.next())
				return rs.getInt(1);
			else
				return -1;

		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

			return -1;
		}
		finally {
			finalizeOperation(stmt, rs);
		}


	}
	
	
	public int findIdRun(String fileName, String executionMode){
		
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			String find = "select * from Run where fileName=? AND executionMode=?";
			stmt = conn.prepareStatement(find);
			stmt.setString(1, fileName);
			stmt.setString(2, executionMode);
			rs = stmt.executeQuery();

			if(rs.next())
				return rs.getInt(1);
			else
				return -1;

		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

			return -1;
		}
		finally {
			finalizeOperation(stmt, rs);
		}

		
	}
	
	


	public int findIdAttribute(String attribute) {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			String find = "select * from Attribute where name=?";
			stmt = conn.prepareStatement(find);
			stmt.setString(1, attribute);
			rs = stmt.executeQuery();

			if(rs.next())
				return rs.getInt(1);
			else
				return -1;

		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

			return -1;
		}
		finally {
			finalizeOperation(stmt, rs);
		}

	}

	public int findIdFoundAttributesSetInRun(String[] window, int idRun){
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<Integer, List<String>> tmpMapOfLists = new HashMap<Integer, List<String>>();
		int idFoundAttributeSet_tmp = -1;
		List<String> tmpList = new LinkedList<String>();

		try {

			String findMap = "select FoundAttributesSet_Attribute.idFoundAttributesSet, Attribute.name from FoundAttributesSet_Attribute, Attribute, FoundAttributesSet where FoundAttributesSet_Attribute.idAttribute = Attribute.idAttribute and FoundAttributesSet_Attribute.idFoundAttributesSet = FoundAttributesSet.idFoundAttributesSet and FoundAttributesSet.idRun = ? group by FoundAttributesSet_Attribute.idFoundAttributesSet, Attribute.name;";
			stmt = conn.prepareStatement(findMap);
			stmt.setInt(1, idRun);

			rs = stmt.executeQuery();

			while(rs.next()){

				if(idFoundAttributeSet_tmp == -1){
					idFoundAttributeSet_tmp=rs.getInt(1);
				}

				int idFoundAttributeSet = rs.getInt(1);
				String attributeName = rs.getString(2);

				if(idFoundAttributeSet == idFoundAttributeSet_tmp)
					tmpList.add(attributeName);
				else{
					tmpMapOfLists.put(idFoundAttributeSet_tmp, tmpList);
					idFoundAttributeSet_tmp = idFoundAttributeSet;
					tmpList = new LinkedList<String>();
					tmpList.add(attributeName);
				}
			}
			tmpMapOfLists.put(idFoundAttributeSet_tmp, tmpList);

			for(int idFASet : tmpMapOfLists.keySet()){
				List<String> list = tmpMapOfLists.get(idFASet);
				if(list.size() == window.length){
					String[] array = new String[list.size()];
					int i=0;
					for(String s : list){
						array[i] = s;
						i++;
					}
					Arrays.sort(array);
					Arrays.sort(window);

					if(areStringArraysEquals(array, window))
						return idFASet;

				}
			}

			return -1;
		}catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}
		finally {
			finalizeOperation(stmt, rs);
		}

		return -1;
	}




	private boolean areStringArraysEquals(String[] array, String[] array2) {
		if(array.length != array2.length)
			return false;

		for(int z=0; z<array.length; z++){
			if(!array[z].equals(array2[z]))
				return false;
		}
		return true;
	}


	public Map<Integer, List<String>> retrieve(String query,
			JComboBox<String> dataset_choice_menu,
			JComboBox<String> execution_modes_menu) {
		
		
		PreparedStatement stmt = null;
		ResultSet rs = null;

		Map<Integer, List<String>> results = new HashMap<Integer, List<String>>();


		try {

			stmt = conn.prepareStatement(query);
			
			if(dataset_choice_menu != null && execution_modes_menu != null){
				stmt.setInt(1, this.findIdRun((String) dataset_choice_menu.getSelectedItem(), (String) execution_modes_menu.getSelectedItem()));
			}
			
			rs = stmt.executeQuery();
			
			
			List<String> columns = new LinkedList<String>();
			
			for(int i=1; i<=rs.getMetaData().getColumnCount(); i++){
				columns.add(rs.getMetaData().getColumnName(i));
			}
			
			results.put(0, columns);
			
						
			
			int rowsCount = 0;
/*			 //PRINT RS CONTENT			
			int columns = rs.getMetaData().getColumnCount();

			StringBuilder message = new StringBuilder();

			while (rs.next()) {
			    for (int i = 1; i <= columns; i++) {
			        message.append(rs.getString(i) + " ");
			    }
			    message.append("\n");
			}

			System.out.println(message); 
*/
			while(rs.next()){
				rowsCount++;
				List<String> tempList = new LinkedList<String>();

				for(int i=1; i<=rs.getMetaData().getColumnCount(); i++){
					tempList.add(rs.getString(i));
				}
				results.put(rowsCount, tempList);
			}
			

			
			return results;

		}catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}
		finally {
			finalizeOperation(stmt, rs);
		}

		return null;
	}

	
	
	
	
	
	
	
	
	
	
	
	

	private void finalizeOperation(Statement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException sqlEx) { } // ignore

			rs = null;
		}

		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) { } // ignore
			stmt = null;
		}

	}



	public void closeConnection(){

		if (this.conn != null) {
			try {
				this.conn.close();
			} catch (SQLException sqlEx) { } // ignore
			this.conn = null;
		}

	}








}