package com.webage.zippy;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    @Test
    public void sayHello() throws Exception {
        var temp = "<div a='aval' :b='firstName'>Hello there <p :c='lastName'>Buddy</p></div>";
        var in = new ByteArrayInputStream(temp.getBytes());
        var template = Zippy.compile(in);
        Map<String, Object> ctx = new HashMap<>();

        ctx.put("firstName", "Daffy");
        ctx.put("lastName", "Duck");

        System.out.println(Zippy.evalAsString(template, ctx));
    }
}
