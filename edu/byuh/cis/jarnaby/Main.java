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
	private final static String IMG = "Image";
	private final static String TXT = "Text";
	private static Map<Integer, Path> paths;
	private static Map<Integer, Page> pages = new HashMap<Integer, Page>();
	//private Map<Page, List<Path>> usedBy = new HashMap<Page, List<Path>>();
	
	private JPanel mainPanel;
	private JPanel start_menu;
	
	private StoryParser story;

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
		setContentPane(mainPanel);
		
		setVisible(true);
	}

	/**
	 * Switch to story UI, load story from file into canvas
	 */
	private void startReader()
	{
		gotoStoryView();
		try {
			loadStory(new File(Config.getStoryPath() + Config.storyPrefix + ".xml"));
		} catch (SAXException e) {
			Main.say("invalid XML! "+e.getMessage());
		} catch (IOException e) {
			Main.say("cannot find file.");
		}
	}
	
	private void startEditor()
	{
		JButton add_image_btn = new JButton(IMG);
		add_image_btn.addActionListener(this);
		canvas.add(add_image_btn);
		JButton add_txt_btn = new JButton(TXT);
		add_txt_btn.addActionListener(this);
		canvas.add(add_txt_btn);
		gotoStoryView();
//		revalidate();
	}
	
	private void gotoStoryView()
	{
		mainPanel.add(buildToolBar(), BorderLayout.SOUTH);
		mainPanel.add(canvas, BorderLayout.CENTER);
		revalidate();
	}

	/**
	 * Reads a story xml file and parses it into a Story object, then
	 * initializes the canvas to begin displaying the story
	 * @param story_file
	 * @throws SAXException
	 * @throws IOException
	 */
	private void loadStory(File story_file) throws SAXException, IOException {
		//load the input file
		XMLReader xml_reader = XMLReaderFactory.createXMLReader();
		story = new StoryParser();
		xml_reader.setContentHandler(story);
		xml_reader.setErrorHandler(story);
		FileReader file_reader = new FileReader(story_file);
		xml_reader.parse(new InputSource(file_reader));

		//pull data from the state.
		paths = story.getPaths();
		pages = story.getPages();
		setTitle(story.getStoryTitle());
		resolveLinks();
		Path dp = story.getDefaultPath();
		canvas.set(dp, 0);
		canvas.setPaths(paths, pages.values());
//		canvas.setPathsPerPage(story.getPathListPerPage());
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

	/**
	 * Builds navigation toolbar for story UI.
	 * Includes 'previous' & 'next' buttons, as well as 'aerial view'
	 * @return JPanel
	 */
	private JPanel buildToolBar() {
		JPanel bar = new JPanel();
		JButton backButton = new JButton(PREVIOUS_PAGE);
		backButton.setToolTipText("Go to the previous page in the current character's story");
		backButton.addActionListener(this);
		treeButton = new JToggleButton(AERIAL_VIEW);
		treeButton.setToolTipText("See an aerial view of the current character's story");
		treeButton.addActionListener(this);
		JButton nextButton = new JButton(NEXT_PAGE);
		nextButton.setToolTipText("Go to the next page in the current character's story");
		nextButton.addActionListener(this);
//		JButton exportButton = new JButton("test");
//		exportButton.addActionListener(this);
		bar.add(treeButton);
		bar.add(backButton);
		bar.add(nextButton);
//		bar.add(exportButton);
		return bar;
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
            mainPanel.remove(mainPanel.getComponent(0));
			startReader();
		}
        if (cmd.equals(NEW)) {
            mainPanel.remove(mainPanel.getComponent(0));
            startEditor();
		}
        if (cmd.equals("test")) {
        	story.exportStory();
		}
	}
	
	/**
	 * Check directory for available stories
	 */
	private void getAvailableStories()
	{
		File folder = new File("stories");
        File[] listOfFiles = folder.listFiles();
        String[] story_names = new String[listOfFiles.length];

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isDirectory()) {
                story_names[i] = listOfFiles[i].getName();
                System.out.println(listOfFiles[i].getName());
            }
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
