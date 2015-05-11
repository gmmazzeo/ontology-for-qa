/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.AttributeLookupResult;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaAttributeLookupResult extends AttributeLookupResult {

    DBpediaAttribute attribute;
    boolean invertedRelationship;

    public DBpediaAttributeLookupResult(DBpediaAttribute attribute, double weight) {
        this.attribute = attribute;
        this.weight = weight;
    }

    @Override
    public DBpediaAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(DBpediaAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        return attribute.toString() + "[" + weight + "]"; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isInvertedRelationship() {
        return invertedRelationship;
    }
}
