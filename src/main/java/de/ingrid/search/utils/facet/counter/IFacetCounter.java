package de.ingrid.search.utils.facet.counter;

import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.facet.FacetDefinition;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;

public interface IFacetCounter {
    
    /**
     * Returns a IngridDocument with facet class as key and the number of hits for the result set of a given query.
     * 
     * @param result Map with facet class as key and the number of hits
     * @param query The initial query of the request.
     * @param bitset The search result bitSet of the query against the index.
     * @param facetDefs The facet definition from the search request
     * @return Map with facet class as key and the number of hits
     */
    public IngridDocument count(IngridDocument result, IngridQuery query, OpenBitSet[] bitsets, List<FacetDefinition> facetDefs);

}
