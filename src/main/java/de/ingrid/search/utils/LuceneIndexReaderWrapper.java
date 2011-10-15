/**
 * 
 */
package de.ingrid.search.utils;

import org.apache.lucene.index.IndexReader;

/**
 * @author joachim
 * 
 */

public class LuceneIndexReaderWrapper {

    private volatile IndexReader[] indexReader;
    
    public LuceneIndexReaderWrapper() {
        
    }

    public LuceneIndexReaderWrapper(IndexReader[] indexReader) {
        this.indexReader = indexReader;
    }

    public IndexReader[] getIndexReader() {
        return indexReader;
    }

    public void setIndexReader(IndexReader[] indexReader) {
        this.indexReader = indexReader;
    }

}
