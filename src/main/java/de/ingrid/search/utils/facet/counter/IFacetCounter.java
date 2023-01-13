/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
    
    /**
     * Initializes the facet counter. Clears all caches and stored facet classes.
     * 
     */
    public void initialize();

}
