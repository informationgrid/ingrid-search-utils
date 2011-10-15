package de.ingrid.search.utils.facet;

import java.io.Serializable;

import org.apache.lucene.util.OpenBitSet;

public class FacetClass implements Serializable {

    /**
     * The serialization ID for storing objects.
     */
    private static final long serialVersionUID = 7282020135406385812L;

    private String facetClassName;

    private OpenBitSet[] bitSets;

    public FacetClass(String facetClassname, OpenBitSet[] filterBitSet) {
        this.facetClassName = facetClassname;
        this.bitSets = filterBitSet;
    }

    public void setFacetClassName(String facetName) {
        this.facetClassName = facetName;
    }

    public String getFacetClassName() {
        return facetClassName;
    }

    public void setBitSets(OpenBitSet[] bitSet) {
        this.bitSets = bitSet;
    }

    public OpenBitSet[] getBitSets() {
        return bitSets;
    }

    public String toString() {
        String result = facetClassName + " with bitsets:";
        for (int i = 0; i < bitSets.length; i++) {
            if (bitSets[i] != null) {
                result += " " + i + ":" + bitSets[i].cardinality();
            } else {
                result += " " + i + ":" + null;
            }
        }
        return result;
    }

}