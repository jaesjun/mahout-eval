package org.mahouteval.model;

public class TFIDFTermVector {
	String term;
	long found;
	double weight;
	int termId;
	
	public TFIDFTermVector(String term, long found, double weight) {
		this.term = term;
		this.found = found;
		this.weight = weight;
	}

	public String getTerm() {
		return term;
	}

	public double getWeight() {
		return weight;
	}
	
	public long getFound() {
		return found;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public void setFound(long found) {
		this.found = found;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	
	
	
}
