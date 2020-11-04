package io.demo.weichai.req.model;

import java.util.LinkedList;
import java.util.List;

public class Content {
	
	private Document document;
	
	private String text;
	
	private List<Parameter> parameters = new LinkedList<Parameter>();

	public Content(String text) {
		this.text = text;
	}

	public void addParameter(Parameter parameter) {
		parameters.add(parameter);
	}

	@Override
	public String toString() {
		return "Content [" + text + "]";
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public String getText() {
		return text;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}
}
