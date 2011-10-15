/**
 * 
 */
package de.ingrid.search.utils.facet;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * @author joachim
 *
 */
public class DummyIndex {

    static Object[][][] IndexDef = {
            {
                // 1
                {"partner", "bund", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"provider", "bund_1", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"datatype", "iso", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"metaclass", "1", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"content", "Die Geschichte der menschlichen Nutzung des Wassers", Field.Store.NO, Field.Index.ANALYZED},
                {"title", "Wasser", Field.Store.NO, Field.Index.ANALYZED},
            },
            {
                // 2
                {"partner", "bund", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"provider", "bund_1", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"datatype", "iso", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"metaclass", "1", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"content", "und somit jene der Hydrologie, der Wasserwirtschaft und besonders", Field.Store.NO, Field.Index.ANALYZED},
                {"title", "Wasser", Field.Store.NO, Field.Index.ANALYZED},
            },
            {
                // 3
                {"partner", "bund", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"provider", "bund_2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"datatype", "iso", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"metaclass", "1", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"content", "one side, the bursting of the housing", Field.Store.NO, Field.Index.ANALYZED},
                {"title", "Wasser", Field.Store.NO, Field.Index.ANALYZED},
            },
            {
                // 4
                {"partner", "bund", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"provider", "bund_2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"datatype", "iso", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"metaclass", "2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"content", "der Antike über das Mittelalter bis zur Neuzeit stand im Zentrum immer ein Konflikt zwischen einem zu viel und einem zu wenig an Wasser", Field.Store.NO, Field.Index.ANALYZED},
                {"title", "Wasser", Field.Store.NO, Field.Index.ANALYZED},
            },
            {
                // 5
                {"partner", "bund", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"provider", "bund_2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"datatype", "iso", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"metaclass", "2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"content", "heute kommt dem Wasser in den meisten Religionen der Welt eine Sonderstellung", Field.Store.NO, Field.Index.ANALYZED},
                {"title", "Wasser", Field.Store.NO, Field.Index.ANALYZED},
            },
            {
                // 6
                {"partner", "ni", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"provider", "ni_2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"content", "one side, the bursting of the housing", Field.Store.NO, Field.Index.ANALYZED},
            },
            {
                // 7
                {"partner", "ni", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"provider", "ni_2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"datatype", "csw", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"metaclass", "2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"content", "heute kommt dem Wasser in den meisten Religionen der Welt eine Sonderstellung", Field.Store.NO, Field.Index.ANALYZED},
                {"title", "Wasser", Field.Store.NO, Field.Index.ANALYZED},
            },
            {
                // 7
                {"partner", "ni", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"provider", "ni_2", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"datatype", "csw", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"metaclass", "3", Field.Store.YES, Field.Index.NOT_ANALYZED},
                {"content", "heute kommt dem Wasser in den meisten Religionen der Welt eine Sonderstellung", Field.Store.NO, Field.Index.ANALYZED},
                {"title", "Wasser", Field.Store.NO, Field.Index.ANALYZED},
            },
    };
    
    
    private static File createTestIndex() {
        File indexDirectory = new File("./test_index");
        if (!indexDirectory.exists()) {
            try {
                IndexWriter writer = new IndexWriter(FSDirectory.getDirectory(indexDirectory), new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
                for (Object[][] doc : IndexDef) {
                    Document document = new Document();
                    for (Object[] fields: doc) {
                        document.add(new Field((String)fields[0], (String)fields[1], (Field.Store)fields[2], (Field.Index)fields[3]));
                    }
                    writer.addDocument(document);
                }
                writer.close();
            } catch (CorruptIndexException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (LockObtainFailedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return indexDirectory;
    }
    
    public static File getTestIndex() {
        File indexDir = new File("src/test/resources/index-big");
        if (!indexDir.exists()) {
            indexDir = createTestIndex();
        }
        return indexDir;
        
    }
    
}
