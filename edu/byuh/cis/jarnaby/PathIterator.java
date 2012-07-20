package edu.byuh.cis.jarnaby;

import java.util.ArrayDeque;

public class PathIterator {
	Path path;
	int position;
	//private ArrayDeque<Link> trail;

	public PathIterator() {
		path = null;
		position = 0;
		//trail = new ArrayDeque<Link>();
	}
	
	public PathIterator(Path p, int pos) {
		this();
		set(p, pos);
	}

	public Page set(Path p, int pos) {
//		path = p;
//		position= pos;
//		return path.getPage(pos);
		return go(new Link(p, pos));
	}
	
	public Page go(Link link) {
		path = link.getPath();
		position = link.toPageIndex;
		//trail.push(link);
		return path.getPage(position);
	}

	public Page next() {
		if (position < path.length()-1) {
			position++;
			//trail.push(new Link(path, position));
			return path.getPage(position);
		}
		return null;
	}

	public Page prev() {
		if (position > 0) {
			position--;
			return path.getPage(position);
		}
//		if (trail.size() > 1) {
//			Link foo = trail.pop();
//			Link prev = trail.peek();
//			path = prev.toPath;
//			position = prev.toPageIndex;
//			Main.say("Going to page " + position + " of path " + path.toString());
//			return path.getPage(position);
//		}
		return null;
	}
	
	public Path getPath(){
		return path;
	}
	
	public int getPos(){
		return position;
	}
	
}
