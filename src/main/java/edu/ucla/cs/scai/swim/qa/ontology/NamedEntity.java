package edu.ucla.cs.scai.swim.qa.ontology;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Set;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public interface NamedEntity {

    public String getName();
    
    public String getDescription();
    
    public String getPageUrl();
    
    public String getThumbUrl();

    public String getProperty(String name);

    public Set<? extends Category> getCategories();
    
    public String getUri();
}
