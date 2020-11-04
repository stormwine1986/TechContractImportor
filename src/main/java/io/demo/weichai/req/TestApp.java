package io.demo.weichai.req;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;

public class TestApp {
	
	public static void main(String[] args) throws IOException, XmlException {
		System.setProperty("MKSSI_NISSUE", "1");
		System.setProperty("MKSSI_ISSUE0", "3");
		PluginMain.run(args);
	}
}
