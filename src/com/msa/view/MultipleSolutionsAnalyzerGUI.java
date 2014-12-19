package com.msa.view;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import com.msa.controller.QueryManager;

public class MultipleSolutionsAnalyzerGUI extends JPanel{

	///////////////////////////////////////////////////////////


	/**
	 * default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/* variables for GUI management */
	private JComboBox<String> dataset_choice_menu;
	private JComboBox<String> execution_modes_menu;
	private JComboBox<String> queries_menu;
	private JComboBox<String> order_by_menu;
	private JComboBox<String> order_mode_menu;

	/* STATISTICS */
	private JComboBox<String> statistics_fields;
	private JComboBox<String> statistics_operators;
	private JTextField statistics_textField_value;

	private JCheckBox statistics_checkbox;

	private boolean statistics_choosed = false;
	/**************/


	private JCheckBox order_by_checkbox;

	private JButton startButton;


	/*
	 * USER INTERFACE
	 */
	private JPanel labelPanel1 = new JPanel();
	private JPanel panel1 = new JPanel();
	private JPanel labelPanel2= new JPanel();
	private JPanel panel2 = new JPanel();
	private JPanel panel3 = new JPanel();
	private JPanel panelStatistics = new JPanel();

	/*
	 * ResultsViewer
	 */
	private JScrollPane scrollPane = new JScrollPane();
	private JPanel displayResultsPanel = new JPanel();



	private JTextArea log;
	//private JFileChooser fcOut;
	private boolean threadStarted = false;
	private QueryManager queryManager;

	private String[] datasets;
	private String[] execution_modes;
	private String[] queries;
	private String[] order_by;
	private String[] order_mode;


	public boolean isThreadStarted(){
		return this.threadStarted;
	}

	public void setThreadStarted(boolean threadS){
		this.threadStarted = threadS;
	}


	public MultipleSolutionsAnalyzerGUI() {
		super.setLayout(new BoxLayout(this, 1));

		this.setPreferredSize(new Dimension(800, 600));

		this.queryManager = new QueryManager();

		//Create the log first, because the action listeners
		//need to refer to it.
		log = new JTextArea(30,40);
		log.setMargin(new Insets(2,2,2,2));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);
		DefaultCaret caret = (DefaultCaret) log.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);



		datasets = queryManager.getDatasetsNames();
		this.dataset_choice_menu = new JComboBox<String>(datasets);

		execution_modes = new String[]{"loose", "strict"};
		this.execution_modes_menu = new JComboBox<String>(execution_modes);

		queries = this.queryManager.getAvailableQueries();
		this.queries_menu = new JComboBox<String>(queries);

		//order by checkbox
		order_by_checkbox = new JCheckBox("", true);		

		//Dynamic, based on queries choice
		order_by = this.queryManager.getResultsColumnsFromQueryName( (String) this.queries_menu.getSelectedItem() );
		this.order_by_menu = new JComboBox<String>(order_by);

		order_mode = new String[]{"ASC", "DESC"};
		this.order_mode_menu = new JComboBox<String>(order_mode);

		this.startButton = new JButton("START"); 


		/* STATISTICS PANEL MANAGMENT */

		statistics_fields = new JComboBox<String>(new String[]{"counter", "standard_deviation", "relevance"});
		statistics_operators = new JComboBox<String>(new String[]{">", "<", ">=", "<=", "="});
		statistics_textField_value = new JTextField(5);
		/******************************/



		labelPanel1.add(new JLabel("                        Dataset    Execution Mode  ORDER BY?"));
		panel1.add(dataset_choice_menu);
		panel1.add(execution_modes_menu);
		panel1.add(order_by_checkbox);

		labelPanel2.add(new JLabel("Queries                 Order By         Order Mode"));

		panel2.add(queries_menu);
		panel2.add(order_by_menu);
		panel2.add(order_mode_menu);

		panel3.add(startButton);

		/*
		 * Filter Panel, showed only in case of statistics!
		 * */
		statistics_checkbox = new JCheckBox("<- Filter (shows only records that respect the given rule)");
		statistics_checkbox.setSelected(false);
		statistics_fields.setEnabled(false);
		statistics_operators.setEnabled(false);
		statistics_textField_value.setEnabled(false);
		
		panelStatistics.add(statistics_fields);
		panelStatistics.add(statistics_operators);
		panelStatistics.add(statistics_textField_value);
		panelStatistics.add(statistics_checkbox);
		panelStatistics.setVisible(false);


		add(labelPanel1);
		add(panel1);
		add(labelPanel2);
		add(panel2);
		add(panelStatistics);
		add(panel3);
		add(logScrollPane);


		order_by_checkbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(order_by_checkbox.isSelected()){
					order_by_menu.setEnabled(true);
					order_mode_menu.setEnabled(true);
				}else{
					order_by_menu.setEnabled(false);
					order_mode_menu.setEnabled(false);
				}

			}
		});


		queries_menu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeItemsComboBox(order_by_menu, queryManager.getResultsColumnsFromQueryName( (String) queries_menu.getSelectedItem() ));

				if(((String) queries_menu.getSelectedItem()).equals("literals_statistics")){
					panelStatistics.setVisible(true);
				}else{
					panelStatistics.setVisible(false);
				}
			}

		});

		statistics_checkbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!statistics_checkbox.isSelected()){
					statistics_fields.setEnabled(false);
					statistics_operators.setEnabled(false);
					statistics_textField_value.setEnabled(false);
				}else{
					statistics_fields.setEnabled(true);
					statistics_operators.setEnabled(true);
					statistics_textField_value.setEnabled(true);

				}

			}
		});



		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Handle start button action.
				if(isThreadStarted())
					log.append("Already started!\n");
				else{
					log.append("\n\n\n");
					log.append("Starting process...\n");
					setThreadStarted(true);

					Map<Integer, List<String>> results = queryManager.executeQuery(dataset_choice_menu, execution_modes_menu,
							queries_menu, order_by_checkbox,
							order_by_menu, order_mode_menu);

					if(statistics_checkbox.isSelected()){
						results = filterOnResults(results);
					}


					log.append("Ending process...\n");

					printResults(results);
					setThreadStarted(false);



				}
			}



		});


	}



	private void changeItemsComboBox(JComboBox<String> combo_box,
			String[] items) {

		combo_box.removeAllItems();

		for(String s : items)
			combo_box.addItem(s);		

	}


	private Map<Integer, List<String>> filterOnResults(Map<Integer, List<String>> results) {

		Map<Integer, List<String>> newResults = new HashMap<Integer, List<String>>();

		/* Searching the index of the column to filter */
		int columnToFilter = 0;
		for(String s : results.get(0)){
			if(s.equals((String) this.statistics_fields.getSelectedItem())){
				break;
			}else{
				columnToFilter++;
			}
		}
		newResults.put(0, results.get(0));

		int index = 1;

		try{
			String valueLimitString = (String) this.statistics_textField_value.getText();
			valueLimitString = valueLimitString.replace(",", ".");
			double valueLimit = Double.parseDouble(valueLimitString);

			for(int i=1; i<results.keySet().size(); i++){
				String toConvert = results.get(i).get(columnToFilter);
				double valueToVerify = 0;

				if(toConvert != null){
					valueToVerify = Double.parseDouble(toConvert);
				}

				boolean toDelete = false;


				switch((String) this.statistics_operators.getSelectedItem()){
				case ">" :
					if(valueToVerify <= valueLimit){
						toDelete = true;
					}
					break;
				case "<" :
					if(valueToVerify >= valueLimit){
						toDelete = true;
					}
					break;
				case ">=" :
					if(valueToVerify < valueLimit){
						toDelete = true;
					}
					break;
				case "<=" :
					if(valueToVerify > valueLimit){
						toDelete = true;
					}
					break;
				case "=" :
					if(valueToVerify != valueLimit){
						toDelete = true;
					}
					break;
				}

				if(!toDelete){
					newResults.put(index, results.get(i));
					index++;
				}


			}


		}catch(Exception e){
			log.append("ERROR - The value must be a Real number\n");
		}



		return newResults;
	}



	private void printResults(Map<Integer, List<String>> results) {

		List<String> firstRow = results.get(0);
		int numberOfRows = results.keySet().size();

		String[] header = new String[firstRow.size()+1];
		Object[][] content = new Object[numberOfRows-1][firstRow.size()+1];

		/* Build the Header */
		header[0] = "#";
		for (int i=0; i<firstRow.size(); i++){
			header[i+1] = firstRow.get(i);
		}

		/* Build the content */
		//////////////////////
		/* Fill the first column */
		for(int i=0; i<numberOfRows-1; i++){
			content[i][0] = i+1;
		}
		/* Fill the rest of the matrix */
		for(int i=1; i<numberOfRows; i++){
			for(int j=1; j<firstRow.size()+1; j++){
				content[i-1][j] = results.get(i).get(j-1);
			}
		}

		JTable table = new JTable(content, header);
		JFrame resultsFrame = new JFrame();
		resultsFrame.setResizable(true);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		//		JPanel saveResultsPanel = new JPanel();
		//		JButton saveResultsButton = new JButton("Save CSV");
		//		saveResultsPanel.add(saveResultsButton);
		//	    add(table);
		//		resultsFrame.setVisible(true);

		scrollPane.setViewportView(table);
		this.displayResultsPanel.add(scrollPane);
		this.displayResultsPanel.revalidate();

		this.log.append("Number Of ROWS:\t\t"+(numberOfRows-1)+"\n");
		this.log.append("Number Of COLUMNS:\t\t"+firstRow.size()+"\n");

		Object[] options = {"Save as CSV", "Cancel"};

		/* GOOD!!! */
		//JOptionPane.showMessageDialog(null, displayResultsPanel);

		int answer = JOptionPane.showOptionDialog(resultsFrame, displayResultsPanel, "Results", JOptionPane.YES_NO_OPTION, JOptionPane.NO_OPTION, null, options, options[1]); //resultsFrame, displayResultsPanel);

		if(answer == JOptionPane.YES_OPTION){
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setVisible(true);

			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);			
			int returnVal = fileChooser.showOpenDialog(MultipleSolutionsAnalyzerGUI.this);

			if(returnVal == JFileChooser.APPROVE_OPTION){
				String pathToFile = fileChooser.getSelectedFile().getAbsolutePath();
				String name = JOptionPane.showInputDialog("Choose a name for your file");
				if (name.equals("")){
					name = "results";
				}

				if(pathToFile.endsWith("\\") || pathToFile.endsWith("/")){
					pathToFile+=name+".csv";
				}else{
					pathToFile+=returnSlash()+name+".csv";
				}

				saveAsCSV(results, pathToFile);

			}


		}


	}



	private void saveAsCSV(Map<Integer, List<String>> results,
			String pathToFile) {

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(pathToFile));
			for(int i=0; i<results.keySet().size(); i++){
				String row = "";
				List<String> tmpList = results.get(i);

				for(int j=0; j<tmpList.size()-1; j++){
					row += tmpList.get(j)+",";
				}

				row += tmpList.get(tmpList.size()-1)+"\n";
				writer.write(row);					

			}

			writer.flush();
			writer.close();

		} catch (IOException e) {

			JOptionPane.showMessageDialog(null, "An error occurred during while saving file, please retry", "ERROR", JOptionPane.ERROR_MESSAGE);


			e.printStackTrace();
		}


	}


	public String returnSlash() {

		String OS = System.getProperty("os.name").toLowerCase();

		if (OS.indexOf("win") >= 0)
			return "\\";
		else
			return "/";
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = MultipleSolutionsAnalyzerGUI.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("Multiple Solutions Analyzer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Add content to the window.
		frame.add(new MultipleSolutionsAnalyzerGUI());

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}




	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE); 
				createAndShowGUI();
			}
		});

	}





}
