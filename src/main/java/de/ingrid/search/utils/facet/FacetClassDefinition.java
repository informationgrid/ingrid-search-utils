package de.ingrid.search.utils.facet;

public class FacetClassDefinition {
    private String name;

    private String queryFragment;
    
    /**
     * contains the calculated document hits matching this Facet class
     */
    private long hitCount = -1;

    
    public FacetClassDefinition(String facetName, String queryFragment) {
        this.name = facetName;
        this.queryFragment = queryFragment;
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

    public void setQueryFragment(String queryFragment) {
        this.queryFragment = queryFragment;
    }

    public String getFragment() {
        return queryFragment;
    }

}
