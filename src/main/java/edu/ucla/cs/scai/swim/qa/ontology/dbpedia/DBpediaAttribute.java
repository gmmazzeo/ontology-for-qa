/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.Attribute;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaAttribute implements Attribute {

    String label, uri, range;
    HashSet<String> rangeUri = new HashSet<>();
    HashSet<String> domainUri = new HashSet<>();
    boolean rangeCanBeBasicType;

    HashMap<String, Double> domainDistribution = new HashMap<>();
    HashMap<String, Double> rangeDistribution = new HashMap<>();
    long triplesCount;

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
        if (label == null) {
            return;
        }
        if (label.contains("_label")) {
            label = label.replace("_label", "Label");
        }
        try {
            String[] w = URLDecoder.decode(label, "UTF-8").split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
            words = w[0].toLowerCase().trim();
            for (int i = 1; i < w.length; i++) {
                if (!w[i].toLowerCase().trim().equals("of")) {
                    words += " " + w[i].toLowerCase().trim();
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

    public HashSet<String> getRangeUri() {
        return rangeUri;
    }

    public void setRangeUri(HashSet<String> rangeUri) {
        this.rangeUri = rangeUri;
    }

    public HashSet<String> getDomainUri() {
        return domainUri;
    }

    public void setDomainUri(HashSet<String> domainUri) {
        this.domainUri = domainUri;
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

    public boolean rangeCanBeBasicType() {
        return rangeCanBeBasicType;
    }

    public void setRangeCanBeBasicType(boolean rangeCanBeBasicType) {
        this.rangeCanBeBasicType = rangeCanBeBasicType;
    }

    @Override
    public String toString() {
        return uri;
    }

    public HashMap<String, Double> getDomainDistribution() {
        return domainDistribution;
    }

    public void setDomainDistribution(HashMap<String, Double> domainDistribution) {
        this.domainDistribution = domainDistribution;
    }

    public HashMap<String, Double> getRangeDistribution() {
        return rangeDistribution;
    }

    public void setRangeDistribution(HashMap<String, Double> rangeDistribution) {
        this.rangeDistribution = rangeDistribution;
    }

    public long getTriplesCount() {
        return triplesCount;
    }

    public void setTriplesCount(long triplesCount) {
        this.triplesCount = triplesCount;
    }

}
