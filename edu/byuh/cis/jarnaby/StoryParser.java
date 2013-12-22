package edu.byuh.cis.jarnaby;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.image.codec.jpeg.JPEGCodec;

public class StoryParser extends DefaultHandler {
	
	public static final String ROOT_NODE = "jarnaby";
	public static final String VERSION = "version";
	public static final String STORY = "story";
	public static final String TITLE = "title";
	public static final String IMAGE = "image";
	public static final String ID = "id";
	public static final String FILE = "file";
	public static final String PAGE = "page";
	public static final String IMAGE_ID = "image_id";
	public static final String TEXT = "text";
	public static final String LINK = "link";
	public static final String TRACKREF = "pathref";
	public static final String PAGEREF = "pageref";
	public static final String RECT = "rect";
	public static final String X = "x";
	public static final String Y = "y";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String PATH = "path";
	public static final String NAME = "name";
	public static final String DEFAULT = "default";
	public static final String BKGD_TEXTURE = "graph_background";
	
	private String storyTitle;
	private Map<String, ImageIcon> images = new HashMap<String, ImageIcon>();
	private Map<Integer, Page> pages = new HashMap<Integer, Page>();
	private Map<Integer, Path> paths = new HashMap<Integer, Path>();
	private List<TempLink> tmpLinks = new ArrayList<TempLink>();
	private Map<Page, List<Path>> usedBy = new HashMap<Page, List<Path>>();
	private Page currentPage;
	private TempLink currentLink;
	private Path currentPath;
	private Path defaultPath;
	private State state = State.DEFAULT;
	private Paint graphBackground;

	
	private enum State {
		EXPECTING_TITLE,
		EXPECTING_TEXT,
		DEFAULT
	}
	
	private class TempLink {
		Page fromPage;
		int toPageID, toPath;
		Rectangle2D rect;
		String text;
	}
	
	public StoryParser() {
		super();
		currentPage = null;
		currentLink = null;
	}
	
	
	@Override
    public void startElement (String junk1, String junk2,
			      String tag, Attributes atts)
    {
		if ("".equals (junk1)) {
			//System.out.println(tag);
			if (tag.equals(ROOT_NODE)) {
				float version = Float.parseFloat(atts.getValue(VERSION));
				//only supported version right now is 1.0, so we don't need
				//to put any logic here yet.
			}
			if (tag.equals(TITLE)) {
				state = State.EXPECTING_TITLE;
			}
			if (tag.equals(IMAGE)) {
				//int id = Integer.parseInt(atts.getValue(ID));
				String id = atts.getValue(ID);
				ImageIcon icon = new ImageIcon(Config.getImagePath() + atts.getValue(FILE));
				images.put(id, icon);
			}
			if (tag.equals(PAGE)) {
				int id = Integer.parseInt(atts.getValue(ID));
				//int pid = Integer.parseInt(atts.getValue(IMAGE_ID));
				String pid = atts.getValue(IMAGE_ID);
				Page p = new Page(id, pid);
				pages.put(id, p);
				currentPage = p;
			}
			if (tag.equals(TEXT)) {
				if (currentPage != null) {
					state = State.EXPECTING_TEXT;
				}
			}
			if (tag.equals(LINK)) {
				int track = Integer.parseInt(atts.getValue(TRACKREF));
				int pageID = Integer.parseInt(atts.getValue(PAGEREF));
				String text = atts.getValue(TEXT);
				TempLink tmp = new TempLink();
				tmp.fromPage = currentPage;
				tmp.toPageID = pageID;
				tmp.toPath = track;
				tmp.text = text;
				tmp.rect = new Rectangle2D.Float();
				tmpLinks.add(tmp);
				currentLink = tmp;
			}
			if (tag.equals(RECT)) {
				float x = Config.X_RESN * Float.parseFloat(atts.getValue(X));
				float y = Config.Y_RESN * Float.parseFloat(atts.getValue(Y));
				float w = Config.X_RESN * Float.parseFloat(atts.getValue(WIDTH));
				float h = Config.Y_RESN * Float.parseFloat(atts.getValue(HEIGHT));
				currentLink.rect.setRect(x,y,w,h);
			}
			if (tag.equals(PATH)) {
				int id = Integer.parseInt(atts.getValue(ID));
				String name = atts.getValue(NAME);
				boolean defalt = Boolean.parseBoolean(atts.getValue(DEFAULT));
				currentPath = new Path(name, id);
				if (defalt) currentPath.makeDefault();
				paths.put(id, currentPath);
				if (defalt) {
					defaultPath = currentPath;
				}
			}
			if (tag.equals(PAGEREF)) {
				int pid = Integer.parseInt(atts.getValue(ID));
				currentPath.placeholders.add(pid);
			}

		} else
		    System.out.println("Start element: {" + junk1 + "}" + junk2);
    }

	@Override
	public void characters(char[] ch, int start, int length) 
     throws SAXException {
		String foo = new String(ch, start, length).trim();
		switch (state) {
		case EXPECTING_TITLE:
			storyTitle = foo;
			Main.say("The title of our story is "  + storyTitle);
			state = State.DEFAULT;
			break;
		case EXPECTING_TEXT:
			if (currentPage != null) {
				currentPage.addText(foo);
				
				state = State.DEFAULT;
			}		
			break;
		}
	}

	@Override
    public void endElement (String junk1, String junk2, String tag)
    {
		if ("".equals (junk1)) {
			if (tag.equals(TITLE)) {
				state = State.DEFAULT;
			}
			if (tag.equals(PAGE)) {
				state = State.DEFAULT;
				currentPage = null;
			}
			if (tag.equals(LINK)) {
				state = State.DEFAULT;
				currentLink = null;
			}
			if (tag.equals(TEXT)) {
				state = State.DEFAULT;
			}
			
		}	
		else {
		    System.out.println("End element:   {" + junk1 + "}" + junk2);
	    }
    }
	
	@Override
	public void endDocument() {
		//loop through all the Page objects, and update their images.
		for (Page p : pages.values()) {
			p.setImage(images.get(p.pictID));
		}
		
		//update link references to pages that maybe weren't there when
		//we parsed the first time.
		for (TempLink tl : tmpLinks) {
			Link ln = Link.createPartialLink(paths.get(tl.toPath), tl.toPageID);
			ln.rect = tl.rect;
			tl.fromPage.addLink(ln);
			ln.setText(tl.text);
		}
		
		//update the paths, in case of page references we haven't parsed yet
		for (Path path : paths.values()) {
			for (int j=0; j<path.placeholders.size(); j++) {
				int id = path.placeholders.get(j);
				path.addPage(id, pages.get(id));
			}
		}
		
		//update the links so they point to the index of the page relative to
		//its path, instead of using absolute page ID numbers.
		resolveLinks();
		//for (Path path : paths.values()) {
			//path.resolveLinks();
		//}
		//System.out.println("FINI");
		
		//make a list of which paths use each page
		for (Page page : pages.values()) {
//			List<Path> pathList = usedBy.get(page);
//			if (pathList == null) {
			List<Path> pathList = new ArrayList<Path>();
//			}
			for (Path path : paths.values()) {
				if (path.contains(page)) {
					pathList.add(path);
				}
			}
			usedBy.put(page, pathList);
		}
	}
	
	/**
	 * Store xml-version of the story into a file
	 */
	public void exportStory()
	{
		String story_contents = storyToXml();
		String folder_path = System.getProperty("user.dir"); //TODO
		String file_name = storyTitle.replaceAll(" ", "_").toLowerCase() + ".xml";
		String full_path = folder_path + "\\"+ file_name;
		System.out.println("path is "+ full_path);
		writeFile(full_path, story_contents);
	}
	
	/**
	 * Convert loaded story into xml string
	 */
	private String storyToXml()
	{
		String version = "1.0";
		String story = "<jarnaby version=\"" + version + "\">";
		story += "<story>";
		String file_placeholder = "lrrh_wolf_pounce.png";
		story += "<title file=\""+ file_placeholder +"\">"+ storyTitle +"</title>";
		story += getImagesXml();
		story += getPagesXml();
		story += getPathsXml();
		story += "</story>";
		story += "</jarnaby>";
		story = story.replaceAll(">", ">\r\n");
		return story;
	}
	
	/**
	 * Generate xml for the story's images
	 */
	private String getImagesXml()
	{
		String images_xml = "";
		for (Map.Entry<String, ImageIcon> img : images.entrySet()) {
			ImageIcon icon = img.getValue();
			String filename = icon.toString().replaceAll(".*/","");
			images_xml += "<image id=\""+ img.getKey() +"\" file=\""+filename+"\" />";
		}
		return images_xml;
	}
	
	/**
	 * Generate xml for the story's pages
	 */
	private String getPagesXml()
	{
		String pages_xml = "";
		for (Page page : pages.values()) {
			pages_xml += page.toXml();
		}
		return pages_xml;
	}
	
	/**
	 * Generate xml for the story's paths
	 */
	private String getPathsXml()
	{
		String paths_xml = "";
		for (Path path : paths.values()) {
			paths_xml += path.toXml();
		}
		return paths_xml;
	}

	public Map<Integer, Path> getPaths() {
		return paths;
	}
	
	public Map<Page, List<Path>> getPathListPerPage() {
		return usedBy;
	}
	
	public Path getDefaultPath() {
		return defaultPath;
	}


	public Map<Integer, Page> getPages() {
		return pages;
	}
	
	public Paint getGraphBackground() {
		return graphBackground;
	}


	public String getStoryTitle() {
		return storyTitle;
	}
	
	private void resolveLinks() {
		for (Page p : pages.values()) {
			for (Link ln : p.getLinks()) {
				Path toPath = ln.getPath();
				for (int i=0; i<toPath.placeholders.size(); ++i) {
					if (ln.toPageID == toPath.placeholders.get(i)) {
						ln.toPageIndex = i;
					}
				}
			}
		}
	}

	/**
	 * Write content to file_path, creating directories and/or file if needed
	 * @param file_path
	 * @param content
	 */
	private void writeFile(String file_path, String content) {
		try {
			File file = new File(file_path);
			
			if(!file.getParentFile().exists()) {
				System.out.println("Creating new directory...");
			     file.getParentFile().mkdirs();
			}
 
			if (!file.exists()) {
				System.out.println("Creating new file...");
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
 
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
