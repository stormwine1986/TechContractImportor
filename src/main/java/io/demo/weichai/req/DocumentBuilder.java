package io.demo.weichai.req;

import java.util.List;

import io.demo.weichai.req.model.Content;
import io.demo.weichai.req.model.Document;
import io.demo.weichai.req.model.Parameter;

public class DocumentBuilder {

	private static Document doc;
	
	private static Content cContext;
	
	private static Parameter pContext;
	
	public static void init() {
		doc = new Document();
		cContext = null;
		pContext = null;
	}

	public static void addContent(Content content) {
		doc.addContent(content);
		cContext = content;
		pContext = null;
	}

	public static boolean addParameter(Parameter parameter) {
		if(cContext == null) return false;
		cContext.addParameter(parameter);
		pContext = parameter;
		return true;
	}

	public static boolean addParameterValue(String text) {
		if(pContext == null) return false;
		pContext.appendValue(text);
		return true;
	}

	public static void resetParameterContext() {
		pContext = null;
	}

	public static Document getDocument() {
		return doc;
	}

	public static boolean hasParameterContext() {
		return pContext != null;
	}

	public static void addContents(List<Content> contents) {
		for(Content content :contents) {
			addContent(content);
		}
	}

	public static int getDocumentSize() {
		return doc == null?0:doc.getContents().size();
	}
}
