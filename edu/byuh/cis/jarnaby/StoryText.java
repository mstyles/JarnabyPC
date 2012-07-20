package edu.byuh.cis.jarnaby;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StoryText {

	List<String> texts;
	List<Link> links;
	Map<Rectangle, Link> linkRects;
	private static final Color background = new Color(255,253,208);

	public StoryText() {
		texts = new ArrayList<String>();
		linkRects = new HashMap<Rectangle, Link>();
	}

	public StoryText(String t) {
		this();
		addText(t);
	}

	public void addText(String t) {
		texts.add(t);
	}
	
	public void setLinks(List<Link> linx) {
		links = linx;
	}

	public void draw(Graphics2D g) {		
		int buf = 10;
		int fontHeight = g.getFontMetrics().getAscent();
		int textHeight = fontHeight * texts.size()+buf;
		int textWidth = 0;
		final int SPACE_BETWEEN_WORDS = 4;
		for (String s : texts) {
			textWidth = Math.min(Config.Y_RESN-buf*4,
				Math.max(g.getFontMetrics().stringWidth(s), textWidth));
		}
		for (String s : texts) {
			//Main.say("stringWidth: " + g.getFontMetrics().stringWidth(s)  + "; textWidth: " + textWidth + " div: " + (g.getFontMetrics().stringWidth(s) / textWidth));
			if (g.getFontMetrics().stringWidth(s) > textWidth) {
				textHeight += ((g.getFontMetrics().stringWidth(s) / textWidth) * fontHeight);
			}
		}
		int rectangleWidth = Config.X_RESN;//textWidth + buf*2;
		int rectX = 0;//(totalWidth-rectangleWidth)/2;
		int textStart = rectX + buf;
		int x;
		int y = Config.Y_RESN;//(totalHeight-100-textHeight);
		int rectHeight = 70;
		Rectangle r = new Rectangle(rectX,y,rectangleWidth, rectHeight);
		g.setColor(background);
		g.fill(r);
		g.setColor(Config.textColor);
		g.draw(r);
		for (int i=0; i<texts.size(); ++i) {
			x = textStart;
			//Main.say("drawing " + texts.get(i) + " at " + (x+buf) + ", " + (y+fontHeight*(i+1)));

			Scanner s = new Scanner(texts.get(i));

			while (s.hasNext()) {
				String token = s.next();
				int width = g.getFontMetrics().stringWidth(token);
				if ((x+width) > textStart+textWidth) {
					x = textStart;
					y += fontHeight;
				}
				Link link = getLinkForWord(token);
				if (link != null && link.getPath() != Main.getCurrentPath()) {
					//render linked words with blue/underline
					//as long as we're not on the path being linked to
					linkRects.put(new Rectangle(x, y+fontHeight*i, width, fontHeight), link);
					Main.say("LinkRect at " + (new Rectangle(x,y+fontHeight*i,width,fontHeight)));
					g.setColor(Config.linkColor);
					g.drawString(token, x, y+fontHeight*(i+1));
					g.drawLine(x, y+fontHeight*(i+1)+1, x+width, y+fontHeight*(i+1)+1);
					g.setColor(Config.textColor);
				} else {
					g.drawString(token, x, y+fontHeight*(i+1));
				}
				x += (width + SPACE_BETWEEN_WORDS);
			}
		}

	}

	private Link getLinkForWord(String token) {
		Link result = null;
		for (Link ln : links) {
			if (ln.text != null && 
					(ln.text.equalsIgnoreCase(token) || token.contains(ln.text))) {
				result = ln;
				break;
			}
		}
		return result;
	}

	public Link handleClick(Point p) {
		//Main.say("clicked inside text! at " + p);
		for  (Rectangle r : linkRects.keySet()) {
			if (r.contains(p)) {
				//Main.say("clicked on link!");
				return linkRects.get(r);
			}
		}
		return null;
	}
}
