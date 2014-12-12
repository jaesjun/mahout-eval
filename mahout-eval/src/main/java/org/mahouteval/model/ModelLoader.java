package org.mahouteval.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class ModelLoader {
	private List<ModelLoaderListener.Listener<Preference>> preferenceLoaderListener = new ArrayList<ModelLoaderListener.Listener<Preference>>();
	private List<ModelLoaderListener.Listener<Item>> itemLoaderListener = new ArrayList<ModelLoaderListener.Listener<Item>>();
	private List<ModelLoaderListener.Listener<User>> userLoaderListener = new ArrayList<ModelLoaderListener.Listener<User>>();
	private List<ModelLoaderListener.Listener<Document>> documentLoaderListener = new ArrayList<ModelLoaderListener.Listener<Document>>();
	
	private String[] parseRecord(String record) {
		return record.split(",");
	}

	public void loadUser(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		
		String line = null;
		int rowIndex = 0;
		while ((line = br.readLine()) != null) {
			String[] columns = parseRecord(line);
			if (rowIndex++ == 0) {
				notifyLoadUserStarted(columns);
			} else if (columns != null && columns.length > 4) {
				User user = new User();
				try {
					user.setId(Long.parseLong(columns[0]));
					for (String column : columns) {
						user.addColumn(column);
					}
					notifyUserLoad(user);
				} catch (NumberFormatException ne) {
					System.err.println("Skip user record : " + line);
				}
			}
		}
		
		notifyLoadUserEnded();
	}

	public void loadItem(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		
		String line = null;
		int rowIndex = 0;
		while ((line = br.readLine()) != null) {
			String[] columns = parseRecord(line);
			if (rowIndex++ == 0) {
				notifyLoadItemStarted(columns);
			} else if (columns != null && columns.length > 1) {
				try {
					Item item = new Item();
					item.setId(Long.parseLong(columns[0]));
					for (String column : columns) {
						item.addColumn(column);
					}
					notifyItemLoad(item);
				} catch (NumberFormatException ne) {
					System.err.println("Skip item record : " + line);
				}
			}
		}
		
		notifyLoadItemEnded();
	}

	public void loadPreference(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		
		String line = null;
		int rowIndex = 0;
		while ((line = br.readLine()) != null) {
			String[] columns = parseRecord(line);
			if (rowIndex++ == 0) {
				notifyLoadPreferenceStarted(columns);
			} else if (columns != null && columns.length > 3) {
				Preference preference = new Preference();
				try {
					preference.setUserId(Integer.parseInt(columns[0]));
					preference.setItemId(Integer.parseInt(columns[1]));
					preference.setPreference(Integer.parseInt(columns[2]));
					notifyPreferenceLoad(preference);
				} catch (NumberFormatException ne) {
					System.err.println("Skip preference record : " + line);
				}
			}
		}
		
		notifyLoadPreferenceEnded();
	}

	public void loadDocument(URL url) throws URISyntaxException, IOException {
		String[] header = {"Cluster ID", "document ID", "Title", "Contents"};
		notifyLoadDocumentStarted(header);
		
		File file = new File(url.toURI());
		for (File news : file.listFiles()) {
			Document document = new Document();
			
			FileInputStream fis = new FileInputStream(news);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			br.readLine();
			br.readLine();
			
			document.setId(news.getName().substring(0, news.getName().length() - 4));
			document.setTitle(br.readLine());
			br.readLine();
			String line = null;
			String contents = "";
			while((line = br.readLine()) != null) {
				contents += line;
			}
			
			document.setContents(contents);
			if (document.getTitle() != null && document.getContents() != null) {
				if (!StringUtils.isEmpty(document.getTitle()) || StringUtils.isEmpty(document.getContents())) {
					notifyDocumentLoad(document);
				}
			}
		}
	}

	private void notifyLoadDocumentStarted(String[] columns) {
		List<ModelLoaderListener.Listener<Document>> listeners = Collections.synchronizedList(documentLoaderListener);	
		for (ModelLoaderListener.Listener<Document> listener : listeners) {
			listener.modelLoadStarted(columns);
		}
	}


	private void notifyLoadUserStarted(String[] columns) {
		List<ModelLoaderListener.Listener<User>> listeners = Collections.synchronizedList(userLoaderListener);	
		for (ModelLoaderListener.Listener<User> listener : listeners) {
			listener.modelLoadStarted(columns);
		}
	}

	private void notifyLoadItemStarted(String[] columns) {
		List<ModelLoaderListener.Listener<Item>> listeners = Collections.synchronizedList(itemLoaderListener);	
		for (ModelLoaderListener.Listener<Item> listener : listeners) {
			listener.modelLoadStarted(columns);
		}
	}

	private void notifyLoadPreferenceStarted(String[] columns) {
		List<ModelLoaderListener.Listener<Preference>> listeners = Collections.synchronizedList(preferenceLoaderListener);	
		for (ModelLoaderListener.Listener<Preference> listener : listeners) {
			listener.modelLoadStarted(columns);
		}
	}
	
	private void notifyLoadUserEnded() {
		List<ModelLoaderListener.Listener<User>> listeners = Collections.synchronizedList(userLoaderListener);	
		for (ModelLoaderListener.Listener<User> listener : listeners) {
			listener.modelLoadEnded();
		}
	}

	private void notifyLoadItemEnded() {
		List<ModelLoaderListener.Listener<Item>> listeners = Collections.synchronizedList(itemLoaderListener);	
		for (ModelLoaderListener.Listener<Item> listener : listeners) {
			listener.modelLoadEnded();
		}
	}

	private void notifyLoadPreferenceEnded() {
		List<ModelLoaderListener.Listener<Preference>> listeners = Collections.synchronizedList(preferenceLoaderListener);	
		for (ModelLoaderListener.Listener<Preference> listener : listeners) {
			listener.modelLoadEnded();
		}
	}

	public void addPreferenceLoadListener(ModelLoaderListener.Listener<Preference> listener) {
		List<ModelLoaderListener.Listener<Preference>> listeners = Collections.synchronizedList(preferenceLoaderListener);	
		listeners.add(listener);
	}

	public void addItemLoadListener(ModelLoaderListener.Listener<Item> listener) {
		List<ModelLoaderListener.Listener<Item>> listeners = Collections.synchronizedList(itemLoaderListener);	
		listeners.add(listener);
	}

	public void addUserLoadListener(ModelLoaderListener.Listener<User> listener) {
		List<ModelLoaderListener.Listener<User>> listeners = Collections.synchronizedList(userLoaderListener);	
		listeners.add(listener);
	}

	public void addDocumentLoadListener(ModelLoaderListener.Listener<Document> listener) {
		List<ModelLoaderListener.Listener<Document>> listeners = Collections.synchronizedList(documentLoaderListener);	
		listeners.add(listener);
	}

	private void notifyUserLoad(User user) {
		List<ModelLoaderListener.Listener<User>> listeners = Collections.synchronizedList(userLoaderListener);	
		for (ModelLoaderListener.Listener<User> listener : listeners) {
			listener.modelLoaded(user);
		}
	}

	private void notifyItemLoad(Item item) {
		List<ModelLoaderListener.Listener<Item>> listeners = Collections.synchronizedList(itemLoaderListener);	
		for (ModelLoaderListener.Listener<Item> listener : listeners) {
			listener.modelLoaded(item);
		}
	}

	private void notifyPreferenceLoad(Preference preference) {
		List<ModelLoaderListener.Listener<Preference>> listeners = Collections.synchronizedList(preferenceLoaderListener);	
		for (ModelLoaderListener.Listener<Preference> listener : listeners) {
			listener.modelLoaded(preference);
		}
	}

	private void notifyDocumentLoad(Document document) {
		List<ModelLoaderListener.Listener<Document>> listeners = Collections.synchronizedList(documentLoaderListener);	
		for (ModelLoaderListener.Listener<Document> listener : listeners) {
			listener.modelLoaded(document);
		}
	}

	public void clearPreferenceLoaderListener() {
		this.preferenceLoaderListener.clear();
	}

	public void clearItemLoaderListener() {
		this.itemLoaderListener.clear();
	}

	public void clearModelLoaderListener() {
		this.userLoaderListener.clear();
	}

}
