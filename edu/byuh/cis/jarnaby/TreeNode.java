package edu.byuh.cis.jarnaby;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class TreeNode implements Drawable {
	private Page page;
	private ImageIcon thumbnail;
	private static ImageIcon mysteryThumbnail;
	//private Point pos;
	private Point bottomCenter;
	private Point topCenter;
	private static final int BUFFER = 10;
	//private List<TreeNode> edges;
	private Rectangle boundingBox;
	//private boolean isLinkDestination;
	
	private Path newPath;	

	static {
		try {
		BufferedImage bi = ImageIO.read(new File("page_visit.png"));
        Image img = bi.getScaledInstance(Config.thumbnailWidth,
                Config.thumbnailHeight, Image.SCALE_SMOOTH);
		mysteryThumbnail = new ImageIcon(img);
		} catch (Exception e) {
			//foo
		}
	}
	
	static class LineSegment /*extends Line2D.Float*/ implements Drawable {

//		public LineSegment(Point2D p1, Point2D p2) {
//			super(p1, p2);
//		}
//		public LineSegment(int x1, int y1, int x2, int y2) {
//			super(x1, y1, x2, y2);
//		}
		
		TreeNode top, bottom;
		
		public LineSegment(TreeNode n1, TreeNode n2) {
			top = n1;
			bottom = n2;
		}
		
		@Override
		public void draw(Graphics2D g) {
			//g.draw(this);
			g.setColor(Color.BLACK);
			g.drawLine(top.bottomCenter.x, top.bottomCenter.y,
							bottom.topCenter.x, bottom.topCenter.y);
		}
		
	}


	public TreeNode(Page p) {
		page = p;
		thumbnail = p.getThumbnail();
		//isLinkDestination = false;
		//pos = new Point();
		topCenter = new Point();
		bottomCenter = new Point();
		//edges = new ArrayList<TreeNode>();
		boundingBox = new Rectangle(0, 0, Config.thumbnailWidth, Config.thumbnailHeight);
	}
	
	public TreeNode(Page p, Path path) {
		page = p;
		thumbnail = p.getThumbnail();
		//isLinkDestination = false;
		//pos = new Point();
		topCenter = new Point();
		bottomCenter = new Point();
		//edges = new ArrayList<TreeNode>();
		boundingBox = new Rectangle(0, 0, Config.thumbnailWidth, Config.thumbnailHeight);
		newPath = path;
	}

	//	public void draw(Graphics2D g, boolean highlighted) {
	//		g.setColor(Color.BLACK);
	//		if (page.isVisited()) {
	//			thumbnail.paintIcon(null, g, pos.x, pos.y);
	//
	//			//draw edges connecting to other nodes?
	//			for (TreeNode neighbor : edges) {
	//				g.drawLine(bottomCenter.x, bottomCenter.y,
	//						neighbor.topCenter.x, neighbor.topCenter.y);
	//			}
	//		}
	//
	//
	//		if (isVisited() || isLinkDestination) {
	//			g.drawRect(pos.x, pos.y, Config.thumbnailWidth, Config.thumbnailHeight);
	//		}
	//
	//		if (highlighted) {
	//			Stroke regular = g.getStroke();
	//			g.setStroke(Config.thickLine);
	//			g.setColor(Color.YELLOW);
	//			g.drawRect(pos.x, pos.y, Config.thumbnailWidth, Config.thumbnailHeight);
	//			g.setStroke(regular);
	//		}
	//	}

	public void draw(Graphics2D g/*, boolean highlighted*/) {
		g.setColor(Color.BLACK);
		if (page.isVisited()) {
			thumbnail.paintIcon(null, g, boundingBox.x, boundingBox.y);
		} else {
			mysteryThumbnail.paintIcon(null, g, boundingBox.x, boundingBox.y);
		}
		//g.drawRect(pos.x, pos.y, Config.thumbnailWidth, Config.thumbnailHeight);
		g.draw(boundingBox);

		//draw edges connecting to other nodes?
//		for (TreeNode neighbor : edges) {
//			g.drawLine(bottomCenter.x, bottomCenter.y,
//					neighbor.topCenter.x, neighbor.topCenter.y);
//		}

		/*if (highlighted) {
			Stroke regular = g.getStroke();
			g.setStroke(Config.thickLine);
			g.setColor(Color.YELLOW);
			g.drawRect(pos.x, pos.y, Config.thumbnailWidth, Config.thumbnailHeight);
			g.setStroke(regular);
		}*/
	}
	
	public void drawHighlight(Graphics2D g) {
		Stroke regular = g.getStroke();
		g.setStroke(Config.thickLine);
		g.setColor(Color.YELLOW);
		g.draw(boundingBox);
		g.setStroke(regular);
	}

	public void setX(int x) {
		setPosition(x, boundingBox.y);
	}

	public void setY(int y) {
		setPosition(boundingBox.x, y);
	}

	public void setPosition(int x, int y) {
		//pos.setLocation(x, y);
		topCenter.x = x + Config.thumbnailWidth/2;
		bottomCenter.x = topCenter.x;
		topCenter.y = y;
		bottomCenter.y = y + Config.thumbnailHeight;
		boundingBox.setLocation(x, y);
	}

	public void setBeneath(TreeNode parent) {
		if (Config.orientation == Config.Orientation.PORTRAIT) {
			this.setPosition(parent.boundingBox.x,
					parent.boundingBox.y + Config.thumbnailHeight+BUFFER);
		} else {
			//TODO
		}
		//parent.edges.add(this);
		// new LineSegment(parent.bottomCenter.x, parent.bottomCenter.y,
		//						this.topCenter.x, this.topCenter.y);
	}

	public void setAboveAndOffset(TreeNode parent, int x) {
		if (Config.orientation == Config.Orientation.PORTRAIT) {
			this.setPosition(x,
					parent.boundingBox.y - (Config.thumbnailHeight+BUFFER));
		} else {
			//TODO
		}
		//parent.edges.add(this);
		//return new LineSegment(parent.bottomCenter.x, parent.bottomCenter.y,
		//		this.topCenter.x, this.topCenter.y);
	}

	public List<Link> getLinks() {
		return page.getLinks();
	}

	public boolean isVisited() {
		return page.isVisited();
	}
	//	
	//	public void setVisited() {
	//		visited = true;
	//	}

	public Page getPage() {
		return page;
	}

	public boolean contains(Point2D p) {
		return boundingBox.contains(p);
	}

//	public void setAsLinkDestination() {
//		isLinkDestination = true;
//	}

	public int getY() {
		return boundingBox.y;
	}
	
	public Path getNewPath(){
		return newPath;
	}
	
	public boolean hasPath(){
		if(newPath != null) return true;
		return false;
	}
	
	public void addPath(Path p){
		newPath = p;
	}
}
