package io.demo.weichai.req.model;

public class ControlWord {
	
	public static boolean isIgnoreOn(String text) {
		return "<忽略>".equals(text);
	}
	
	public static boolean isIgnoreOff(String text) {
		return "</忽略>".equals(text);
	}

	public static boolean isOrignFormatOutputOn(String text) {
		return "<原格式输出>".equals(text);
	}

	public static boolean isOrignFormatOutputOff(String text) {
		return "</原格式输出>".equals(text);
	}

	public static boolean isSplitByRowOn(String text) {
		return "<按行拆分>".equals(text);
	}

	public static boolean isSplitByRowOff(String text) {
		return "</按行拆分>".equals(text);
	}

}
