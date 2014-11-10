/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.LuceneIndexReaderWrapper;
import de.ingrid.search.utils.facet.counter.IFacetCounter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * <p>
 * The FacetManger provides an interface to add facet information to a
 * IngridHits object. It extracts the facet definition information from an
 * IngridQuery object.
 * </p>
 * 
 * <p>
 * This implementation must be configured with at least one index.
 * </p>
 * 
 * 
 * <p>
 * It must be configured with
 * </p>
 * <ul>
 * <li><b>IQueryParsers</b> - provides parsing from IngridQuery to Lucene Query
 * object.</li>
 * <li><b>LuceneIndexReaderWrapper</b> - wraps one or more lucene index reader.
 * More than one because the nutch search engine can use multiple indexes.</li>
 * <li><b>List of IFacetCounter</b> - A list of facet counters that create
 * facets and match the facets with the current query.</li>
 * <li><b>Initial facet query</b> - An initial facet query to initialize the
 * facets of this index based iplug.</li>
 * </ul>
 * 
 */
public class FacetManager extends AbstractFacetManager {

    private static Logger LOG = Logger.getLogger(FacetManager.class);

    private LuceneIndexReaderWrapper indexReaderWrapper = null;

    private String initialFacetQuery;

    public FacetManager() {
        super();
    }

    @Override
    public void initialize() {
        LOG.info("Initialize facet manager with query: " + initialFacetQuery);
        for (IFacetCounter fc : facetCounters) {
            fc.initialize();
        }
        if (facetCounters != null && initialFacetQuery != null) {
            IngridDocument result = new IngridDocument();

            try {
                IngridQuery q = QueryStringParser.parse(initialFacetQuery);
                List<FacetDefinition> defs = FacetUtils.getFacetDefinitions(q);
                // filter facet definitions if appropriate
                filterFacetDefinitions(defs);

                for (IFacetCounter fc : facetCounters) {
                    fc.count(result, q, new OpenBitSet[] { new OpenBitSet(1L) }, defs);
                }
            } catch (Exception e) {
                LOG.error("Error initialize facet manager with query: " + initialFacetQuery, e);
            }
        }
    }

    @Override
    protected OpenBitSet[] getResultBitsets(IngridHits hits, IngridQuery query) {
        return FacetUtils.getBitSetsFromQuery(queryParsers.parse(query), indexReaderWrapper);
    }

    public LuceneIndexReaderWrapper getIndexReaderWrapper() {
        return indexReaderWrapper;
    }

    public void setIndexReaderWrapper(LuceneIndexReaderWrapper indexReaderWrapper) {
        this.indexReaderWrapper = indexReaderWrapper;
    }

    public String getInitialFacetQuery() {
        return initialFacetQuery;
    }

    public void setInitialFacetQuery(String initialFacetQuery) {
        this.initialFacetQuery = initialFacetQuery;
    }

}
