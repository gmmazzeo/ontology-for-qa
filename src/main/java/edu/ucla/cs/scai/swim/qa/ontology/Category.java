package edu.ucla.cs.scai.swim.qa.ontology;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public interface Category {

    public String getName();

    public HashSet<? extends Attribute> getDomainOfAttributes();
    
    public HashSet<? extends Attribute> getRangeOfAttributes();

    public HashSet<? extends Category> getParents();
    
    public String getUri();

}
