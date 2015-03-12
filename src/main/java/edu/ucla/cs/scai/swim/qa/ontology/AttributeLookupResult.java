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
public abstract class AttributeLookupResult extends WeightedResult {

    public abstract Attribute getAttribute();

    @Override
    public boolean equals(Object obj) {
        return getAttribute().equals(((AttributeLookupResult)obj).getAttribute());
    }

    @Override
    public int hashCode() {
        return getAttribute().hashCode();
    }

}
