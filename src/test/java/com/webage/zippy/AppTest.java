package com.webage.zippy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;


public class AppTest {
    Map<String, Object> ctx = new HashMap<>();

    @Before
    public void setup() {
        ctx.put("firstName", "Daffy");
        ctx.put("lastName", "Duck");
        ctx.put("age", 12);
    }

    @Test
    public void basicLoopTest() throws Exception {
        var nameList = Arrays.asList("Daffy", "Bugs");
        var templateStr = "<div><p v-for='n  in   nameList' :name='n'></p></div>";
        var template = Zippy.compile(templateStr);

        ctx.put("nameList", nameList);

        var out = Zippy.eval(template, ctx);
        var childList = out.getElementsByTagName("p");

        assertEquals(nameList.size(), childList.getLength());

        for (int i = 0; i < childList.getLength(); ++i) {
            var child = (Element) childList.item(i);

            assertEquals(nameList.get(i), child.getAttribute("name"));
        }

        //Repeat eval
        nameList = Arrays.asList("Daffy", "Bugs", "Sylvester");
        ctx.put("nameList", nameList);
        out = Zippy.eval(template, ctx);
        childList = out.getElementsByTagName("p");

        assertEquals(nameList.size(), childList.getLength());
    }

    @Test
    public void loopWithArrayTest() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("AA.html")) {
            String nameList[] = {"Daffy", "Bugs"};
            var template = Zippy.compile(in);
    
            ctx.put("nameList", nameList);

            var out = Zippy.eval(template, ctx);
            var childList = out.getElementsByTagName("div");
    
            assertEquals(nameList.length, childList.getLength());                
        }
    }

    @Test
    public void forWithIfTest() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("AB.html")) {
            var nameList = Arrays.asList("Daffy", "Bugs");
            var template = Zippy.compile(in);
    
            ctx.put("nameList", nameList);
    
            var out = Zippy.eval(template, ctx);
            var childList = out.getElementsByTagName("div");
    
            assertEquals(1, childList.getLength());                
        }
    }

    @Test
    public void emptyList() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("AC.html")) {
            var nameList = new ArrayList<String>();
            var template = Zippy.compile(in);
    
            ctx.put("nameList", nameList);
    
            var out = Zippy.eval(template, ctx);
            var childList = out.getElementsByTagName("p");
    
            assertEquals(nameList.size(), childList.getLength());
            
            childList = out.getElementsByTagName("h3");
    
            assertEquals(1, childList.getLength());
        }
    }

    @Test
    public void resourceTest() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("AA.html")) {
            var nameList = Arrays.asList("Daffy", "Bugs");
            var template = Zippy.compile(in);
    
            ctx.put("nameList", nameList);
    
            var out = Zippy.eval(template, ctx);
            var childList = out.getElementsByTagName("div");
    
            assertEquals(nameList.size(), childList.getLength());
        }
    }

    @Test
    public void ifTest() throws Exception {
        var templateStr = "<div>Hello <p v-if='age == 12' :a='firstName'>OK</p><p v-if='age != 12'>BAD</p></div>";
        var template = Zippy.compile(templateStr);


        var out = Zippy.evalAsString(template, ctx);
        var expected = "<div>Hello <p a=\"Daffy\">OK</p></div>";

        assertEquals(expected, out);
    }

    @Test
    public void bodyExprTest() throws Exception {
        var templateStr = "<div>Hello {{firstName + \"::\" + lastName}}</div>";
        var template = Zippy.compile(templateStr);


        var out = Zippy.evalAsString(template, ctx);
        var expected = "<div>Hello Daffy::Duck</div>";

        assertEquals(expected, out);
    }

    @Test
    public void inerHTMLTest() throws Exception {
        var innerTemplate = Zippy.compile("<p id='p1'>Hello</p>");
        var innerDoc = Zippy.eval(innerTemplate, ctx);

        var templateStr = "<div>{{html}}</div>";
        var template = Zippy.compile(templateStr);

        ctx.put("html", innerDoc);

        var out = Zippy.eval(template, ctx);
        var childList = out.getElementsByTagName("p");

        assertEquals(1, childList.getLength());
    }

    @Test
    public void bodyTest() throws Exception {
        var templateStr = "<div>Hello there {{firstName}} -- {{lastName}}. How does it go?</div>";
        var template = Zippy.compile(templateStr);


        var out = Zippy.evalAsString(template, ctx);
        var expected = "<div>Hello there Daffy -- Duck. How does it go?</div>";

        assertEquals(expected, out);
    }

    @Test
    public void bodyNestTest() throws Exception {
        var templateStr = "<div>Hello {{firstName}} <p>{{lastName}}</p></div>";
        var template = Zippy.compile(templateStr);


        var out = Zippy.evalAsString(template, ctx);
        var expected = "<div>Hello Daffy <p>Duck</p></div>";

        assertEquals(expected, out);
    }

    @Test
    public void attrTest() throws Exception {
        var templateStr = "<div a='aval' :b='firstName' c='cval' :d='lastName'>Hello</div>";
        var template = Zippy.compile(templateStr);


        var out = Zippy.evalAsString(template, ctx);
        var expected = "<div a=\"aval\" b=\"Daffy\" c=\"cval\" d=\"Duck\">Hello</div>";

        assertEquals(expected, out);
    }

    @Test
    public void nullValueTest() throws Exception {
        var templateStr = "<div>Hello {{salary}} there!</div>";
        var template = Zippy.compile(templateStr);

        ctx.put("salary", null);

        var out = Zippy.evalAsString(template, ctx);
        var expected = "<div>Hello  there!</div>";

        assertEquals(expected, out);
    }

    @Test
    public void testImg() throws Exception {
        var template = Zippy.compileResource("img.html");

        assertTrue(Zippy.evalAsString(template, ctx).contains("<img src=\"url\"/>"));
    }

    @Test
    public void testInnerHTML() throws Exception {
        var template = Zippy.compile("<div v-html=\"message\"></div>");

        ctx.put("message", "<h1>Hello</h1><p>Para <b>bold</b></p>");

        assertEquals("<div><h1>Hello</h1><p>Para <b>bold</b></p></div>", Zippy.evalAsString(template, ctx));
    }

    @Test
    public void testInnerHTML2() throws Exception {
var template = Zippy.compile("<div v-html=\"message\"></div>");

ctx.put("message", "<h1>Hello</h1>");

System.out.println(Zippy.evalAsString(template, ctx));
    }
}
