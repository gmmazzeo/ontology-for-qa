/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia.tipicality;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Pair implements Comparable<Pair> {

    String s;
    double p;

    public Pair(String s, double p) {
        this.s = s;
        this.p = p;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    @Override
    public int compareTo(Pair o) {
        return Double.compare(o.p, p);
    }

    @Override
    public boolean equals(Object obj) {
        Pair o = (Pair) obj;
        return s.equals(o.s);
    }
}
