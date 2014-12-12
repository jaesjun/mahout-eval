package org.mahouteval.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.mahouteval.cf.DataModelBuilder;
import org.mahouteval.cf.UserItemRecommender;
import org.mahouteval.model.Document;
import org.mahouteval.model.Item;
import org.mahouteval.model.ModelLoader;
import org.mahouteval.model.ModelLoaderListener;
import org.mahouteval.model.Preference;
import org.mahouteval.model.User;

public class MahoutDemoUI {
	private CfContainer cfContainer;
	private ClusteringContainer clusteringContainer;
	private DocumentClusteringContainer documentClusteringContainer;
	
	private JTabbedPane tabPane = new JTabbedPane();
	
	private Container container;
	
	private DataModelBuilder builder = new DataModelBuilder();
	private UserItemRecommender userItemRecommender;
	private ModelLoader csvModelLoader;

	private JDialog msgPopupDlg;

	public enum LoadEnum {
		USER, ITEM, PREFERENCE
	};
	
	public MahoutDemoUI(Container container, ModelLoader csvModelLoader) {
		this.container = container;
		this.csvModelLoader = csvModelLoader;
		initUIs();
	}

	private void initUIs() {
		cfContainer = new CfContainer(this);
		clusteringContainer = new ClusteringContainer(this);
		documentClusteringContainer = new DocumentClusteringContainer(this);
		
		tabPane.addTab("Collaborative Filtering", cfContainer);
		tabPane.addTab("2D Vector Clustering", clusteringContainer);
		tabPane.addTab("Document Clustering", documentClusteringContainer);
		
		container.add(tabPane, BorderLayout.CENTER);
	}

	public void loadPreference(InputStream is) throws IOException {
		ModelLoaderListener.Listener<Preference> listener = new ModelLoaderListener.Listener<Preference>() {
			@Override
			public void modelLoadStarted(String[] columns) {
				cfContainer.clearPreferenceChart();
			}

			@Override
			public void modelLoaded(Preference csvBasedObject) {
				cfContainer.addPreference(csvBasedObject);
				builder.addPreference(csvBasedObject);
			}

			@Override
			public void modelLoadEnded() {
			}
		};
		
		csvModelLoader.addPreferenceLoadListener(listener);
		csvModelLoader.loadPreference(is);
		userItemRecommender = new UserItemRecommender(builder.buildDataModel());
	}

	public void loadUser(InputStream is) throws IOException {
		final List<User> users = new ArrayList<User>();
		final List<String> header = new ArrayList<String>();
		final List<Long> maxUserId = new ArrayList<Long>();
		maxUserId.add(-1L);
		
		ModelLoaderListener.Listener<User> listener = new ModelLoaderListener.Listener<User>() {
			@Override
			public void modelLoadStarted(String[] columns) {
				for (String column : columns) {
					header.add(column);
				}
			}
			
			@Override
			public void modelLoadEnded() {
			}

			@Override
			public void modelLoaded(User csvBasedObject) {
				if (maxUserId.get(0) < csvBasedObject.getId()) {
					maxUserId.remove(0);
					maxUserId.add(csvBasedObject.getId());
				}
				users.add(csvBasedObject);
			}
		};
		
		csvModelLoader.addUserLoadListener(listener);
		csvModelLoader.loadUser(is);
		
		cfContainer.setMaxUserId(maxUserId.get(0));
		cfContainer.displayUser(header, users);
	}
	
	public void loadDocument(URL url) throws URISyntaxException, IOException {
		final List<Document> documents = new ArrayList<Document>();
		final List<String> header = new ArrayList<String>();
		
		ModelLoaderListener.Listener<Document> listener = new ModelLoaderListener.Listener<Document>() {
			@Override
			public void modelLoadStarted(String[] columns) {
				for (String column : columns) {
					header.add(column);
				}
			}
			
			@Override
			public void modelLoadEnded() {
			}

			@Override
			public void modelLoaded(Document csvBasedObject) {
				documents.add(csvBasedObject);
			}
		};
		
		csvModelLoader.addDocumentLoadListener(listener);
		csvModelLoader.loadDocument(url);
		documentClusteringContainer.dispalyDocument(header, documents);
	}
	
	public void loadItem(InputStream is) throws IOException {
		final List<Item> items = new ArrayList<Item>();
		final List<String> header = new ArrayList<String>();
		final List<Long> maxItemId = new ArrayList<Long>();
		maxItemId.add(-1L);
		
		ModelLoaderListener.Listener<Item> listener = new ModelLoaderListener.Listener<Item>() {
			@Override
			public void modelLoadStarted(String[] columns) {
				for (String column : columns) {
					header.add(column);
				}
			}

			@Override
			public void modelLoaded(Item csvBasedObject) {
				if (maxItemId.get(0) < csvBasedObject.getId()) {
					maxItemId.remove(0);
					maxItemId.add(csvBasedObject.getId());
				}
				
				items.add(csvBasedObject);
			}

			@Override
			public void modelLoadEnded() {
			}

		};
		
		csvModelLoader.addItemLoadListener(listener);
		csvModelLoader.loadItem(is);
		
		cfContainer.setMaxItemId(maxItemId.get(0));
		cfContainer.displayItem(header, items);
	}
	
	public void setPreferredSize(Dimension preferredSize) {
		container.setPreferredSize(preferredSize);
	}

	public void loadData(LoadEnum loadEnum) {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(container);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			FileInputStream fis = null;
			
			try {
				fis = new FileInputStream(file);
				if (loadEnum == LoadEnum.USER) {
					loadUser(fis);
				} else if (loadEnum == LoadEnum.ITEM) {
					loadItem(fis);
				} else {
					loadPreference(fis);
				}
			} catch (Exception e) {
				JOptionPane.showConfirmDialog(container, e.getMessage());
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	public DataModelBuilder getDataModelBuilder() {
		return builder;
	}

	public UserItemRecommender getUserItemRecommender() {
		return userItemRecommender;
	}
	
	public void showWaitDialog(String title, String msg) {
		msgPopupDlg = new JDialog();
		JLabel label = new JLabel(msg);
		msgPopupDlg.setLocationRelativeTo(null);
		msgPopupDlg.setTitle(title);
		msgPopupDlg.add(label);
		msgPopupDlg.pack();	
		msgPopupDlg.setSize(300, 100);
		msgPopupDlg.setModal(true);
		msgPopupDlg.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int x = (screenSize.width - msgPopupDlg.getWidth()) / 2;
		int y = (screenSize.height - msgPopupDlg.getHeight()) / 2;
		
		msgPopupDlg.setLocation(x, y);
		
		msgPopupDlg.setResizable(false);
		msgPopupDlg.setVisible(true);
	}
	
	public void hideWaitDialog() {
		if (msgPopupDlg != null) {
			msgPopupDlg.setVisible(false);
			msgPopupDlg = null;
		}
	}

}
