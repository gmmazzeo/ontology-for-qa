/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.Attribute;
import edu.ucla.cs.scai.swim.qa.ontology.Category;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaCategory implements Category {

    String label;

    String uri;

    String words;

    HashSet<DBpediaCategory> parents = new HashSet<>();

    HashSet<DBpediaCategory> subClasses = new HashSet<>();

    HashSet<DBpediaAttribute> domainOfAttributes = new HashSet<>();

    HashSet<DBpediaAttribute> rangeOfAttributes = new HashSet<>();

    HashSet<DBpediaCategory> ancestors;

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

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getName() {
        return label;
    }

    @Override
    public HashSet<DBpediaCategory> getParents() {
        return parents;
    }

    public HashSet<DBpediaCategory> getSubClasses() {
        return subClasses;
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
        return Objects.equals(this.uri, other.uri);
    }

    @Override
    public String getUri() {
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
        if (parents != null) {
            for (DBpediaCategory parent : parents) {
                if (parent.updateMostPopularEntity(mostPopularEntity, popularity)) {
                    parent.updateAncestorsPopularity();
                }
            }
        }
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    @Override
    public String toString() {
        return uri;
    }

    @Override
    public HashSet<? extends Attribute> getDomainOfAttributes() {
        return domainOfAttributes;
    }

    @Override
    public HashSet<? extends Attribute> getRangeOfAttributes() {
        return rangeOfAttributes;
    }

    public HashSet<DBpediaCategory> computeAncestors() {
        if (ancestors == null) {
            ancestors = new HashSet<>();
            ancestors.addAll(parents);
            for (DBpediaCategory c : parents) {
                ancestors.addAll(c.computeAncestors());
            }
        }
        return ancestors;
    }

    public boolean hasAncestor(DBpediaCategory c) {
        if (ancestors == null) {
            computeAncestors();
        }
        return ancestors.contains(c);
    }

    public HashSet<DBpediaCategory> lowestCommonAncestors(DBpediaCategory c) {
        HashSet<DBpediaCategory> res = new HashSet<>();

        if (this.equals(c)) {
            res.add(c);
            return res;
        }

        if (hasAncestor(c)) {
            res.add(c);
            return res;
        } else if (c.hasAncestor(this)) {
            res.add(this);
            return res;
        }

        //here, ancestors of this and c have been already computed

        HashSet<DBpediaCategory> temp = new HashSet<>();
        for (DBpediaCategory a : ancestors) {
            if (c.ancestors.contains(a)) {
                temp.add(a);
            }
        }

        for (DBpediaCategory a : temp) {
            boolean add = true;
            for (Iterator<DBpediaCategory> it = res.iterator(); it.hasNext();) {
                DBpediaCategory a2 = it.next();
                if (a.hasAncestor(a2)) {
                    add = false;
                    break;
                } else if (a2.hasAncestor(a)) {
                    it.remove();
                    //don't break - a can be the ancestor of other categories in res
                }
            }
            if (add) {
                res.add(a);
            }
        }

        return res;
    }

}
