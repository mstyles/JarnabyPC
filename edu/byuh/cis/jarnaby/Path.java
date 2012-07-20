package edu.byuh.cis.jarnaby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Path {
	private List<Page> pages;
	//private Map<Integer, Page> pageMap; 
	List<Integer> placeholders; //for XML parser
	private int id;
	private String name;
	private boolean defaultPath;

	public Path() {
		pages = new ArrayList<Page>();
		//pageMap = new HashMap<Integer, Page>();
		placeholders = new ArrayList<Integer>();
		defaultPath = false;
	}

	public Path(String name, int id) {
		this();
		this.name = name;
		this.id = id;
	}

	public void addPage(int i, Page p) {
		//pageMap.put(i, p);
		pages.add(p);
	}

	public Page getPage(int i) {
		Page p = null;

		try {
			p = pages.get(i);
		} catch (IndexOutOfBoundsException e) {
			//do nothing; return null.
		}
		return p;
	}

	public int length() {
		return pages.size();
	}

//	public void resolveLinks() {
//		for (Page p : pages) {
//			for (Link ln : p.getLinks()) {
//				for (int i=0; i<placeholders.size(); ++i) {
//					if (ln.toPageID == placeholders.get(i)) {
//						ln.toPageIndex = i;
//					}
//				}
//			}
//		}
//	}

	//	public int getFirstPage() {
	//		//return firstPage;
	//	}

	@Override
	public String toString() {
		return name;
	}

	public int getIndexForPage(Page p) {
		for (int i=0; i<pages.size(); ++i) {
			if (pages.get(i) == p) {
				return i;
			}
		}
		return -1;
	}

	public void makeDefault() {
		defaultPath = true;	
	}

	public boolean isDefault() {
		return defaultPath;
	}

	public List<Page> getPages() {
		return pages;
	}

	public boolean contains(Page page) {
		return pages.contains(page);
	}

	public int getID(){
		return id;
	}

}
