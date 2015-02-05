/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.Category;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaCategory implements Category {

    String label;

    String uri;

    String words;

    DBpediaCategory parent;

    ArrayList<DBpediaCategory> subclasses = new ArrayList<>();

    ArrayList<DBpediaAttribute> attributes = new ArrayList<>();

    String mostPopularEntity;
    int popularity;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        String[] w = label.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        words = w[0].toLowerCase();
        for (int i = 1; i < w.length; i++) {
            words += " " + w[i].toLowerCase();
        }
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getName() {
        return label;
    }

    @Override
    public Category getParent() {
        return parent;
    }

    public void setParent(DBpediaCategory parent) {
        this.parent = parent;
    }

    public ArrayList<DBpediaCategory> getSubclasses() {
        return subclasses;
    }

    @Override
    public ArrayList<DBpediaAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.label);
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
        final DBpediaCategory other = (DBpediaCategory) obj;
        if (!Objects.equals(this.uri, other.uri)) {
            return false;
        }
        return true;
    }

    @Override
    public String getURI() {
        return uri;
    }

    public String getMostPopularEntity() {
        return mostPopularEntity;
    }

    public boolean updateMostPopularEntity(String mostPopularEntity, int popularity) {
        if (this.mostPopularEntity == null || popularity > this.popularity) {
            this.mostPopularEntity = mostPopularEntity;
            this.popularity = popularity;
            return true;
        }
        return false;
    }
    
    public void updateAncestorsPopularity() {
        if (parent!=null && parent.updateMostPopularEntity(mostPopularEntity, popularity)) {
            parent.updateAncestorsPopularity();
        }
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }
}
