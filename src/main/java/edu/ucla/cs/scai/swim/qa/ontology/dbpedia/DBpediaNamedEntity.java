/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import com.google.api.client.util.Key;
import edu.ucla.cs.scai.swim.qa.ontology.Category;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaNamedEntity implements NamedEntity {

    @Key
    String label;

    @Key
    String uri;

    @Key
    String description;

    @Key
    ArrayList<DBpediaCategory> classes = new ArrayList<>();

    @Key
    int refCount;

    HashSet<DBpediaCategory> categories;

    HashSet<String> domainOfAttributes = new HashSet<>();

    HashSet<String> rangeOfAttributes = new HashSet<>();

    String thumbUrl;

    String pageUrl;

    @Override
    public String getName() {
        return label;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

    @Override
    public Set<? extends Category> getCategories() {
        if (categories == null) {
            categories = new HashSet<>(classes);
        }
        return categories;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getPageUrl() {
        return pageUrl;
    }

    @Override
    public String getThumbUrl() {
        return thumbUrl;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public HashSet<String> getDomainOfAttributes() {
        return domainOfAttributes;
    }

    public HashSet<String> getRangeOfAttributes() {
        return rangeOfAttributes;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getRefCount() {
        return refCount;
    }

    public void setRefCount(int refCount) {
        this.refCount = refCount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public void setNameFromUri() {
        if (uri == null) {
            label = null;
            return;
        }
        String[] s = uri.split("/");
        String l = s[s.length - 1];
        label = l.replace("_", " ");
    }

}
