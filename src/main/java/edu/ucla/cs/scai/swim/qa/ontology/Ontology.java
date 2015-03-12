package edu.ucla.cs.scai.swim.qa.ontology;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public interface Ontology {
    
    public ArrayList<? extends NamedEntityLookupResult> lookupEntity(String entityName) throws Exception;
    
    public ArrayList<? extends AttributeLookupResult> lookupAttribute(String attributeName) ;
    
    public ArrayList<? extends CategoryLookupResult> lookupCategory(String categoryName);
    
    public ArrayList<? extends CategoryLookupResult> lookupCategory(Collection<Attribute> attributes);
    
    public ArrayList<? extends NamedEntityAnnotationResult> annotateNamedEntities(String sentence);

    public Attribute getAbstractAttribute();
    
    //public ArrayList<? extends QueryResult> executeQuery(Query q);
    
}
