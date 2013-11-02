package edu.byuh.cis.jarnaby;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Main extends JFrame implements ActionListener, KeyListener {
	private static Main instance = null;
	private boolean testing;
	private static Canvas canvas;
	private JToggleButton treeButton;
	//private TreePanel treePanel;
	private final static String PREVIOUS_PAGE = "<";
	private final static String AERIAL_VIEW = "MAP";
	private final static String NEXT_PAGE = ">";
	private final static String LOAD = "Load";
	private final static String NEW = "New";
	private static Map<Integer, Path> paths;
	private static Map<Integer, Page> pages = new HashMap<Integer, Page>();
	//private Map<Page, List<Path>> usedBy = new HashMap<Page, List<Path>>();
	
	private JPanel mainPanel;
	private JPanel start_menu;

	public static Main instance() {
		if(instance == null) {
			instance = new Main();
		}
		return instance;
	}

	public Main() {
		//TODO look for which stories are installed
		//show the user a list of installed stories
		//let user pick one
		//load xml and go.
		//FOR NOW, just[ use LRRH.
		//setTitle("Infocomics 2010");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(Config.X_RESN, Config.Y_RESN+130);
		testing = true;

		canvas = new Canvas();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		start_menu = new JPanel();
		start_menu.setLayout(new BoxLayout(start_menu, BoxLayout.PAGE_AXIS));
		mainPanel.add(start_menu);

		JButton load_btn = new JButton(LOAD);
		load_btn.addActionListener(this);
		start_menu.add(load_btn);
		JButton create_btn = new JButton(NEW);
		create_btn.addActionListener(this);
		start_menu.add(create_btn);
//		mainPanel.add(buildToolBar(), BorderLayout.SOUTH);
//		mainPanel.add(canvas, BorderLayout.CENTER);
		setContentPane(mainPanel);
		
		if (testing) {
			testing();
		} else {

		
		}
		
		setVisible(true);
	}

	private void startReader()
	{
		mainPanel.remove(mainPanel.getComponent(0));
		
//		Main.say(mainPanel.getComponents().length);
		mainPanel.add(buildToolBar(), BorderLayout.SOUTH);
		mainPanel.add(canvas, BorderLayout.CENTER);
		
		revalidate();
		
		try {
			loadState(new File(Config.getStoryPath() + Config.storyPrefix + ".xml"));
		} catch (SAXException e) {
			Main.say("invalid XML!");
		} catch (IOException e) {
			Main.say("cannot find file.");
		}
	}

	private void loadState(File f) throws SAXException, IOException {
		//load the input file!!
		XMLReader xr = XMLReaderFactory.createXMLReader();
		StoryParser state = new StoryParser();
		xr.setContentHandler(state);
		xr.setErrorHandler(state);
		FileReader r = new FileReader(f);
		xr.parse(new InputSource(r));

		//pull data from the state.
		paths = state.getPaths();
		pages = state.getPages();
		setTitle(state.getStoryTitle() /*+ " - Jarnaby"*/);
		resolveLinks();
		Path dp = state.getDefaultPath();
		canvas.set(dp, 0);
		canvas.setPaths(paths, pages.values());
		canvas.setPathsPerPage(state.getPathListPerPage());
		canvas.addPagesPaths(paths, pages);
	}

	private void resolveLinks() {
		for (Page p : pages.values()) {
			for (Link ln : p.getLinks()) {
				int pageID = ln.toPageID;
				Page toPage = pages.get(pageID);
				ln.resolvePageIndex(toPage);
			}
		}
	}



	private JPanel buildToolBar() {
		JPanel bar = new JPanel();
		JButton backButton = new JButton(PREVIOUS_PAGE);
		backButton.setToolTipText("Go to the previous page in the current character's story");
		treeButton = new JToggleButton(AERIAL_VIEW);
		treeButton.setToolTipText("See an aerial view of the current character's story");
		JButton nextButton = new JButton(NEXT_PAGE);
		nextButton.setToolTipText("Go to the next page in the current character's story");
		backButton.addActionListener(this);
		treeButton.addActionListener(this);
		nextButton.addActionListener(this);
		bar.add(treeButton);
		bar.add(backButton);
		bar.add(nextButton);
		return bar;
	}

	private void testing() {
	//	Page p0 = new Page(0, new ImageIcon("stories/lrrh/page0.png"));
	//	canvas.setCurrentPage(p0);
	}


	public static void main(String[] args) {
		//this makes the GUI adopt the look-n-feel of the windowing system (Windows/X/Mac)
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { }

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Main.instance();
			}
		});
	}

	public static void say(Object o) {
		System.out.println(o);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Main.say(cmd);
		if (cmd.equals(PREVIOUS_PAGE)) {
			canvas.backOnePage();
		}
		if (cmd.equals(NEXT_PAGE)) {
			canvas.advancedPage();
		}
		if (cmd.equals(AERIAL_VIEW)) {
			canvas.toggleMode();
			Main.say("go to vistrail view");
		}
		if (cmd.equals(LOAD)) {
			startReader();
		}
	}

	public static int getWindowWidth() {
		return instance.getWidth();
	}

	public static int getWindowHeight() {
		return instance.getHeight();
	}
	
	//TODO think... should we move actionPerformed
	//and key* to the Canvas class?

	@Override
	public void keyPressed(KeyEvent e) {
		int kc = e.getKeyCode();
		switch (kc) {
		case KeyEvent.VK_RIGHT:
			canvas.advancedPage();
			break;
		case KeyEvent.VK_LEFT:
			canvas.backOnePage();
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	public static Path getCurrentPath() {
		return canvas.getCurrentPath();
	}
	
	public static Page getCurrentPage() {
		return canvas.getCurrentPage();
	}
	
	public static Collection<Path> getPaths() {
		return paths.values();
	}

}
