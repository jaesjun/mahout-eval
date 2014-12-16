package org.mahouteval.gui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.mahout.common.distance.ChebyshevDistanceMeasure;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.distance.MahalanobisDistanceMeasure;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.common.distance.MinkowskiDistanceMeasure;
import org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure;
import org.apache.mahout.common.distance.TanimotoDistanceMeasure;
import org.apache.mahout.common.distance.WeightedDistanceMeasure;
import org.apache.mahout.common.distance.WeightedEuclideanDistanceMeasure;
import org.apache.mahout.common.distance.WeightedManhattanDistanceMeasure;
import org.mahouteval.clustering.NewsKMeansClustering;
import org.mahouteval.gui.table.ClusterSummaryTable;
import org.mahouteval.gui.table.ClusterSummaryTable.CLUSTER_SUMMAY;
import org.mahouteval.gui.table.DocumentTable;
import org.mahouteval.model.Document;
import org.mahouteval.model.TFIDFTermVector;

public class DocumentClusteringContainer extends JPanel implements ActionListener {
	private JTextField minDfField = new JTextField();
	private JTextField maxDfPercentField = new JTextField();
	private JTextField maxNGramSizeField = new JTextField();
	private JTextField maxIterationField = new JTextField();
	private JTextField convergenceDeltaField = new JTextField();
	private JTextField fuzinessField = new JTextField();

	private Object[] distanceMeasureOption = {"ChebyshevDistanceMeasure", "CosineDistanceMeasure", 
			"EuclideanDistanceMeasure", "MahalanobisDistanceMeasure", "ManhattanDistanceMeasure", 
			"MinkowskiDistanceMeasure", "SquaredEuclideanDistanceMeasure", "TanimotoDistanceMeasure", 
			"WeightedDistanceMeasure", "WeightedEuclideanDistanceMeasure", "WeightedManhattanDistanceMeasure"
		};
	private JComboBox distanceMeasureOptionBox = new JComboBox(distanceMeasureOption);

	private Object[] clusterSizeChoose = {"5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60"};
	private JComboBox clusterSizeChooseBox = new JComboBox(clusterSizeChoose);

	private Object[] clusteringChoose = {"KMeans", "Fuzzy KMeans"};
	private JComboBox clusteringChooseBox = new JComboBox(clusteringChoose);

	private JButton clusteringBtn = new JButton("Clustering");

	private JPanel contentPanel = new JPanel();
	private JPanel clusterPanel = new JPanel();
	private JPanel vectorDescPanel = new JPanel();
	
	private DocumentTable documentTable = new DocumentTable();
	private ClusterSummaryTable clusterTable = new ClusterSummaryTable(CLUSTER_SUMMAY.CLUSTER_SIZE);
	private ClusterSummaryTable termTable = new ClusterSummaryTable(CLUSTER_SUMMAY.TERM_WEIGHT);
	private List<String> clusterTableHeader;
	private List<String> termTableHeader;
	
	private Map<String, List<TFIDFTermVector>> docTerms;
	private Map<String, List<String>> clusterIdMap;
	
	private MahoutDemoUI demoUI;
	
	public DocumentClusteringContainer(MahoutDemoUI demoUI) {
		this.demoUI = demoUI;
		
		try {
			initUIs(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initUIs() throws URISyntaxException, IOException {
		setLayout(new BorderLayout());
		initClusterButton();
		initContents();
		initVectorSummary();
		
		add(clusterPanel, BorderLayout.NORTH);
		add(contentPanel, BorderLayout.CENTER);
		add(vectorDescPanel, BorderLayout.WEST);
	}

	private void initVectorSummary() {
		String[] header = {"Cluster ID", "Size"};
		clusterTableHeader = Arrays.asList(header);
		List<String[]> empty = new ArrayList<String[]>();
		clusterTable.displayCluster(clusterTableHeader, empty);
		
		vectorDescPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
		vectorDescPanel.setLayout(new BorderLayout());
		
		JScrollPane clusterTableScroll = new JScrollPane(clusterTable);
		clusterTableScroll.setPreferredSize(new Dimension(200, 300));
		vectorDescPanel.add(clusterTableScroll, BorderLayout.NORTH);

		String[] termHeader = {"Term", "Found", "TF*IDF Weight"};
		termTableHeader = Arrays.asList(termHeader);
		termTable.displayCluster(termTableHeader, empty);
		
		JScrollPane termTableScroll = new JScrollPane(termTable);
		vectorDescPanel.add(termTableScroll, BorderLayout.CENTER);

		vectorDescPanel.setPreferredSize(new Dimension(300, 100));
		
		clusterTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		    	if(e.getValueIsAdjusting()) {
		    		return;
		    	}
		    	
	        	new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							loadTermVector();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							demoUI.hideWaitDialog();
						}
					}
	        		
	        	}).start();
	        	demoUI.showWaitDialog("Loading Term", "  Please wait until term loading finish...");
		    }
		});
	}

	private void initContents() throws URISyntaxException, IOException {
		contentPanel.setLayout(new BorderLayout());
		JScrollPane documentTableScroll = new JScrollPane(documentTable);
		contentPanel.add(documentTableScroll, BorderLayout.CENTER);
	}

	private void initClusterButton() {
		clusterPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		clusterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		clusterPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		clusterPanel.add(new JLabel( "Distance Measure"));
		distanceMeasureOptionBox.setSelectedIndex(2);
		clusterPanel.add(distanceMeasureOptionBox);

		clusterPanel.add(new JLabel(" Min Doc Freq"));
		minDfField.setText("5");
		minDfField.setPreferredSize(new Dimension(40, 20));
		clusterPanel.add(minDfField);

		clusterPanel.add(new JLabel(" Max Doc Freq(%)"));
		maxDfPercentField.setText("95");
		maxDfPercentField.setPreferredSize(new Dimension(40, 20));
		clusterPanel.add(maxDfPercentField);

		clusterPanel.add(new JLabel( " NGram"));
		maxNGramSizeField.setPreferredSize(new Dimension(30, 20));
		maxNGramSizeField.setText("1");
		clusterPanel.add(maxNGramSizeField);

		clusterPanel.add(new JLabel(" Cluster Size"));
		clusterSizeChooseBox.setSelectedIndex(3);
		clusterPanel.add(clusterSizeChooseBox);

		clusterPanel.add(new JLabel(" Max Iteration"));
		maxIterationField.setText("20");
		clusterPanel.add(maxIterationField);

		clusterPanel.add(new JLabel(" Convergence Delta"));
		convergenceDeltaField.setPreferredSize(new Dimension(50, 20));
		convergenceDeltaField.setText("0.001");
		clusterPanel.add(convergenceDeltaField);

		clusterPanel.add(new JLabel(" Fuziness"));
		fuzinessField.setPreferredSize(new Dimension(40, 20));
		fuzinessField.setText("2.0");
		fuzinessField.setEnabled(false);
		clusterPanel.add(fuzinessField);

		clusteringChooseBox.addActionListener(this);
		clusterPanel.add(new JLabel(" Method"));
		clusterPanel.add(clusteringChooseBox);

		clusteringBtn.addActionListener(this);
		clusterPanel.add(clusteringBtn);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == clusteringBtn) {
			if (clusteringChooseBox.getSelectedIndex() == 0) {
				clusteringByKMeans();
			} else {
				clusteringByFuzzyKMeans();
			}
		} else if (event.getSource() == clusteringChooseBox) {
			convergenceDeltaField.setEnabled(clusteringChooseBox.getSelectedIndex() == 1);
			fuzinessField.setEnabled(clusteringChooseBox.getSelectedIndex() == 1);
		}
	}

	private void clusteringByKMeans() {
		if (validatePositiveIntegerField() && validatePositiveFloatField(convergenceDeltaField)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Class<? extends DistanceMeasure> clazz = getDistanceMesureClass();
					try {
						DistanceMeasure measure = clazz.newInstance();
						reflectCluster(NewsKMeansClustering.clusterByKMeans(DocumentClusteringContainer.class.getResource("/reuter/convert"), measure,
								Integer.parseInt(clusterSizeChooseBox.getSelectedItem().toString()),
								Integer.parseInt(maxIterationField.getText()),
								2, 
								Integer.parseInt(minDfField.getText()),
								Integer.parseInt(maxDfPercentField.getText()), 
								Integer.parseInt(maxNGramSizeField.getText()),
								Double.parseDouble(convergenceDeltaField.getText())));
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						demoUI.hideWaitDialog();
					}
				}
			}).start();
			demoUI.showWaitDialog("Clustering document", "  Please wait until clustering finish...");
		}
	}

	private void clusteringByFuzzyKMeans() {
		if (validatePositiveIntegerField() && validatePositiveFloatField()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Class<? extends DistanceMeasure> clazz = getDistanceMesureClass();
					try {
						DistanceMeasure measure = clazz.newInstance();
						reflectCluster(NewsKMeansClustering.clusterByFuzzyKMeans(DocumentClusteringContainer.class.getResource("/reuter/convert"), measure,
								Integer.parseInt(clusterSizeChooseBox.getSelectedItem().toString()),
								Integer.parseInt(maxIterationField.getText()),
								2, 
								Integer.parseInt(minDfField.getText()),
								Integer.parseInt(maxDfPercentField.getText()), 
								Integer.parseInt(maxNGramSizeField.getText()),
								Double.parseDouble(convergenceDeltaField.getText()),
								Float.parseFloat(fuzinessField.getText())));
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						demoUI.hideWaitDialog();
					}
				}
			}).start();
			demoUI.showWaitDialog("Clustering document", "  Please wait until clustering finish...");
		}
	}

	private boolean validatePositiveFloatField() {
		if (validatePositiveFloatField(convergenceDeltaField) && validatePositiveFloatField(fuzinessField)) {
			return true;
		}
		return false;
	}

	private boolean validatePositiveFloatField(JTextField field) {
		String text = field.getText();
		if (StringUtils.isEmpty(text)) {
			field.setFocusable(true);
			return false;
		} else {
			try {
				float num = Float.parseFloat(text);
				if (num <= 0) {
					field.setFocusable(true);
					return false;
				}
				
			} catch (Exception e) {
				field.setFocusable(true);
				return false;
			}
		}
		
		return true;
	}

	private boolean validatePositiveIntegerField() {
		if (validatePositiveIntegerField(minDfField) && validatePositiveIntegerField(maxDfPercentField) && 
				validatePositiveIntegerField(maxNGramSizeField) && validatePositiveIntegerField(maxIterationField)) {
			return true;
		}
		return false;
	}
	
	private boolean validatePositiveIntegerField(JTextField field) {
		String text = field.getText();
		if (StringUtils.isEmpty(text)) {
			field.setFocusable(true);
			return false;
		} else {
			try {
				int num = Integer.parseInt(text);
				if (num <= 0) {
					field.setFocusable(true);
					return false;
				}
				
			} catch (Exception e) {
				field.setFocusable(true);
				return false;
			}
		}
		
		return true;
	}

	private void reflectCluster(Map<String, List<String>> clusterIdMap) throws IOException {
		this.clusterIdMap = clusterIdMap;
		this.docTerms = NewsKMeansClustering.readTFIDFTermVector();
		
		documentTable.clearCluster();
		List<String[]> clusters = new ArrayList<String[]>();
		
		for (Map.Entry<String, List<String>> entry : clusterIdMap.entrySet()) {
		      String clusterId = entry.getKey();
		      List<String> docIds = entry.getValue();
		      String[] idSize = {clusterId, String.valueOf(docIds.size())};
		      clusters.add(idSize);
		      documentTable.updateCluster(clusterId, docIds);
		      
		}
		
		clusterTable.displayCluster(clusterTableHeader, clusters);
	}
	
	private void loadTermVector() throws IOException {
		termTable.clearCluster();
		String[] row = clusterTable.getSelectedCluster();
		String clusterId = row[0];
		
		if (docTerms != null && clusterIdMap != null && row != null) {
			List<String> docIds = clusterIdMap.get(clusterId);
			List<String[]> termWeight = new ArrayList<String[]>(); 
			List<TFIDFTermVector> clusterWideVector = new ArrayList<TFIDFTermVector>();
			Map<String, List<int[]>> termFrequency = NewsKMeansClustering.readTermFrequency(docIds);
			
			System.out.println("cluster id [" + clusterId + "] contains [" + docIds.size() + "] documents");
			for (String docId : docIds) {
				List<TFIDFTermVector> termVector = docTerms.get(docId);
				System.out.println("doc id : " + docId + ", term size : " + termVector.size());
				List<int[]> freqVector = termFrequency.get(docId);
				
				mergeTermAndFrequency(clusterWideVector, termVector, freqVector);
			}
			
			for (TFIDFTermVector vector : clusterWideVector) {
				if (vector.getFound() > 0) {
					termWeight.add(new String[]{vector.getTerm(), String.valueOf(vector.getFound()), String.valueOf(vector.getWeight())});
				}
			}
			termTable.displayCluster(termTableHeader, termWeight);
		}
	}

	
	private void mergeTermAndFrequency(List<TFIDFTermVector> clusterWideVector, List<TFIDFTermVector> termVector, List<int[]> freqVector) {
		List<TFIDFTermVector> candidate = new ArrayList<TFIDFTermVector>();
		for (TFIDFTermVector term : termVector) {
			for (int[] freq : freqVector) {
				if (term.getTermId() == freq[0]) {
					TFIDFTermVector clone = new TFIDFTermVector(term.getTerm(), freq[1], term.getWeight());
					clone.setTermId(term.getTermId());
					candidate.add(clone);
				}
			}
		}
		
		List<TFIDFTermVector> newList = new ArrayList<TFIDFTermVector>();
		for (TFIDFTermVector cv : candidate) {
			boolean found = false;
			for (TFIDFTermVector v : clusterWideVector) {
				if (cv.getTerm().equals(v.getTerm())) {
					found = true;
					v.setFound(v.getFound() + cv.getFound());
				}
			}
			
			if (!found) {
				newList.add(cv);
			}
		}
		
		clusterWideVector.addAll(newList);

	}
	
	public void dispalyDocument(List<String> header, List<Document> documents) {
		documentTable.displayDocument(header, documents);
	}

	private Class<? extends DistanceMeasure> getDistanceMesureClass() {
		Class<? extends DistanceMeasure> clazz = null;
		
		if (distanceMeasureOptionBox.getSelectedIndex() == 0) {
			clazz = ChebyshevDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 1) {
			clazz = CosineDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 2) {
			clazz = EuclideanDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 3) {
			clazz = MahalanobisDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 4) {
			clazz = ManhattanDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 5) {
			clazz = MinkowskiDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 6) {
			clazz = SquaredEuclideanDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 7) {
			clazz = TanimotoDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 8) {
			clazz = WeightedDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 9) {
			clazz = WeightedEuclideanDistanceMeasure.class; 
		} else if (distanceMeasureOptionBox.getSelectedIndex() == 10) {
			clazz = WeightedManhattanDistanceMeasure.class; 
		}
		
		return clazz;
	}

}
