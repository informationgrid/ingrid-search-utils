/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.search.utils.facet;

import org.apache.log4j.Logger;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.facet.counter.IFacetCounter;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

/**
 * <p>The FacetManger provides an interface to add facet information to a
 * IngridHits object. It extracts the facet definition information from an
 * IngridQuery object.</p> 
 * 
 * <p>This implementation can only be used with index-less iPlugs. It must not 
 * be configured with facet counters that require an index.</p> 
 * 
 * <p>It must be configured with</p>
 * <ul>
 * <li><b>IQueryParsers</b> - provides parsing from IngridQuery to Lucene Query
 * object.</li>
 * <li><b>List of IFacetCounter</b> - A list of facet counters that create
 * facets and match the facets with the current query.</li>
 * </ul>
 * 
 */
public class NoIndexFacetManager extends AbstractFacetManager {

    private static Logger LOG = Logger.getLogger(NoIndexFacetManager.class);

    public NoIndexFacetManager() {
        super();
    }

    @Override
    public void initialize() {
        LOG.info("Initialize facet manager.");
        for (IFacetCounter fc : facetCounters) {
            fc.initialize();
        }
    }

    @Override
    protected OpenBitSet[] getResultBitsets(IngridHits hits, IngridQuery query) {
        return new OpenBitSet[] { FacetUtils.createOpenBitsetWithCardinality(hits.length()) };
    }

}
