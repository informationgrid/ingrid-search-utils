package de.ingrid.search.utils.facet;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.IQueryParsers;
import de.ingrid.search.utils.facet.counter.IFacetCounter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

/**
 * <p>
 * Abstract FacetManger that abstracts common functionality.
 * </p>
 * 
 */
public abstract class AbstractFacetManager implements IFacetManager {

    private static Logger LOG = Logger.getLogger(AbstractFacetManager.class);

    protected List<IFacetCounter> facetCounters;

    protected IQueryParsers queryParsers = null;

    public AbstractFacetManager() {
    }

    /* (non-Javadoc)
     * @see de.ingrid.search.utils.facet.IFacetManager#initialize()
     */
    public abstract void initialize();

    /* (non-Javadoc)
     * @see de.ingrid.search.utils.facet.IFacetManager#addFacets(de.ingrid.utils.IngridHits, de.ingrid.utils.query.IngridQuery)
     */
    public void addFacets(IngridHits hits, IngridQuery query) {
        if (query.containsKey("FACETS")) {
            long start = 0;
            if (LOG.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            IngridDocument facets = getFacetClassCounts(query, getResultBitsets(hits, query));
            hits.put("FACETS", facets);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Add facet classes to hits in " + (System.currentTimeMillis() - start) + " ms.");
            }
        }
    }

    /**
     * 
     * @param query
     * @param bitset
     * @return a map, with each facet class and its hits-count on the query or
     *         null, if no facets were specified
     */
    protected IngridDocument getFacetClassCounts(IngridQuery query, OpenBitSet[] bitset) {
        // get all FacetDefinitions from the Query
        List<FacetDefinition> defs = FacetUtils.getFacetDefinitions(query);

        IngridDocument result = new IngridDocument();
        // calculate the Facets from the results of the base query
        // use different counters as configured
        for (IFacetCounter fc : facetCounters) {
            fc.count(result, query, bitset, defs);
        }

        return result;
    }

    protected abstract OpenBitSet[] getResultBitsets(IngridHits hits, IngridQuery query);

    public IQueryParsers getQueryParsers() {
        return queryParsers;
    }

    public void setQueryParsers(IQueryParsers queryParsers) {
        this.queryParsers = queryParsers;
    }

    public List<IFacetCounter> getFacetCounters() {
        return facetCounters;
    }

    public void setFacetCounters(List<IFacetCounter> facetCounters) {
        this.facetCounters = facetCounters;
    }

}
