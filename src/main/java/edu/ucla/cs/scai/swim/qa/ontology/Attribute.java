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
public interface Attribute {
    
    public String getName();
    
    public HashSet<? extends String> getDomainUri();
    
    public HashSet<? extends String> getRangeUri();
    
    public String getUri();
    
}
