/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.search.utils.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;

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
            String facetField = (String) aFacet.get("field");
            if (facetField == null) {
                facetField = facetName;
            }
            String facetFragment = (String) aFacet.get("query");
            FacetDefinition fd = new FacetDefinition(facetName, facetField);
            fd.setQueryFragment(facetFragment);

            List facetClasses = (List) aFacet.get("classes");
            // if facet classes were defined then look through those and create
            // their queries as String
            if (facetClasses != null) {
                String facetClassName;
                for (Object facetClass : facetClasses) {
                    facetClassName = (String) ((Map) facetClass).get("id");
                    fd.addFacetClass(new FacetClassDefinition(FacetUtils.getCacheKeyName(facetName, facetClassName),
                            (String) ((Map) facetClass).get("query")));
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
                DocIdSet queryBitset = filter.getDocIdSet(indexReaders[i]);
                OpenBitSet queryOpenBitset;
                // check for an required OpenBitSet, create one if the docIdSet
                // is not already a OpenBitSet instance
                // not 100% sure when an openBitSet is returned and when not
                // was observed if the query is no Boolean query or if the query
                // is a single/multiple MUST_NOT query
                if (queryBitset instanceof OpenBitSet) {
                    queryOpenBitset = (OpenBitSet) queryBitset;
                } else {
                    queryOpenBitset = new OpenBitSetDISI(queryBitset.iterator(), indexReaders[i].maxDoc());
                }

                result[i] = queryOpenBitset;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Create bit set for indexreader[" + i + "] for lucene query '" + query
                            + "' with cardinallity=" + queryOpenBitset.cardinality() + " in "
                            + (System.currentTimeMillis() - start) + " ms.");
                }
            }
            return result;
        } catch (IOException e) {
            LOG.error("Error producing bitset from query '" + query + "'.", e);
        }
        return null;
    }

    /**
     * Creates an OpenBitset with a specific cardinality. 
     * 
     * @param cardinality
     * @return
     */
    public static OpenBitSet createOpenBitsetWithCardinality(long cardinality) {

        OpenBitSet result = new OpenBitSet(cardinality);
        result.set(0, cardinality);
        return result;

    }

}
