/**
 * 
 */
package de.ingrid.search.utils;

import java.util.List;

import de.ingrid.search.utils.facet.FacetDefinition;

/**
 * @author joachim
 *
 */
public interface IFacetDefinitionProcessor {

    void process(List<FacetDefinition> facetDefinitions);
    
}
