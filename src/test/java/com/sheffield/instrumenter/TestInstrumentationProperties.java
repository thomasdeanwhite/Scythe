package com.sheffield.instrumenter;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by thomas on 29/11/2016.
 */
public class TestInstrumentationProperties {

    private static class MockProperties extends InstrumentationProperties {

        private static MockProperties instance = null;

        public static MockProperties instance(){
            if (instance == null){
                instance = new MockProperties();
            }

            InstrumentationProperties.instance();

            return instance;
        }

        public enum Enum {
            FOO, BAR
        }

        @Parameter(key = "enum", description = "", hasArgs = true, category = "")
        public static Enum ENUM = null;

        @Parameter(key = "string", description = "", hasArgs = true, category = "")
        public static String STRING = null;

        @Parameter(key = "boolean", description = "", hasArgs = true, category =
                "")
        public static boolean BOOLEAN = false;

        @Parameter(key = "boolean_no_args", description = "", hasArgs = false,
                category =
                "")
        public static boolean BOOLEAN_NO_ARGS = false;

        @Parameter(key = "integer", description = "", hasArgs = true, category =
                "")
        public static int INTEGER = 0;

        @Parameter(key = "long", description = "", hasArgs = true, category =
                "")
        public static long LONG = 0;

        @Parameter(key = "double", description = "", hasArgs = true, category =
                "")
        public static double DOUBLE = 0;

        @Parameter(key = "float", description = "", hasArgs = true, category =
                "")
        public static float FLOAT = 0;


        public MockProperties(){
            super();
        }


    }

    MockProperties ip;

    @Before
    public void setup(){
        ip = MockProperties.instance();
    }


    @Test
    public void setInvalidProperty(){
        String property = "object";

        String value = "none";

        try {
            ip.setParameter(property, value);
            fail("Property " + property + " should not be found!");
        } catch (IllegalAccessException e) {

        } catch (IllegalArgumentException e){

        }
    }


    @Test
    public void propertiesSynced(){
        Set<String> values = ip.annotationMap.keySet();

        for (String v : values){
            assertTrue("Annotation and parameter map may be desynced", ip.hasParameter(v));
        }
    }

    @Test
    public void parametersExist(){
        String[] keys = new String[]{
                "string",
                "double",
                "float",
                "integer",
                "long",
                "boolean",
                "boolean_no_args",
                "enum"
        };

        for (String s : keys){
            assertTrue(ip.getParameterNames().contains(s));
        }
    }

    @Test
    public void setBoolean(){
        String property = "boolean";

        try {
            ip.setParameter(property, "false");

            assertFalse(MockProperties.BOOLEAN);

            ip.setParameter(property, "true");


        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertTrue(MockProperties.BOOLEAN);
    }

    @Test
    public void setBooleanNoArgs(){
        String property = "boolean_no_args";

        try {

            ip.setParameter(property, "true");


        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertTrue(MockProperties.BOOLEAN_NO_ARGS);
    }

    @Test
    public void setString(){
        String property = "string";

        String value = "test.log";

        try {
            ip.setParameter(property, value);
        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertEquals(value, MockProperties.STRING);
    }

    @Test
    public void setInteger(){
        String property = "integer";

        int v = 8;

        String value = "" + v;

        try {
            ip.setParameter(property, value);
        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertEquals(v, MockProperties.INTEGER);
    }

    @Test
    public void setLong(){
        String property = "long";

        long v = 9;

        String value = "" + v;

        try {
            ip.setParameter(property, value);
        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertEquals(v, MockProperties.LONG);
    }

    @Test
    public void setLongAsDouble(){
        String property = "long";

        long v = 9;

        String value = v + ".0d";

        try {
            ip.setParameter(property, value);
        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertEquals(v, MockProperties.LONG);
    }

    @Test
    public void setFloat(){
        String property = "float";


        float v = 10f;
        String value = "" + v;

        try {
            ip.setParameter(property, value);
        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertEquals(v, MockProperties.FLOAT, 0.0000001);
    }

    @Test
    public void setDouble(){
        String property = "double";

        double v = 11d;

        String value = "" + v;

        try {
            ip.setParameter(property, value);
        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertEquals(v, MockProperties.DOUBLE, 0.0000001);
    }

    @Test
    public void setEnum(){
        String property = "enum";

        MockProperties.Enum v = MockProperties.Enum.FOO;

        String value = "" + v;

        try {
            ip.setParameter(property, value);
        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertEquals(v, MockProperties.ENUM);

        v = MockProperties.Enum.BAR;

        value = "" + v;

        try {
            ip.setParameter(property, value);
        } catch (IllegalAccessException e) {
            fail("Property  " + property + " was not found.");
        }

        assertEquals(v, MockProperties.ENUM);
    }





}
