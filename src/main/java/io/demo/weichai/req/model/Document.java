package io.demo.weichai.req.model;

import java.util.LinkedList;
import java.util.List;

public class Document {
	
	private String id;
	
	private List<Content> contents = new LinkedList<Content>();

	public void addContent(Content content) {
		contents.add(content);
		content.setDocument(this);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Content> getContents() {
		return contents;
	}
}
