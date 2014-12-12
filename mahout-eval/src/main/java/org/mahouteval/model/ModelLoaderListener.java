package org.mahouteval.model;

import java.io.IOException;
import java.io.InputStream;

public interface ModelLoaderListener<T> {
	void addListener(Listener<T> listener);
	
	void clearModelLoaderListener();
	
	void loadModel(InputStream is) throws IOException;

	public interface Listener<T> {
		void modelLoadStarted(String[] columns);
		
		void modelLoaded(T csvBasedObject);
		
		void modelLoadEnded();
	}
}
