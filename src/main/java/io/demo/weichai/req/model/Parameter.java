package io.demo.weichai.req.model;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Parameter {
	
	private String name;
	
	private List<String> values = new LinkedList<String>();

	public Parameter(String name) {
		this.name = name;
	}

	public void appendValue(String text) {
		values.add(text);
	}

	@Override
	public String toString() {
		return "Parameter [" + name + "] values [" + values + "]";
	}

	public String getName() {
		return name;
	}

	public String getValues() {
		if(values.size() == 0) {
			return "";
		}
		return values.stream().collect(Collectors.joining("|"));
	}
}
