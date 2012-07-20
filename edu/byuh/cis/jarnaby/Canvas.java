package edu.byuh.cis.jarnaby;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

public class Canvas extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

	private Page currentPage;
	private TreePanel treePanel;
	private PathIterator currentPath;
	private Map<Page, List<Path>> usedBy = new HashMap<Page, List<Path>>();
	private boolean storyMode;
	
	private static Map<Integer, Path> paths;
	private static Map<Integer, Page> pages = new HashMap<Integer, Page>();

	public Canvas() {
		super();
		currentPath = new PathIterator();
		storyMode = true;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		
	}

	public void setPaths(Map<Integer, Path> paths, Collection<Page> pages) {
		treePanel = new TreePanel(this, paths, pages);
	}

	public void setBackgroundImage(Paint img) {
		if (treePanel != null) {
			treePanel.setBackgroundTexture(img);
		}
	}

	@Override
	public void paintComponent(Graphics g1) {
		draw((Graphics2D)g1);
	}

	public void draw(Graphics2D g) {
		if (storyMode) {
			g.setColor(Color.WHITE);
			g.fillRect(0,0,getWidth(), getHeight());
			if (currentPage != null) {
				currentPage.draw(g);
			}
		} else {
			treePanel.draw(g);
		}

	}

	public void setCurrentPage(Page p) {
		currentPage = p;
		currentPage.setVisited(true);
//		Main.say(p.getPictID() + " is VISITED");
		repaint();
	}

	public Page getCurrentPage() {
		return currentPage;
	}

	public void set(Path p, int pos) {
		follow(new Link(p, pos));
	}

	/**
	 * Follow a link without creating a backlink to the current location
	 * @param ln the path/page to go to
	 */
	public void follow(Link ln) {
		Page cp = currentPath.go(ln);
		cp.setBacklink(null);
		setCurrentPage(cp);
	}
	
	/**
	 * Follow a link AND create a backlink to the current location
	 * @param ln the path/page to go to
	 */
	public void followLink(Link ln) {
		Link backlink = new Link(currentPath);
		Page cp = currentPath.go(ln);
		cp.setBacklink(backlink);
		setCurrentPage(cp);
	}


	public void advancedPage() {
//		Main.say("advance page!");
		Page next = currentPath.next();
		if (next != null) {
			storyMode = true;
			setCurrentPage(next);
		}
//		Main.say("position: "+ currentPath.getPos());
	}

	public void backOnePage() {
//		Main.say("previous page!");
		
		//ATTN: Backlink behavior removed temporarily to conform more to Android version
//		Link bak = currentPage.getBacklink(); //page has backlink only if user 'linked' to it from another page
//		if (bak != null) {
//			Main.say("bak NOT NULL!");
//			follow(bak);
		
		int currentPageIndex = currentPage.getID();
		int currentPathIndex = currentPath.getPath().getID();
		int currentPageInPath = currentPath.getPos();
		
		if(currentPageInPath == 0){
			boolean found = false;
//			Main.say("SEARCHING BACK!");
//			if(pages.isEmpty()) Main.say("EMPTY PAGES!!!! WAAAA");
			for (Map.Entry<Integer, Page> page : pages.entrySet()) { //loop through ALL pages
				if (page.getValue().hasLink()) { //if the page has any links
					for (Link l :page.getValue().getLinks()) { //loop through all the links
						if ( l.toPageID == currentPageIndex && l.toPath.getID() == currentPathIndex ) { //if the link points to the page and path you're on
							currentPageIndex = page.getKey(); //take the page which has the link, and set it as your current page
							for( int i: paths.keySet()) { //loop through all paths
								if ( i != currentPathIndex && paths.get(i).placeholders.contains(currentPageIndex)) { //if the path contains your new current page, and you're not in that path already
									currentPathIndex = i; //your current path is set to this new path
									for(int j = 0; j < paths.get(i).placeholders.size(); j++ ) { //loop through your new path's pages
										if(paths.get(i).placeholders.get(j) == currentPageIndex){ //find your new 'current page' within your new current path
											currentPageInPath = j; //once found, update your PageInPath to the corresponding place in the path
//											if(storyMode) { //if you're not in map mode, display story image
												setCurrentPage(page.getValue());
												set(paths.get(currentPathIndex), currentPageInPath);
												storyMode = true;
//												Main.say("Page index: "+ currentPageIndex);
//												Main.say("Path index: "+ currentPathIndex);
//												Main.say("Position: "+ currentPageInPath);
//											}
											found = true;
											break;
										}
									}
								}
								if (found) break;
							}
							if(found) break;
						}
					}
				}
				if (found) break;
			}
		} else {
			Page prev = currentPath.prev();
			if (prev != null) {
				storyMode = true;
				setCurrentPage(prev);
			}
		}
			//else don't do anything. Just stay on current page.
//		}
//		Main.say("position: "+ currentPath.getPos());

	}

	public void handleClick(Point p) {
		if (storyMode) {
			Link result = currentPage.handleClick(p);
			if (result != null) {
				followLink(result);
//				Main.say("Found LINK");
				//set(result.toPath, result.toPageIndex);
				//currentPath.go(result);
			}
		} else {
			treePanel.handleClick(p);
		}

	}

	public void toggleMode() {
		storyMode = !storyMode;
		treePanel.resetTransformations();
		repaint();
	}

	//	public class Mouse extends MouseInputAdapter {
	boolean dragging = false;
	Point mouseDown;

	@Override
	public void mousePressed(MouseEvent me) {
		Point p = me.getPoint();
		dragging = false;
		if (contains(p)) {
			dragging = true;
		}
		if (!storyMode) {
			treePanel.handleMousePressed(p);
		}
		mouseDown = p;
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		Point p = me.getPoint();
		int dx = mouseDown.x - p.x;
		//int dy = mouseDown.y - p.y;
		if (dragging) {
			if (storyMode) {
				handleDrags(p, dx);
			}
		}
		if (Math.abs(dx) < 10) {
			handleClick(p);
		}
		dragging = false;
	}

	public void handleDrags(Point p, int dx) {
		if (dx > 50) {
			//user made a right-to-left gesture
			//so advance one page
			advancedPage();
		}
		if (dx < -50) {
			//user made a left-to-right gesture
			//so go back one page
			backOnePage();
		}
	}

	@Override 
	public void mouseMoved(MouseEvent me) {
		Point p = me.getPoint();
		if (storyMode) {
			Link result = currentPage.handleClick(p);
			if (result != null) {
				Main.say("link goes to page (" 
						+ result.toPageID + "," + result.toPageIndex + ")");
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				setCursor(Cursor.getDefaultCursor());
			}
		} else {
			if (treePanel.hovering(p)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!storyMode) {
			treePanel.handleDrags(e.getPoint());
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!storyMode) {
			treePanel.handleMouseWheel(e);
		}

	}

	public void setPathsPerPage(Map<Page, List<Path>> pathListPerPage) {
		usedBy = pathListPerPage;
	}

	public List<Path> getPathListPerPage(Page p) {
		return usedBy.get(p);
	}
	
	public Path getCurrentPath() { //made unnecessary when I added getPath method to Iterator class
		return currentPath.path;
	}
	
	public void addPagesPaths(Map<Integer, Path> main_paths, Map<Integer, Page> main_pages){
		paths = main_paths;
		pages = main_pages;
	}

}
