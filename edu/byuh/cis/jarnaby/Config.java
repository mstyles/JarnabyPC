package edu.byuh.cis.jarnaby;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;

public class Config {
	public static final int X_RESN = 320;
	public static final int Y_RESN = 350;
	public static final int thumbnailWidth = 64;
	public static final int thumbnailHeight = 70;
	public static String storyPrefix = "lrrh";
	public static Orientation orientation = Orientation.PORTRAIT;
	public static Stroke thickLine = new BasicStroke(2);
	public static Color textColor = Color.BLACK;
	public static Color linkColor = Color.BLUE;
	public static Rectangle dogEar = new Rectangle(Config.X_RESN-30, 0, 30, 30);
	
	public static String getStoryPath() {
		return "stories/" + storyPrefix + "/";
	}
	
	public static String getImagePath() {
		return getStoryPath() + "images/";
	}
	
	public enum Orientation {
		PORTRAIT,
		LANDSCAPE
	}

}
