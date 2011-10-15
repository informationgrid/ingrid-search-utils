package de.ingrid.search.utils.facet;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.IQueryParsers;
import de.ingrid.search.utils.LuceneIndexReaderWrapper;
import de.ingrid.search.utils.facet.counter.IFacetCounter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * The FacetManger provides an interface to add facet information to a
 * IngridHits object. It extracts the facet definition information from an
 * IngridQuery object. It must be configured with
 * <ul>
 * <li><b>IQueryParsers</b> - provides parsing from IngridQuery to Lucene Query object.</li>
 * <li><b>LuceneIndexReaderWrapper</b> - wraps one or more lucene index reader. More than one because the nutch search engine can use multiple indexes.</li>
 * <li><b>List of IFacetCounter</b> - A list of facet counters that create facets and match the facets with the current query.</li>
 * </ul> 
 * 
 */
public class FacetManager {

    private static Logger LOG = Logger.getLogger(FacetManager.class);

    private List<IFacetCounter> facetCounters;

    private IQueryParsers queryParsers = null;

    private LuceneIndexReaderWrapper indexReaderWrapper = null;

    private String initialFacetQuery;

    public FacetManager() {
    }

    public void initialize() {
        LOG.info("Initialize facet manager with query: " + initialFacetQuery);
        for (IFacetCounter fc : facetCounters) {
            fc.initialize();
        }
        if (facetCounters != null && initialFacetQuery != null) {
            IngridDocument result = new IngridDocument();

            try {
                IngridQuery q = QueryStringParser.parse(initialFacetQuery);

                for (IFacetCounter fc : facetCounters) {
                    fc.count(result, q, new OpenBitSet[] { new OpenBitSet(1L) }, FacetUtils.getFacetDefinitions(q));
                }
            } catch (Exception e) {
                LOG.error("Error initialize facet manager with query: " + initialFacetQuery, e);
            }
        }
    }

    public void addFacets(IngridHits hits, IngridQuery query) {
        if (query.containsKey("FACETS")) {
            long start = 0;
            if (LOG.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            IngridDocument facets = getFacetClassCounts(query, FacetUtils.getBitSetsFromQuery(
                    queryParsers.parse(query), indexReaderWrapper));
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
    public IngridDocument getFacetClassCounts(IngridQuery query, OpenBitSet[] bitset) {
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

    public IQueryParsers getQueryParsers() {
        return queryParsers;
    }

    public void setQueryParsers(IQueryParsers queryParsers) {
        this.queryParsers = queryParsers;
    }

    public LuceneIndexReaderWrapper getIndexReaderWrapper() {
        return indexReaderWrapper;
    }

    public void setIndexReaderWrapper(LuceneIndexReaderWrapper indexReaderWrapper) {
        this.indexReaderWrapper = indexReaderWrapper;
    }

    public List<IFacetCounter> getFacetCounters() {
        return facetCounters;
    }

    public void setFacetCounters(List<IFacetCounter> facetCounters) {
        this.facetCounters = facetCounters;
    }

    public String getInitialFacetQuery() {
        return initialFacetQuery;
    }

    public void setInitialFacetQuery(String initialFacetQuery) {
        this.initialFacetQuery = initialFacetQuery;
    }

}
