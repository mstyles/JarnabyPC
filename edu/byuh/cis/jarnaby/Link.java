package edu.byuh.cis.jarnaby;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class Link {
	public Path toPath;
	int toPageID;
	int toPageIndex;
	String text;
	Rectangle2D rect;

	public Link(Path path, int index) {
		toPath = path;
		toPageID = -1;
		toPageIndex = index;
		text = null;
	}
	
	public Link(Path path, Page page) {
		this(path, path.getIndexForPage(page));
	}
	
	public Link(PathIterator pi) {
		this(pi.path, pi.position);
	}
	
	/**
	 * private constructor only to be used by static helper method.
	 * @param pageID
	 * @param path
	 */
	private Link(int pageID, Path path) {
		toPath = path;
		toPageID = pageID;
		toPageIndex = -1;
		text = null;
	}
	
	/**
	 * Helper method for use by the XML parser.
	 * @param path
	 * @param pageID
	 * @return
	 */
	public static Link createPartialLink(Path path, int pageID) {
		return new Link(pageID, path);
	}

	public boolean contains(Point p) {
		return rect.contains(p);
	}

	public boolean resolvePageIndex(Page p) {
		if (toPageIndex != -1) return true;
		else {
			toPageIndex = toPath.getIndexForPage(p);
			return (toPageIndex != -1);
		}
	}
	
	public Page getPage() {
		return toPath.getPage(toPageIndex);
	}
	
	public Path getPath() {
		return toPath;
	}
	
	public void setText(String t) {
		text = t;
	}
	
	/**
	 * Generate xml representation of the link
	 * @return xml string
	 */
	public String toXml() {
		String link_xml = "";
		link_xml += "<link pathref=\""+ toPath.getID() +"\" ";
		link_xml += "pageref=\""+ toPageID +"\" ";
		link_xml += "text=\""+ text +"\">";
		
		link_xml += "<rect x=\""+ rect.getX() +"\" y=\""+ rect.getY() +"\" ";
		link_xml += "width=\""+ rect.getWidth() + "\" height=\""+ rect.getHeight() +"\" />";
		link_xml += "</link>";
		return link_xml;
	}
}
