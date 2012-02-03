package de.ingrid.search.utils.facet;

import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

/**
 * Interface that describes facet manager methods to be exposed to other componentes. 
 * 
 * @author joachim
 *
 */
public interface IFacetManager {

    public void initialize();

    public void addFacets(IngridHits hits, IngridQuery query);

}