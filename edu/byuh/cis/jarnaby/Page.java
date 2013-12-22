package edu.byuh.cis.jarnaby;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Page {
	private List<Link> links;
	private ImageIcon background;
	private StoryText story_text;
	int id;
	String pictID;//for convenience of XML parser; unused otherwise
	private boolean visited;
	private Link backlink;

	public Page(int id) {
		this.id = id;
		story_text = new StoryText();
		links = new ArrayList<Link>();
		visited = false;
		backlink = null;
	}

//	public Page(int id, ImageIcon bg) {
//		this(id);
//	}

	public Page(int id, String pictID) {
		this(id);
		this.pictID = pictID;
	}

	public void draw(Graphics2D g, boolean thumbnail) {
		background.paintIcon(null, g, 0, 0);
		if (!thumbnail) {
			story_text.setLinks(links);
			story_text.draw(g);

			//draw the dogear if there's a link on this page
			//(and if we're not already on the link's path!)
//			if (links.size() > 0 && links.get(0).getPath() != Main.getCurrentPath()) {
//				g.setColor(Color.RED);
//				g.draw(Config.dogEar);
//				g.drawLine(Config.dogEar.x, Config.dogEar.y,
//						Config.dogEar.x + Config.dogEar.width, Config.dogEar.height);
//			}
		}
	}
	
	/**
	 * Generate xml representation of the page
	 * @return xml string
	 */
	public String toXml() {
		String page_xml = "";
		page_xml += "<page id=\""+ id +"\" image_id=\""+ pictID +"\">";
		for (String text : story_text.getTexts()) {
			page_xml += "<text>"+text+"</text>";
		}
		for (Link link : links) {
			page_xml += link.toXml();
		}
		page_xml += "</page>";
		return page_xml;
	}
	
	public void draw(Graphics2D g) {
		draw(g, false);
	}

	public ImageIcon getThumbnail() {
		BufferedImage bi = new BufferedImage(Config.X_RESN,
				Config.Y_RESN, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		draw(g, true);
		g.dispose();
        Image img = bi.getScaledInstance(Config.thumbnailWidth,
                Config.thumbnailHeight, Image.SCALE_SMOOTH);
		return new ImageIcon(img);
	}

	public void setImage(ImageIcon ic) {
		background = ic;
	}

//	public void setText(StoryText st) {
//	text = st;
//	}

	public void addText(String s) {
		story_text.addText(s);
	}
	
	public StoryText getStoryText() {
		return story_text;
	}

	public void addLink(Link ln) {
		links.add(ln);
	}

	public List<Link> getLinks() {
		return links;
	}
	
	public boolean hasLink(){
		if(links.isEmpty()) return false;
		return true;
	}
	
	public int getID(){
		return id;
	}

	public Link handleClick(Point p) {
		Link link = null;
		//check for links in the picture
		for (Link ln : links) {
			if (ln.contains(p) && ln.getPath() != Main.getCurrentPath()) {
				link = ln;
				break;
			}
		}
		//check for links in the text
		if (link == null) {
			link = story_text.handleClick(p);
		}
		//check for links in the dogear
//		if (linq == null) {
//			if (links.size() > 0) {
//				if (Config.dogEar.contains(p)) {
//					linq = links.get(0);
//				}
//			}
//		}
		
		return link;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public Link getBacklink() {
		return backlink;
	}

	public void setBacklink(Link backlink) {
		this.backlink = backlink;
	}
	
	public String getPictID(){
		return pictID;
	}

}
