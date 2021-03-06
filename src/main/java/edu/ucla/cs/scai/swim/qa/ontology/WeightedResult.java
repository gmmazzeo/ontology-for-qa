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
public class WeightedResult implements Comparable<WeightedResult> {

    protected double weight;

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(WeightedResult o) {
        return Double.compare(o.weight, weight);
    }

}
