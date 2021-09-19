package com.webage.zippy;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Unit test for simple App.
 */
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
    public void forWithIfTest() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("AB.html")) {
            var nameList = Arrays.asList("Daffy", "Bugs");
            var template = Zippy.compile(in);
    
            ctx.put("nameList", nameList);
    
            var out = Zippy.eval(template, ctx);
            var childList = out.getElementsByTagName("p");
    
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

    /*
    @Test
    public void regexTest() {
        String text    = "JJ {{This}} is the {{text}} which is to be {{searched}} ";

        String str = "\\{\\{([^\\{\\}]*)\\}\\}";
        var buff = new StringBuffer();
        Pattern pattern = Pattern.compile(str);
        Matcher matcher = pattern.matcher(text);

        while(matcher.find()) {
            System.out.printf("Starts: %d Ends: %d. %s\n", matcher.start(), matcher.end(), text.substring(matcher.start()+2, matcher.end()-2));
            matcher.appendReplacement(buff, "HHH");
        }

        matcher.appendTail(buff);

        System.out.println(buff.toString());
    }
    */
}
