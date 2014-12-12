package org.mahouteval.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JFrame;

import org.mahouteval.model.ModelLoader;


public class MahoutDemoFrame extends JFrame implements WindowListener {
	private MahoutDemoUI mahoutDemoUI;
	
    public MahoutDemoFrame(final String title) throws IOException, URISyntaxException {
        super(title);
        addWindowListener(this);
        ModelLoader loader = new ModelLoader();
        
        mahoutDemoUI = new MahoutDemoUI(getContentPane(), loader);
        mahoutDemoUI.loadPreference(MahoutDemoFrame.class.getResourceAsStream("/grouplens/ratings/u.data"));
        mahoutDemoUI.loadItem(MahoutDemoFrame.class.getResourceAsStream("/grouplens/data/u.item"));
        mahoutDemoUI.loadUser(MahoutDemoFrame.class.getResourceAsStream("/grouplens/data/u.user"));
        mahoutDemoUI.loadDocument(MahoutDemoFrame.class.getResource("/reuter/convert"));
    }

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
        if (e.getWindow() == this) {
            dispose();
            System.exit(0);
        }
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
	
	public static void main(String[] args) {
		try {
			MahoutDemoFrame frame = new MahoutDemoFrame("mahout demo with movie preferences");
			frame.pack();
	        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH );
			frame.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
