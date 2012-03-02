package de.ingrid.search.utils.facet;

import java.util.ArrayList;
import java.util.List;

public class FacetDefinition {

    /**
     * Should contain the index field where this facet belongs to.
     */
    private String field;

    /**
     * id = FacetName:FacetValue
     * 
     * e.g.: datatype:iso, metaclass:0, partner:bw
     */
    private String name;
    
    /**
     * Optional, a query queryFragment that narrows the facet classes in field definition.
     * 
     */
    private String queryFragment;
    

    /**
     * 
     * @element-type FacetClassDefinition
     */
    private List<FacetClassDefinition> classes;
    

    public FacetDefinition(String facetName, String field) {
        this.name = facetName;
        this.field = field;
        this.classes = new ArrayList<FacetClassDefinition>();
    }

    public Boolean hasFacetClass(String clazz) {
        return null;
    }

    public void addFacetClass(FacetClassDefinition facetClass) {
        this.classes.add(facetClass);        
    }

    public void setClasses(List<FacetClassDefinition> classes) {
        this.classes = classes;
    }

    /**
     * Return the FacetClasses if any, otherwise return null.
     * @return
     */
    public List<FacetClassDefinition> getClasses() {
        if (classes.size() == 0)
            return null;
        return classes;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getQueryFragment() {
        return queryFragment;
    }

    public void setQueryFragment(String queryFragment) {
        this.queryFragment = queryFragment;
    }


}
