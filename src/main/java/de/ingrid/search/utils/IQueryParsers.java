package de.ingrid.search.utils;

import org.apache.lucene.search.Query;

import de.ingrid.utils.query.IngridQuery;

public interface IQueryParsers {

    Query parse(IngridQuery ingridQuery);
}
