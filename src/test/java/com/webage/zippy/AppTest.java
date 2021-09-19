package com.webage.zippy;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    Map<String, Object> ctx = new HashMap<>();

    @Before
    public void setup() {
        ctx.put("firstName", "Daffy");
        ctx.put("lastName", "Duck");
    }

    @Test
    public void bodyTest() throws Exception {
        var templateStr = "<div>Hello there {{firstName}} -- {{lastName}}. How does it go?</div>";
        var template = Zippy.compile(templateStr);


        var out = Zippy.evalAsString(template, ctx);
        var expected = "<div>Hello there Daffy -- Duck. How does it go?</div>\n";

        assertEquals(expected, out);
    }

    @Test
    public void attrTest() throws Exception {
        var templateStr = "<div a='aval' :b='firstName' c='cval' :d='lastName'>Hello</div>";
        var template = Zippy.compile(templateStr);


        var out = Zippy.evalAsString(template, ctx);
        var expected = "<div a=\"aval\" b=\"Daffy\" c=\"cval\" d=\"Duck\">Hello</div>\n";

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
