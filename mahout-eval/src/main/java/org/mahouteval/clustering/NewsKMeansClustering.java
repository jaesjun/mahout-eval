package org.mahouteval.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable;
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansDriver;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.mahouteval.model.TFIDFTermVector;

public class NewsKMeansClustering {
	public static Map<String, List<String>> clusterByKMeans(URL docInput, DistanceMeasure measure, int clusterSize, int maxIteration, 
			int minSupport, int minDocFreq, int maxDocFreqPctg, int ngram, double convergenceDelta) throws Exception {
	    
	    String vecPath = "docCluster/vecFiles";
	    String clusterPath = "docCluster/clusterPath";
	    
	    Configuration conf = new Configuration();
	    FileSystem fs = FileSystem.get(conf);
	    
		createTermVector(conf, vecPath, docInput, minSupport, minDocFreq, maxDocFreqPctg, ngram);

	    //String centroidPath = "docCluster/centroids";
	    //CanopyDriver.run(conf, new Path(vecPath, "tfidf-vectors"), new Path(centroidPath), measure, 250, 120, true, 0.0, false);
	    //KMeansDriver.run(conf, new Path(vecPath, "tfidf-vectors"), new Path(centroidPath, "clusters-0-final"), new Path(clusterPath), 0.001, 20, true, 0.0, false);
	    
	    Path clustersIn = new Path("docCluster/random-seeds");
	    RandomSeedGenerator.buildRandom(conf, new Path(vecPath, "tfidf-vectors"), clustersIn, clusterSize, measure);
	    KMeansDriver.run(new Path(vecPath, "tfidf-vectors"), clustersIn, new Path(clusterPath), convergenceDelta, maxIteration, true, 0.0, true);
	    
	    return loadCluster(conf, fs, clusterPath, "part-m-0");
	}

	public static Map<String, List<String>> clusterByFuzzyKMeans(URL docInput, DistanceMeasure measure, int clusterSize, int maxIteration, 
			int minSupport, int minDocFreq, int maxDocFreqPctg, int ngram, double convergenceDelta, float fuzzyFactor) throws Exception {
	    
	    String vecPath = "docCluster/vecFiles";
	    String clusterPath = "docCluster/clusterPath";
	    
	    Configuration conf = new Configuration();
	    FileSystem fs = FileSystem.get(conf);
	    
		createTermVector(conf, vecPath, docInput, minSupport, minDocFreq, maxDocFreqPctg, ngram);

	    Path clustersIn = new Path("docCluster/random-seeds");
	    RandomSeedGenerator.buildRandom(conf, new Path(vecPath, "tfidf-vectors"), clustersIn, clusterSize, measure);
	    
	    FuzzyKMeansDriver.run(new Path(vecPath, "tfidf-vectors"), clustersIn, new Path(clusterPath), convergenceDelta, maxIteration, fuzzyFactor, true, 
	    		false, 0.05, false);
	    
	    return loadCluster(conf, fs, clusterPath, "part-m-00000");
	}
	
	private static Map<String, List<String>> loadCluster(Configuration conf, FileSystem fs, String clusterPath, String clusterFile) throws IOException {
	    SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(clusterPath + "/" + Cluster.CLUSTERED_POINTS_DIR + "/" + clusterFile), conf);
	    
	    IntWritable key = new IntWritable();
	    WeightedPropertyVectorWritable value = new WeightedPropertyVectorWritable();
	    Map<String, List<String>> clusterSummary = new HashMap<String, List<String>>();
	    
	    while (reader.next(key, value)) {
	    	String clusterKey = key.toString();
	    	List<String> docIds = clusterSummary.get(clusterKey);
	    	if (docIds == null) {
	    		docIds = new ArrayList<String>();
	    		clusterSummary.put(clusterKey, docIds);
	    	} 
	    	
	    	docIds.add(((NamedVector) value.getVector()).getName());
	    }
	    
	    reader.close();
	    
	    return clusterSummary;
	}

	public static void createTermVector(Configuration conf, String vecPath, URL docInput, int minSupport, int minDocFreq, int maxDocFreqPctg, int ngram) throws Exception {
	    
	    String seqPath = "docCluster/seqFiles";

	    HadoopUtil.delete(conf, new Path("docCluster"));
	    
	    generateSeqFile(docInput, seqPath);
	    generateTFIDFTermVector(seqPath, vecPath, minSupport, minDocFreq, maxDocFreqPctg, ngram);
	}

	private static void generateSeqFile(URL docInput, String seqPath) throws IOException, URISyntaxException {
	    Configuration conf = new Configuration();
	    FileSystem fs = FileSystem.get(conf);
	    SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, new Path(seqPath, "documents.seq"), Text.class, Text.class); 
	    File inputDir = new File(docInput.toURI());
	    
	    for (File article : inputDir.listFiles()) {
	    	FileInputStream fis = new FileInputStream(article);
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
	    	String line = null;
	    	int lineNum = 1;
	    	String contents = "";
	    	while((line = reader.readLine()) != null) {
	    		if (lineNum++ > 2) {
	    			contents += line;
	    		}
	    	}
	    	
	    	writer.append(new Text(article.getName().substring(0, article.getName().length() - 4)), new Text(contents)); 

	    }
	    writer.close();
	}
	
	private static void generateTFIDFTermVector(String seqPath, String vecPath, int minSupport, int minDocFreq,
			int maxDocFreqPctg, int ngram) throws Exception {
		String[] commandParams = {"-i", seqPath, "-o", vecPath, "-ow", "-s", String.valueOf(minSupport),
				"-md", String.valueOf(minDocFreq), "-x", String.valueOf(maxDocFreqPctg),
				"-ng", String.valueOf(ngram), "-a", "org.mahouteval.clustering.ClusterAnalyzer"};
		SparseVectorsFromSequenceFiles.main(commandParams);
	}
	
	private static Map<Integer, String> readTermDictionary(Configuration conf) throws IOException {
	    FileSystem fs = FileSystem.get(conf);
		SequenceFile.Reader read = new SequenceFile.Reader(fs, new Path("docCluster/vecFiles/dictionary.file-0"), conf);
		
		IntWritable dicKey = new IntWritable();
		Text text = new Text();
		Map<Integer, String> dictionaryMap = new HashMap<Integer, String>();
		while (read.next(text, dicKey)) {
			dictionaryMap.put(dicKey.get(), text.toString());
		}
		read.close();
		
		return dictionaryMap;
	}
	
	public static Map<String, List<int[]>> readTermFrequency(List<String> docIds) throws IOException {
	    Configuration conf = new Configuration();
	    FileSystem fs = FileSystem.get(conf);
		SequenceFile.Reader read = new SequenceFile.Reader(fs, new Path("docCluster/vecFiles/tf-vectors/part-r-00000"), conf);
		
	    Text key = new Text();
	    VectorWritable value = new VectorWritable();
		Map<String, List<int[]>> frequencyMap = new HashMap<String, List<int[]>>();
		while (read.next(key, value)) {
			String docId = key.toString();
			if (docIds.contains(docId)) {
				System.out.println("Loading terms for : " + key.toString());
		    	RandomAccessSparseVector vect = (RandomAccessSparseVector)value.get();
		    	List<int[]> termCount = new ArrayList<int[]>();
		    	for (Element e : vect.all()) {
		    		int count = (int)e.get();
		    		int [] idCount = new int[]{e.index(), count};
		    		termCount.add(idCount);
		    	}
		    	
				frequencyMap.put(docId, termCount);
			}
		}
		read.close();
		
		return frequencyMap;
	}
	

	public static Map<String, List<TFIDFTermVector>> readTFIDFTermVector() throws IOException {
		Map<String, List<TFIDFTermVector>> vectors = new HashMap<String, List<TFIDFTermVector>>();
		
	    Path path = new Path("docCluster/vecFiles/tfidf-vectors/part-r-00000");
	    Configuration conf = new Configuration();
	    FileSystem fs = FileSystem.get(conf);
	    
	    Map<Integer, String> dictionaryMap = readTermDictionary(conf);
	    
	    SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
	    Text key = new Text();
	    VectorWritable value = new VectorWritable();
	    while (reader.next(key, value)) {
	    	String docId = key.toString();
	    	RandomAccessSparseVector vect = (RandomAccessSparseVector)value.get();
	    	List<TFIDFTermVector> terms = new ArrayList<TFIDFTermVector>();
	    	for (Element e : vect.all()) {
	    		if (e.get() > 0) {
	    			TFIDFTermVector term = new TFIDFTermVector(dictionaryMap.get(e.index()), 1, e.get());
	    			term.setTermId(e.index());
	    			terms.add(term);
	    		}
	    	}
	    	vectors.put(docId, terms);
	    }
	    reader.close();
	    
		return vectors;
	}
}
