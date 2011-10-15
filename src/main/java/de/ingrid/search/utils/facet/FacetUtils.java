package de.ingrid.search.utils.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.LuceneIndexReaderWrapper;
import de.ingrid.utils.query.IngridQuery;

public class FacetUtils {

    private static Logger LOG = Logger.getLogger(FacetUtils.class);

    public static String getCacheKeyName(String facetName, String facetClassName) {
        return facetName + ":" + facetClassName;
    }

    public static String getFacetNameFromFacetClass(String facetClassName) {
        int pos = facetClassName.indexOf(':');
        if (pos >= 0) {
            return facetClassName.substring(0, pos - 1);
        } else {
            return null;
        }
    }

    /**
     * Find all definitions of facets inside a query and check if we already do
     * have a FacetClassDefinition for it. If a FacetClassDefinition does not
     * exist then create it. If no FacetClassDefinitions were found for a Facet,
     * then we have to do a time consuming index search to find all possible
     * values for this FacetClass (but maximum of MAX_VALUES). This definition
     * will be checked in the FacetRegistry. If it's not there then a FacetClass
     * will be produced by the FacetClassProducer, which also will contain all
     * the docids in a BitSet, which is needed for a Facet-Search.
     */
    @SuppressWarnings( { "unchecked" })
    public static List<FacetDefinition> getFacetDefinitions(IngridQuery query) {
        List<FacetDefinition> facetDefs = new ArrayList<FacetDefinition>();

        List facets = (List) query.get("FACETS");
        if (facets == null)
            return null;

        // iterate through all facets
        for (Object facet : facets) {
            Map aFacet = (Map) facet;
            String facetName = (String) aFacet.get("id");
            List facetClasses = (List) aFacet.get("classes");
            FacetDefinition fd = new FacetDefinition(facetName, (String) aFacet.get("id"));
            // if facet classes were defined then look through those and create
            // their queries as String
            if (facetClasses != null) {
                String facetClassName;
                for (Object facetClass : facetClasses) {
                    facetClassName = (String) ((Map) facetClass).get("id");
                    fd.addFacetClass(new FacetClassDefinition(FacetUtils.getCacheKeyName(facetName, facetClassName),
                            (String) ((Map) facetClass).get("fragment")));
                }
            }
            facetDefs.add(fd);
        }
        return facetDefs;
    }

    public static OpenBitSet[] getBitSetsFromQuery(Query query, LuceneIndexReaderWrapper indexReaderWrapper) {
        long start = 0;
        if (LOG.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        CachingWrapperFilter filter = new CachingWrapperFilter(new QueryWrapperFilter(query));
        try {
            IndexReader[] indexReaders = indexReaderWrapper.getIndexReader();
            OpenBitSet[] result = new OpenBitSet[indexReaders.length];
            for (int i = 0; i < indexReaders.length; i++) {
                OpenBitSet queryBitset = (OpenBitSet) filter.getDocIdSet(indexReaders[i]);
                result[i] = queryBitset;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Create bit set for indexreader[" + i + "] for lucene query '" + query
                            + "' with cardinallity=" + queryBitset.cardinality() + " in "
                            + (System.currentTimeMillis() - start) + " ms.");
                }
            }
            return result;
        } catch (IOException e) {
            LOG.error("Error producing bitset from query '" + query + "'.", e);
        }
        return null;
    }

}
