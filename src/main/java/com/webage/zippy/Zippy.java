package com.webage.zippy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Zippy {
    static JexlEngine jexl = new JexlBuilder().create();
    static Pattern pattern = Pattern.compile("\\{\\{([^\\{\\}]*)\\}\\}");
    
    public static Document compile(String template) throws Exception {
        try (var in = new ByteArrayInputStream(template.getBytes())) {
            return compile(in);
        }
    }

    public static Document compile(InputStream input) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document doc = db.parse(input);

        compileElement(doc.getDocumentElement());

        return doc;
    }

    public static Document eval(Document template, Map<String,Object> context) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document doc = db.newDocument();
        JexlContext jc = new MapContext(context);
        
        evalElement(doc, null, template.getDocumentElement(), jc);

        return doc;
    }

    public static String evalAsString(Document template, Map<String,Object> context) throws Exception {
        return DOMtoString(eval(template, context));
    }

    public static String DOMtoString(Document doc) throws Exception {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        //trans.setOutputProperty(OutputKeys.INDENT, "yes");

        // create string from xml tree
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, result);
        
        return sw.toString();
	}

    private static void evalElement(Document doc, Element parent, Element templateElement, JexlContext jexlCtx) {
        var e = doc.createElement(templateElement.getTagName());

        if (parent != null) {
            parent.appendChild(e);
        } else {
            doc.appendChild(e);
        }

        var attrs = templateElement.getAttributes();

        for (int i = 0; i < attrs.getLength(); ++i) {
            var attr = (Attr) attrs.item(i);
            var name = attr.getName();
            var val = attr.getNodeValue();

            if (name.startsWith(":")) {
                JexlExpression expr = (JexlExpression) attr.getUserData("expr");

                val = expr.evaluate(jexlCtx).toString();
                name = name.substring(1);
            }

            e.setAttribute(name, val);
        }

        var childNodes = templateElement.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); ++i) {
            var child = childNodes.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                evalElement(doc, e, (Element) child, jexlCtx);
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                List<MatchResult> matchResults = (List<MatchResult>) child.getUserData("matchResults");
                List<JexlExpression> exprList = (List<JexlExpression>) child.getUserData("expressionList");
                int nextStart = 0;
                var nodeText = child.getNodeValue();
                StringBuffer buff = new StringBuffer(nodeText.length() + 512);

                for (int j = 0; j < matchResults.size(); ++j) {
                    var mr = matchResults.get(j);
                    var expr = exprList.get(j);
                    var val = expr.evaluate(jexlCtx).toString();

                    buff.append(nodeText.substring(nextStart, mr.start()));
                    buff.append(val);

                    nextStart = mr.end();
                }

                buff.append(nodeText.substring(nextStart));

                e.appendChild(doc.createTextNode(buff.toString()));
            } else {
                e.appendChild(doc.importNode(child, false));
            }
        }
    }

    private static void compileElement(Element element) throws Exception {
        var attrs = element.getAttributes();

        for (int i = 0; i < attrs.getLength(); ++i) {
            var attr = (Attr) attrs.item(i);

            var name = attr.getName();
            var val = attr.getNodeValue();

            if (name.startsWith(":")) {
                JexlExpression e = jexl.createExpression(val);

                attr.setUserData("expr", e, null);
            }
        }

        var childNodes = element.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); ++i) {
            var child = childNodes.item(i);

            if (child.getNodeType() == Node.TEXT_NODE) {
                var nodeText = child.getNodeValue();
                Matcher matcher = pattern.matcher(nodeText);
                List<JexlExpression> exprList = new ArrayList<>();

                var matchResults = matcher
                    .results()
                    .peek(mr -> {
                        //Strip out the handlebars
                        var exprTxt = nodeText.substring(mr.start() + 2, mr.end() - 2);
                        JexlExpression expr = jexl.createExpression(exprTxt);

                        exprList.add(expr);
                    })
                    .collect(Collectors.toList());

                child.setUserData("matchResults", matchResults, null);
                child.setUserData("expressionList", exprList, null);
            }

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                compileElement((Element) child);
            }
        }

    }
}
