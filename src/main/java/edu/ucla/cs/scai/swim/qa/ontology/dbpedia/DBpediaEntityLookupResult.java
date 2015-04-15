/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.NamedEntityLookupResult;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaEntityLookupResult extends NamedEntityLookupResult {

    DBpediaNamedEntity namedEntity;

    public DBpediaEntityLookupResult(DBpediaNamedEntity namedEntity, double weight) {
        this.namedEntity = namedEntity;
        this.weight = weight;
    }

    @Override
    public DBpediaNamedEntity getNamedEntity() {
        return namedEntity;
    }

    public void setNamedEntity(DBpediaNamedEntity entity) {
        this.namedEntity = entity;
    }

}
