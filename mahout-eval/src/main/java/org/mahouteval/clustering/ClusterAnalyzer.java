package org.mahouteval.clustering;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.util.Version;

public class ClusterAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		List<String> stopWords = new ArrayList<String>();
		
		try {
			byte[] load = IOUtils.toByteArray(NewsKMeansClustering.class.getResourceAsStream("/stop_word.txt"));
			String convert = new String(load);
			stopWords = Arrays.asList(convert.split("\\s"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Tokenizer source = new LetterTokenizer(Version.LUCENE_46, reader);
	    TokenStream lowerCaseFilter = new LowerCaseFilter(Version.LUCENE_46, source);
	    LengthFilter lengthFilter = new LengthFilter(Version.LUCENE_46, lowerCaseFilter, 3, 100);
	    StopFilter stopWordFilter = new StopFilter(Version.LUCENE_46, lengthFilter, StopFilter.makeStopSet(Version.LUCENE_46, stopWords, true));
	    
	    return new TokenStreamComponents(source, stopWordFilter);
	}

}
