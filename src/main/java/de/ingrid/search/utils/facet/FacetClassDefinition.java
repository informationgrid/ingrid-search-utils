package de.ingrid.search.utils.facet;

public class FacetClassDefinition {
    private String name;

    private String definition;
    
    /**
     * contains the calculated document hits matching this Facet class
     */
    private long hitCount = -1;

    
    public FacetClassDefinition(String facetName, String definition) {
        this.name = facetName;
        this.definition = definition;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

}
