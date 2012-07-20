package edu.byuh.cis.jarnaby;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import edu.byuh.cis.jarnaby.Config.Orientation;
import edu.byuh.cis.jarnaby.TreeNode.LineSegment;

/**
 * Acknowledgement: The dragging/scaling code used here is based on the
 * AffineTransformTest.java demo that I downloaded from... somewhere...
 * a few years ago.
 * 
 *
 */
public class TreePanel {
	private List<Path> paths;
	//private Path defaultPath;//, currentPath;
	private Map<Page, TreeNode> nodes;
	private Map<Path, Integer> pathX;
	private List<Drawable> scenegraph;
	private static final int TOP_BUFFER = 80;
	public static final int PATH_BUFFER = (int)(Config.thumbnailWidth * 1.5);
	private boolean droit = true;	//helper variable for layout algorithm
	//private int droitCount = 1;		//helper variable for layout algorithm
	private double translateX;
	private double translateY;
	private double scale;
	private Canvas canvas;
	private Paint backgroundTexture;
	private AffineTransform tx;
	private int width, height;
	private boolean dirty;
	private int timerCounter;//for animated transitions
	private Timer tim;//for animated transitions
	private boolean animating;//for animated transitions
	private Composite originalComposite, composite; //for animated transitions
	private double alpha;//for animated;
	private Path toPath;//for animated;
	private int transitionalY;

	private int globalX;
	private int globalX2;

	public static Orientation orientation;

	public TreePanel(Canvas canvas, Map<Integer, Path> trax, Collection<Page> pages) {
		this.canvas = canvas;
		dirty = true;
		animating = false;
		transitionalY = TOP_BUFFER;
		pathX = new HashMap<Path, Integer>();
		paths = new ArrayList<Path>(trax.values());
		scenegraph = new ArrayList<Drawable>();
		//		for (Path p : paths) {
		//			if (p.isDefault()) {
		//				defaultPath = p;
		//				break;
		//			}
		//		}

		nodes = new HashMap<Page, TreeNode>();
		for (Page p : pages) {
			nodes.put(p, new TreeNode(p));
		}

		try {
			BufferedImage bi = ImageIO.read(new File("bkgd_texture.jpg"));
			backgroundTexture = new TexturePaint(bi, new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
		} catch (Exception e) {
			backgroundTexture = Color.WHITE;
		}

		resetTransformations();
	}

	private void layout() {
		dirty = false;
		scenegraph.clear();
		//		Main.say("inside layout!");
		width = canvas.getWidth();
		height = canvas.getHeight();
		Integer x, y;
		y = transitionalY;
		x = (width-Config.thumbnailWidth)/2;
		globalX = (width-Config.thumbnailWidth)/2;
		int x2 = x + Config.thumbnailWidth + 10;
		globalX2 = x + Config.thumbnailWidth + 10;
		int x3 = x - Config.thumbnailWidth - 10;
		Path currentPath = Main.getCurrentPath();
		Page root = currentPath.getPage(0);
		TreeNode parent = nodes.get(root);
		parent.setPosition(x, y);
		scenegraph.add(parent);
		if (root.getBacklink() != null) { //REMOVED temporarily to simplify troubleshooting
			Link backLink = root.getBacklink();
			Page backPage = backLink.getPage();
			TreeNode backNode = nodes.get(backPage);
			backNode.addPath(backLink.getPath());
			backNode.setAboveAndOffset(parent, x3);
			scenegraph.add(backNode);
			scenegraph.add(new LineSegment(backNode, parent));
		}
		for (int i=1; i<currentPath.length(); ++i) {
			Page page = currentPath.getPage(i);
			TreeNode node = nodes.get(page);
//			Main.say("holding "+ node.getPage().getPictID());
			node.setBeneath(parent);
			if(node.isVisited() || parent.isVisited()){
//				Main.say("adding to map: "+ node.getPage().getPictID());
				scenegraph.add(node);
				scenegraph.add(new LineSegment(parent, node));

				//			System.out.print("page: "+parent.getPage().getID());
				//			if(parent.getPage().getLinks().isEmpty()){
				//				System.out.println(" has NO links");
				//			}
				//			else System.out.println(" HAS links");

				for (Link ln : parent.getLinks()) {
					if(ln.getPath() != currentPath){
						//					Main.say("Links to "+ ln.toPageID);
						Page linkPage = ln.getPage();
						TreeNode linkNode = new TreeNode(linkPage, ln.getPath());
						nodes.put(linkPage, linkNode);
						//						Main.say(linkNode.getPage().pictID + " has been added to tree");
						linkNode.setBeneath(parent); //if two links point to same page, it's treeNode is re-used and consequently re-set as well
						linkNode.setX(x2);
						scenegraph.add(linkNode);
						scenegraph.add(new LineSegment(parent, linkNode));
					}
					//				else Main.say("Page "+parent.getPage().getID() +" links to same path");
				}
			}
//			else Main.say(parent.getPage().getPictID()+" is UNVISITED!!!");
			parent = node;

		}
		//put the current node in the center of the screen.
		TreeNode currentNode = nodes.get(Main.getCurrentPage());
//		Main.say("Y at: "+ currentNode.getY());
//		Main.say("Window: "+ Main.getWindowHeight());
//		Main.say("Thumb: "+ Config.thumbnailHeight);
		translateY -= (currentNode.getY() - Main.getWindowHeight()/2 + Config.thumbnailHeight);
		//		for(TreeNode node : nodes.values()){
		//			Main.say("I am " + node.getPage().pictID);
		//		}
	}

	public void draw(Graphics2D g) {
		if (dirty) {
			layout();
		}
		if (composite == null) {
			originalComposite = g.getComposite();
			composite = originalComposite;
		}
		tx = new AffineTransform();
		tx.translate(width/2, height/2);
		tx.scale(scale, scale);
		tx.translate(-width/2, -height/2);
		tx.translate(translateX, translateY);
		g.setPaint(backgroundTexture);
		g.fillRect(0,0,width,height);
		g.setTransform(tx);

		if (animating) {
			g.setComposite(composite);
		} else {
			g.setComposite(originalComposite);
		}

		for (Drawable n : scenegraph) {
			n.draw(g);
		}

		TreeNode currentNode = nodes.get(Main.getCurrentPage());
		currentNode.drawHighlight(g);

		//		for (Page p : Main.getCurrentPath().getPages()) {
		//			TreeNode node = nodes.get(p);
		//			node.draw(g, isCurrent(node));
		//			for (Link ln : node.getLinks()) {
		//				Path linkPath = ln.toPath;
		//				Page linkPage = linkPath.getPage(ln.toPageIndex);
		//				TreeNode linkNode = nodes.get(linkPage);
		//				if (animating && linkPath == toPath) {
		//					g.setComposite(originalComposite);
		//				}
		//				linkNode.draw(g, false);
		//			}
		//
		//		}
		//Main.say("scale: " + scale + ", dx=" + translateX + ", dy=" + translateY);
	}

	private static boolean isCurrent(TreeNode node) {
		return node.getPage() == Main.getCurrentPage();
	}

	int lastOffsetX, lastOffsetY;
	public void handleMousePressed(Point p) {
		lastOffsetX = p.x;
		lastOffsetY = p.y;
	}

	public void handleDrags(Point p) {
		//		new x and y are defined by current mouse location subtracted
		//		by previously processed mouse location
		int newX = p.x - lastOffsetX;
		int newY = p.y - lastOffsetY;

		//		increment last offset to last processed by drag event.
		lastOffsetX += newX;
		lastOffsetY += newY;

		//		update the canvas locations
		translateX += newX;
		translateY += newY;

		//		schedule a repaint.
		canvas.repaint();
	}

	public void handleMouseWheel(MouseWheelEvent e) {
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {

			//			make it a reasonable amount of zoom
			//			.1 gives a nice slow transition
			scale += (.1 * e.getWheelRotation());

			//			also, setting scale to 0 has bad effects
			scale = Math.max(0.00001, scale); 
			canvas.repaint();
		}
	}

	public void resetTransformations() {
		translateX = 0;
		translateY = 0;
		scale = 1;
		dirty = true;
	}

	public void setBackgroundTexture(Paint img) {
		backgroundTexture = img;
	}

	public boolean hovering(Point p) {
		//System.out.print (p.x + "," + p.y + " : ");
		Point2D transformed;
		try {
			transformed = tx.inverseTransform(p, null);
			//			for (TreeNode node : nodes.values()) {
			//				if (node.contains(transformed)){
			//					return true;
			//				}
			//			}

			for (Drawable d : scenegraph) {
				if (d instanceof TreeNode){
					TreeNode node = (TreeNode)d;
					if (node.contains(transformed)){
						//						Main.say("inside node " + node.getPage().pictID);
//						if(node.getPage().isVisited())Main.say("Visited");
//						else Main.say("NOT Visited");
						return true;
					}
				}
			}

		} catch (NoninvertibleTransformException e) {
			Main.say("You can't invert this transform!");
		} 
		return false;
	}

	public void handleClick(Point p) {
		//System.out.print (p.x + "," + p.y + " : ");
		Point2D transformed;
		boolean changingPaths = false;
		try {
			transformed = tx.inverseTransform(p, null);
			for (Drawable d : scenegraph){
				if(d instanceof TreeNode){
					TreeNode node = (TreeNode)d;
					if (node.contains(transformed)){
						Main.say("inside node " + node.getPage().pictID);
						Page toPage = node.getPage();
						//Path toPath;
						//How to find the new path?
						//Find a list of all the paths that use that page.
						//If the current path is one of them, use it.
						//else, randomly pick one of the other paths.
						List<Path> paths = canvas.getPathListPerPage(toPage);
						if (paths.contains(canvas.getCurrentPath())) {
							toPath = canvas.getCurrentPath();
						} else {
							int randomPath = (int)(Math.random()*paths.size());
//							toPath = paths.get(randomPath);
							toPath = node.getNewPath();
							changingPaths = true;
							transitionalY = node.getY();
//							Main.say("going to Y:"+ transitionalY);
							Main.say("New PATH: "+ toPath);
							animate(toPage);
						}
						//dirty = true;
						//canvas.set(toPath, toPath.getIndexForPage(toPage));
						if (!changingPaths) {
							Main.say("NOT CHANGING");
							dirty = true;
							canvas.set(toPath, toPath.getIndexForPage(toPage));
							transitionalY = TOP_BUFFER;
							canvas.toggleMode();
						} else {
							Main.say("CHANGING");
							dirty = true;
							transitionalY = TOP_BUFFER;
							canvas.toggleMode();
						}
						break;
					}
				}
			}
			//Main.say("not inside node");
		} catch (NoninvertibleTransformException e) {
			Main.say("You can't invert this transform!");
		} 
	}

	private void animate(final Page toPage) {
		//		int fromX = pathX.get(toPath);
		//		int toX = pathX.get(Main.getCurrentPath());
		int fromX = globalX;
		int toX = globalX2;
		int diff = toX - fromX;
		final double steps = 1;
		final double dx = diff/steps;
		timerCounter=0;
		alpha = 1;
		animating = true;
		final double originalTX = translateX;
		tim = new Timer(100, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				translateX += dx;
				alpha = Math.max(0, alpha-(1/steps));
				//				composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha);
				//Main.say("alpha="+alpha);
				timerCounter++;
				if (timerCounter >= steps) {
					tim.stop();
					dirty = true;
					animating = false;
					translateX = originalTX;
					//canvas.set(toPath, toPath.getIndexForPage(toPage));
					canvas.followLink(new Link(toPath, toPage));
				}
				canvas.repaint();
			}

		});
		tim.start();

	}

	//CODE GRAVEYARD

	//	private int getXForPath(Path newPath, Path oldPath) {
	//		Integer X = pathX.get(newPath);
	//		if (X == null) {
	//			X = pathX.get(oldPath);
	//			if (droit) {
	//				X += PATH_BUFFER;
	//			} else {
	//				X -= PATH_BUFFER;
	//			}
	//			droit = !droit;
	//			pathX.put(newPath, X);
	//			Main.say("x pos is unknown for path " + newPath + ". Assigning " + X + "(" + pathX.size() + " elements in pathX)");
	//		}
	//		return X;
	//	}

	private void setupXPath() {
		pathX.clear();
		int x = (canvas.getWidth()-Config.thumbnailWidth)/2;
		pathX.put(canvas.getCurrentPath(), x);
		for (Path p : Main.getPaths()) {
			if (p != canvas.getCurrentPath()) {
				if (droit) {
					pathX.put(p, x+PATH_BUFFER);
				} else {
					pathX.put(p, x-PATH_BUFFER);
				}
			}
		}

	}
	/*
private void layout() {
	width = canvas.getWidth();
	height = canvas.getHeight();
	Integer x, y;
	y = TOP_BUFFER;
	x = pathX.get(defaultPath);
	if (x == null) {
		x = width/2;
	}
	pathX.put(defaultPath, x);
	Page root = defaultPath.getPage(0);
	TreeNode node = nodes.get(root);
	node.setPosition(x, y);
	layout_rec(node, defaultPath, 0);
}

private void layout_rec(TreeNode canvas, Path track, int i) {
	++i;
	Page nextPage = track.getPage(i);
	if (nextPage != null) {
		TreeNode node = nodes.get(nextPage);
		node.setBeneath(canvas);
		layout_rec(node, track, i);
		for (Link ln : canvas.getLinks()) {
			Path linkPath = ln.toPath;
			Page linkPage = linkPath.getPage(ln.toPageIndex);
			TreeNode linkNode = nodes.get(linkPage);
			int x = getXForPath(linkPath, track);
			linkNode.setBeneath(canvas);
			linkNode.setX(x);
			layout_rec(linkNode, linkPath, ln.toPageIndex);
		}
	}
}*/


}
