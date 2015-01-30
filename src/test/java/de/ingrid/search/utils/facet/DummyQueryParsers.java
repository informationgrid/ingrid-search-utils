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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.search.utils.IQueryParser;
import de.ingrid.search.utils.IQueryParsers;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.IngridQuery;

public class DummyQueryParsers implements IQueryParsers {

    private static Logger log = Logger.getLogger(DummyQueryParsers.class);

    private List<IQueryParser> _queryParsers;

    public DummyQueryParsers() {
        _queryParsers = new ArrayList<IQueryParser>();
    }

    public void setQueryParsers(List<IQueryParser> parsers) {
        this._queryParsers = parsers;
    }

    public Query parse(IngridQuery ingridQuery) {
        BooleanQuery booleanQuery = new BooleanQuery();
        ClauseQuery[] clauses = ingridQuery.getClauses();
        for (ClauseQuery clauseQuery : clauses) {
            final Query sc = parse(clauseQuery);
            if (!sc.equals(new BooleanQuery())) {
                Occur occur = transform(clauseQuery.isRequred(), clauseQuery.isProhibited());
                booleanQuery.add(sc, occur);
            }
        }
        parse(ingridQuery, booleanQuery);
        return booleanQuery;
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        if (log.isDebugEnabled()) {
            log.debug("incoming ingrid query:" + ingridQuery.toString());
        }
        for (IQueryParser queryParser : _queryParsers) {
            if (log.isDebugEnabled()) {
                log.debug("incoming boolean query:" + booleanQuery.toString());
            }
            queryParser.parse(ingridQuery, booleanQuery);
            if (log.isDebugEnabled()) {
                log.debug(queryParser.toString() + ": resulting boolean query:" + booleanQuery.toString());
            }
        }
    }

    private Occur transform(boolean required, boolean prohibited) {
        Occur occur = null;
        if (required) {
            if (prohibited) {
                occur = Occur.MUST_NOT;
            } else {
                occur = Occur.MUST;
            }
        } else {
            if (prohibited) {
                occur = Occur.MUST_NOT;
            } else {
                occur = Occur.SHOULD;
            }
        }
        return occur;
    }    
    
    
}
