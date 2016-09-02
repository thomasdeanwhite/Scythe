package com.sheffield.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by thomas on 9/1/2016.
 */
public class Csv {
    private HashMap<String, String> properties;
    private boolean finalized;

    public Csv() {
        properties = new HashMap<String, String>();
        finalized = false;
    }

    public void add(String property, String value) {
        if (finalized){
            throw new IllegalStateException("Cannot write to finalized Csv.");
        }
        properties.put(property, value);
    }

    public String getHeaders() {
        checkFinalized();
        ArrayList<String> headers = new ArrayList<String>();

        for (String s : properties.keySet()) {
            headers.add(s);
        }

        Collections.sort(headers);

        String h = "";

        for (String s : headers) {
            h += s + ",";
        }

        h.substring(0, h.length() - 1);

        return h;
    }

    public String getValues() {
        checkFinalized();
        ArrayList<String> headers = new ArrayList<String>();

        for (String s : properties.keySet()) {
            headers.add(s);
        }

        Collections.sort(headers);

        String h = "";

        for (String s : headers) {
            h += properties.get(s) + ",";
        }

        h.substring(0, h.length() - 1);

        return h;
    }

    public void finalize(){
        finalized = true;
    }

    private void checkFinalized(){
        if (!finalized){
            throw new IllegalStateException("Csv must be finalized before values can be read!");
        }
    }

    public void merge(Csv csv){
        for (String key : csv.properties.keySet()){
            add(key, csv.properties.get(key));
        }
    }
}