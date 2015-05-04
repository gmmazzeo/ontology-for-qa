/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.Attribute;
import edu.ucla.cs.scai.swim.qa.ontology.DataType;
import java.util.HashSet;
import java.util.Objects;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaDataType implements DataType {

    HashSet<DBpediaAttribute> rangeOfAttributes = new HashSet<>();

    String uri;

    Class javaClass;

    public DBpediaDataType(String uri, Class javaClass) {
        this.uri = uri;
        this.javaClass = javaClass;
    }

    @Override
    public HashSet<? extends Attribute> getRangeOfAttributes() {
        return rangeOfAttributes;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public Class getJavaClass() {
        return javaClass;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.uri);
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
        final DBpediaDataType other = (DBpediaDataType) obj;
        if (!Objects.equals(this.uri, other.uri)) {
            return false;
        }
        return true;
    }

}
