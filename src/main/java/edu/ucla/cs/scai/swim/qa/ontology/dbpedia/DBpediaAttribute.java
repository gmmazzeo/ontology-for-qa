/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.Attribute;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaAttribute implements Attribute {

    String label, uri, range, rangeUri;

    String words;

    @Override
    public String getName() {
        return label;
    }

    public String getLabel() {
        return label;
    }

    public String getWords() {
        return words;
    }

    public void setLabel(String label) {
        this.label = label;
        if (label.contains("_label")) {
            label = label.replace("_label", "Label");
        }
        try {
            String[] w = URLDecoder.decode(label, "UTF-8").split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
            words = w[0].toLowerCase();
            for (int i = 1; i < w.length; i++) {
                if (!w[i].toLowerCase().equals("of")) {
                    words += " " + w[i].toLowerCase();
                }
            }
        } catch (UnsupportedEncodingException ex) {
            words = label;
            Logger.getLogger(DBpediaAttribute.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getRangeUri() {
        return rangeUri;
    }

    public void setRangeUri(String rangeUri) {
        this.rangeUri = rangeUri;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.uri);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DBpediaAttribute other = (DBpediaAttribute) obj;
        if (!Objects.equals(this.uri, other.uri)) {
            return false;
        }
        return true;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return uri;
    }
}
