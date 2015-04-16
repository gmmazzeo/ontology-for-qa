/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public interface DataType {

    public HashSet<? extends Attribute> getRangeOfAttributes();

    public String getUri(); 
}
