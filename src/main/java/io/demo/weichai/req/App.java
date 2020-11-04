package io.demo.weichai.req;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.IntegrationPoint;
import com.mks.api.IntegrationPointFactory;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;

import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import io.demo.weichai.req.model.Content;
import io.demo.weichai.req.model.ControlWord;
import io.demo.weichai.req.model.Document;
import io.demo.weichai.req.model.Parameter;

/**
 * Hello world!
 *
 */
public class App {
	
	protected static String stauts = "Resolving";
	protected static int imported = 0;
	private static boolean ignoreOn = false;
	private static boolean orginFormatOutputOn = false;
	private static boolean splitByRowOn = false;
	private static CmdRunner runner;
	private static org.jsoup.nodes.Document xhtml;
	
    protected static void setRunner(CmdRunner runner) {
		App.runner = runner;
	}

	public static void run(File flie, String documentId)
    {
    	InputStream is = null;
    	XWPFDocument doc = null;
    	try{
    		is = new FileInputStream(flie);
    		doc = new XWPFDocument(is); 
    		// 解析技术协议文档
    		List<IBodyElement> elements = doc.getBodyElements();
    		DocumentBuilder.init();
    		int tNum = 0;
    		for(IBodyElement element: elements) {
    			BodyElementType elementType = element.getElementType();
    			if(elementType == BodyElementType.PARAGRAPH) {
    				buildParagraph(element);
    			} else if(elementType == BodyElementType.TABLE) {
    				tNum++;
    				buildTable(tNum, element);
    			}
    		}
    		// 全文档解析完成，导入到ILM系统
    		Document document = DocumentBuilder.getDocument();
    		document.setId(documentId);
    		stauts = "Importing";
    		//importDocument(document);
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlException e) {
			e.printStackTrace();
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if(doc != null) {
				try {
					doc.close();
				} catch (IOException e) {
				}
			}
			stauts = "Completed";
		}
    }

	private static void importDocument(Document document) {
		IntegrationPoint point = null;
		try {
			point = IntegrationPointFactory.getInstance().createLocalIntegrationPoint(4, 16);
			Session session = point.getCommonSession();
			runner = session.createCmdRunner();
			List<Content> contents = document.getContents();
			for(Content content: contents) {
				importContent(content);
			}
		} catch (APIException e) {
			e.printStackTrace();
		} finally {
			if(point != null) {
				point.release();
			}
		}
	}

	protected static void importContent(Content content) throws APIException {
		List<String> ids = new LinkedList<String>();
		List<Parameter> parameters = content.getParameters();
		for(Parameter parameter :parameters) {
			ids.add(importParameter(parameter));
		}
		// 新建Content
		Command command = new Command("im", "createcontent");
		command.addOption(new Option("parentID", content.getDocument().getId()));
		command.addOption(new Option("type", "Requirement"));
		command.addOption(new Option("field", "Shared Category=技术需求"));
		String text = content.getText();
		command.addOption(new Option("field", String.format("Text=%s", text)));
		final MultiValue mv = new MultiValue(",");
		ids.stream().forEach(id -> {mv.add(id);});
		command.addOption(new Option("field", String.format("Parameter List=%s", mv.toString())));
		Response response = runner.execute(command);
		System.out.println(response);
		// 更新进度
		imported++;
	}

	protected static String importParameter(Parameter parameter) throws APIException {
		Command cmd = new Command("im", "createissue");
		cmd.addOption(new Option("type", "Parameter"));
		String name = parameter.getName();
		String values = parameter.getValues();
		cmd.addOption(new Option("field", String.format("Parameter Name=%s", name)));
		cmd.addOption(new Option("field", String.format("Parameter Value=%s", values)));
		Response response = runner.execute(cmd);
		System.out.println(response);
		return response.getResult().getField("resultant").getItem().getId();
	}

	private static void buildTable(int tNum, IBodyElement element) throws IOException, XmlException {
		XWPFTable table = (XWPFTable) element;
		if(!ignoreOn) {
			if(orginFormatOutputOn) {
				// 启用原格式输出
				String xhtml = convertToXHTML(tNum, table);
				DocumentBuilder.addContent(new Content("<!-- MKS HTML -->" + xhtml));
				return;
			}
			if(splitByRowOn) {
				List<Content> contents = splitByRow(tNum, table);
				DocumentBuilder.addContents(contents);
				return;
			}
			System.out.println("+ "+ tNum + " --------------------------------------------------------------------------------");
			System.out.println(table.getText());
			System.out.println("---------------------------------------------------------------------------------");
			List<XWPFTableRow> rows = table.getRows();
    		int i = 1;
    		for(XWPFTableRow row :rows) {
    			List<XWPFTableCell> cells = row.getTableCells();
    			int j = 1;
    			for(XWPFTableCell cell : cells) {
    				// cell.getParagraphs().get(0).getRuns().get(1).getEmbeddedPictures().get(0).getPictureData().getData();
    				// cell.getParagraphs().get(0).getRuns().get(1).getEmbeddedPictures().get(0).getPictureData().getFileName();
    				String text = cell.getText().trim();
    				if(!"".equals(text)) {
    					if(Dictionary.isParameter(text)) {
    						// 处理参数
    						boolean success = DocumentBuilder.addParameter(new Parameter(text));
    						if(!success) {
    							System.out.println(
    									String.format("没有找到合适的Content上下文，tNum = %s, [%s:%s] text = %s", 
    											tNum, i, j, text));
    						}
    					} else {
    						if(DocumentBuilder.hasParameterContext()) {
    							DocumentBuilder.addParameterValue(text);    							
    						} else {
    							DocumentBuilder.addContent(new Content(text));
    						}
    					}				
    				}
    				j++;
    			}
    			// 一行处理完成
    			DocumentBuilder.resetParameterContext();
    			i++;
    		}
		}
	}

	private static List<Content> splitByRow(int num, XWPFTable table) throws IOException {
		if(xhtml == null) {
			initXHTML(table);
		}
		Elements elements = xhtml.select("table");
		Element tableTag = elements.get(num - 1);
		String style = tableTag.attr("style");
		Elements trTags = tableTag.getElementsByTag("tr");
		List<Content> contents = new LinkedList<>();
		List<Element> group = new LinkedList<>();
		int count = 1;
		for(int i = 0; i < trTags.size(); i++) {
			Element tr = trTags.get(i);
			if(count == 1 && hasRowspan(tr)) {
				count = getRowspan(tr);				
			}
			group.add(tr);
			count--;
			if(count == 0) {
				contents.add(generateContent(group, style));
				group.clear();
				count = 1;
			}
		}
		return contents;
	}

	protected static String convertToXHTML(int num, XWPFTable table) throws IOException {
		if(xhtml == null) {
			initXHTML(table);
		}
		Elements elements = xhtml.select("table");
		Element element = elements.get(num - 1);
		return element.toString();
	}

	private static void initXHTML(XWPFTable table) throws IOException {
		ByteArrayOutputStream baos = null;
		try {
			XWPFDocument document = table.getBody().getXWPFDocument();
			baos = new ByteArrayOutputStream();
			XHTMLOptions options = XHTMLOptions.create();
			XHTMLConverter.getInstance().convert(document, baos, options);
			xhtml = Jsoup.parse(baos.toString());			
		} finally {
			if(baos != null) {
				baos.close();
			}
		}
	}

	private static void buildParagraph(IBodyElement element) {
		XWPFParagraph p = (XWPFParagraph) element;
		String text = p.getText().trim();
		if(!"".equals(text)) {
			System.out.println(text);
			if(ControlWord.isIgnoreOn(text)) {
				ignoreOn = true;
				return;
			} else if(ControlWord.isIgnoreOff(text)) {
				ignoreOn = false;
				return;
			} else if(ControlWord.isOrignFormatOutputOn(text)) {
				orginFormatOutputOn = true;
				return;
			} if(ControlWord.isOrignFormatOutputOff(text)) {
				orginFormatOutputOn = false;
				return;
			} if(ControlWord.isSplitByRowOn(text)) {
				splitByRowOn = true;
				return;
			} if(ControlWord.isSplitByRowOff(text)) {
				splitByRowOn = false;
				return;
			} else {
				if(!ignoreOn) {
					DocumentBuilder.addContent(new Content(text));					
				}
			}
		}
	}
	
	private static boolean hasRowspan(Element tr) {
		return tr.child(0).hasAttr("rowspan");
	}

	private static Content generateContent(List<Element> group, String style) {
		StringBuffer buffer = new StringBuffer();
		for(Element tr :group) {
			buffer.append(tr.toString());
		}
		String text = String.format("<table style=\"%s\"><tbody>%s</tbody></table>", style, buffer.toString());
		
		System.out.println(text);
		System.out.println("===============================");

		return new Content("<!-- MKS HTML -->" + text);
	}

	private static int getRowspan(Element tr) {
		int rowScope = 1;
		String rowspan = tr.child(0).attr("rowspan");
		if(!"".equals(rowspan)) {
			rowScope = Integer.valueOf(rowspan);
		}
		return rowScope;
	}
	
	public static void main(String[] args) {
		App.run(new File("Z:\\faw\\应用开发类-技术协议.docx"), "3");
	}
}
