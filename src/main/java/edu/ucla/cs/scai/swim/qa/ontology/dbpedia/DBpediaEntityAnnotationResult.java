/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.NamedEntity;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntityAnnotationResult;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaEntityAnnotationResult extends NamedEntityAnnotationResult {

    int begin;
    int end;
    DBpediaNamedEntity namedEntity;
    String spot;

    public DBpediaEntityAnnotationResult(DBpediaNamedEntity namedEntity, double weight, int begin, int end, String spot) {
        this.weight = weight;
        this.namedEntity = namedEntity;
        this.begin = begin;
        this.end = end;
        this.spot = spot;
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return namedEntity.getUri() + " w=" + weight + " begin=" + begin + " end=" + end;
    }

    @Override
    public NamedEntity getNamedEntity() {
        return namedEntity;
    }

    @Override
    public String getSpot() {
        return spot;
    }

}
