/**
 * 
 */
package de.ingrid.search.utils.facet;

import java.util.List;
import java.util.Map;

import de.ingrid.search.utils.IFacetDefinitionProcessor;

/**
 * @author joachim
 *
 */
public class ConfigurableFacetClassProcessor implements IFacetDefinitionProcessor {

    /**
     * Holds the filter definitions: {<class_id>, {<query_fragment>, <query_fragment_substitution}}
     * 
     */
    private Map<String, Map<String, String>> facetFilterDefinitions = null;
    
    /* (non-Javadoc)
     * @see de.ingrid.search.utils.IFacetDefinitionProcessor#process(java.util.List)
     */
    @Override
    public void process(List<FacetDefinition> facetDefinitions) {
        for (FacetDefinition facetDef : facetDefinitions) {
            if (facetDef.getClasses() != null) { 
                for (FacetClassDefinition facetClass : facetDef.getClasses()) {
                    for (String filterFacetClass : facetFilterDefinitions.keySet()) {
                        if (facetClass.getName().equals(filterFacetClass)) {
                            for (String facetClassQueryFragment : facetFilterDefinitions.get(filterFacetClass).keySet()) {
                                if (facetClass.getFragment() != null && facetClass.getFragment().equals(facetClassQueryFragment)) {
                                    facetClass.setQueryFragment(facetFilterDefinitions.get(filterFacetClass).get(facetClassQueryFragment));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void setFacetFilterDefinitions(Map<String, Map<String, String>> facetFilterDefinitions) {
        this.facetFilterDefinitions = facetFilterDefinitions;
    }

}
