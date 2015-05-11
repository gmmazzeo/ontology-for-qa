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

    public DBpediaEntityAnnotationResult(TagMeClient.AnnotationResult ar) {
        DBpediaNamedEntity e = new DBpediaNamedEntity();
        e.setLabel(ar.title);
        e.setUri("http://dbpedia.org/resource/" + ar.title.replace(" ", "_"));
        this.namedEntity = e;
        this.weight = ar.rho;
        this.begin = ar.start;
        this.end = ar.end;
        this.spot = ar.spot;
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
        return namedEntity.getUri() + " w=" + weight + " begin=" + begin + " end=" + end + " spot=" + spot;
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
