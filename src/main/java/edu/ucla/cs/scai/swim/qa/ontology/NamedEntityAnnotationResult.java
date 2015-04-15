package edu.ucla.cs.scai.swim.qa.ontology;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public abstract class NamedEntityAnnotationResult extends WeightedResult {
 
    public abstract NamedEntity getNamedEntity();

    public abstract int getBegin();

    public abstract int getEnd();

    public abstract String getSpot();

}
