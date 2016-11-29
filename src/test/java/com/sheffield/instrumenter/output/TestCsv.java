package com.sheffield.instrumenter.output;

import com.sheffield.output.Csv;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by thomas on 29/11/2016.
 */
public class TestCsv {

    @Test
    public void testCsvHeadersAlphabetically(){
        Csv csv = new Csv();

        csv.add("foo", "barV");
        csv.add("bar", "fooV");

        csv.finalize();

        assertEquals("bar,foo", csv.getHeaders());
    }

    @Test
    public void testCsvHeaders(){
        Csv csv = new Csv();
        csv.add("bar", "fooV");
        csv.add("foo", "barV");

        csv.finalize();

        assertEquals("bar,foo", csv.getHeaders());
    }

    @Test
    public void testCsvValues(){
        Csv csv = new Csv();
        csv.add("bar", "fooV");
        csv.add("foo", "barV");

        csv.finalize();

        assertEquals("fooV,barV", csv.getValues());
    }

    @Test
    public void testUnFinialized(){
        Csv csv = new Csv();
        csv.add("bar", "fooV");
        csv.add("foo", "barV");

        try {
            csv.getHeaders();
            fail("Exception should be through, unfinalized");
        } catch (IllegalStateException e){

        }

        try {
            csv.getValues();
            fail("Exception should be through, unfinalized");
        } catch (IllegalStateException e){

        }
    }

    @Test
    public void testFinializedModification(){
        Csv csv = new Csv();
        csv.add("bar", "fooV");
        csv.add("foo", "barV");

        csv.finalize();

        try {
            csv.add("some", "property");
            fail("Adding property after finalized");
        } catch (IllegalStateException e){

        }
    }

    @Test
    public void testMerge(){
        Csv csv = new Csv();
        csv.add("bar", "fooV");

        Csv csv2 = new Csv();

        csv2.add("foo", "barV");

        csv.merge(csv2);

        csv.finalize();

        assertEquals("bar,foo", csv.getHeaders());
        assertEquals("fooV,barV", csv.getValues());

    }

}
