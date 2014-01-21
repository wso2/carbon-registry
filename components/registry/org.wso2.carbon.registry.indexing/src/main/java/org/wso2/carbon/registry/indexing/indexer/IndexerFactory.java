package org.wso2.carbon.registry.indexing.indexer;

import org.wso2.carbon.registry.indexing.RegistryConfigLoader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class IndexerFactory {

    private static IndexerFactory factory;

    protected IndexerFactory() {
    }

    public static IndexerFactory getFactory() {
        if (factory == null) {
            factory = new IndexerFactory();
        }
        return factory;
    }

/*
We are assuming that indexers cannot be added dynamically
    public void addIndexer(String mediaType, Indexer indexer) {
        indexerMap.put(mediaType, indexer);
    }
*/

    //Moved to IndexinManager class
    /*public Indexer getIndexer(String mimeType) throws IndexerException {

        Indexer indexer = null;
        Iterator<String> iterator = indexerMap.keySet().iterator();

        while (iterator.hasNext()) {
            String mediaTypeRegEx = iterator.next();
            if (Pattern.matches(mediaTypeRegEx, mimeType)) {
               // try {
                    //return indexerMap.get(mediaTypeRegEx).getClass().newInstance(); //TODO: Reuse existing ones
                    return indexerMap.get(mediaTypeRegEx);
               // } catch (InstantiationException e) {
               //     throw new IndexerException("Unable to create Indexer instance", e);
               // } catch (IllegalAccessException e) {
               //     throw new IndexerException("Unable to create Indexer instance", e);
               // }
            }
        }
        return indexer;
    }*/


}
