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

import java.util.StringTokenizer;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.search.utils.IQueryParser;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

/**
 * Maps TermQuery(s) from IngridQuery to LuceneQuery.
 */
public class DummyTermQueryParser implements IQueryParser {

    private final String _field;
    private Occur _occur;

    public DummyTermQueryParser(String field, Occur occur) {
        _field = field;
        _occur = occur;
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        TermQuery[] ingridTerms = ingridQuery.getTerms();
        for (TermQuery ingridTermQuery : ingridTerms) {
            if (ingridTermQuery == null) {
                continue;
            }
            String value = ingridTermQuery.getTerm();
            if (value == null) {
                continue;
            }
            value = value.toLowerCase();

            // how to add new query to boolean query
            Occur occur = null;
            if (_occur != null) {
                occur = _occur;
            } else {
                occur = transform(ingridTermQuery.isRequred(), ingridTermQuery.isProhibited());
            }

            // create new query and add to boolean query
            if (value.indexOf(" ") > -1) {
            	// add PhraseQuery
            	addPhraseQuery(booleanQuery, value, occur);

            } else {
                if (value.endsWith("*")) {
                	addPrefixQuery(booleanQuery, value, occur);

                } else {
                	addTermQuery(booleanQuery, value, occur);
                }
            }
        }
        
    }

    private void addPhraseQuery(BooleanQuery booleanQuery, String value, Occur occur) {
    	// add PhraseQuery
        PhraseQuery phraseQuery = new PhraseQuery();
        StringTokenizer tokenizer = new StringTokenizer(value);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            // Filtering with StandardAnalyzer e.g. to remove "*" at end !
            final String filteredTerm = token;
            phraseQuery.add(new Term(_field, filteredTerm));
        }
        if (phraseQuery.getTerms().length > 0) {
            booleanQuery.add(phraseQuery, occur);
        }
    }

    private void addPrefixQuery(BooleanQuery booleanQuery, String value, Occur occur) {
        if (value.endsWith("*")) {
        	// remove "*"
            value = value.substring(0, value.length() - 1);
        }

    	// add PrefixQuery
        Term term = new Term(_field, value);
        PrefixQuery prefixQuery = new PrefixQuery(term);
        booleanQuery.add(prefixQuery, occur);            	
    }

    private void addTermQuery(BooleanQuery booleanQuery, String value, Occur occur) {

        // filter and use phrase like in former AbstractSearcher ? NO ...
        if (value.indexOf(" ") > -1) {
            addPhraseQuery(booleanQuery, value, occur);
            return;
        } else {
            Term term = new Term(_field, value);
            org.apache.lucene.search.TermQuery termQuery = new org.apache.lucene.search.TermQuery(term);
            booleanQuery.add(termQuery, occur);
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
