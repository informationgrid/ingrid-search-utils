package de.ingrid.search.utils.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

/**
 * The FacetRegistry manages all FacetClasses inside a Cache, in case the number
 * gets so big, that parts of the cached objects can be stored on the disk
 * instead of in-memory.
 */
public class FacetClassRegistry {

    private static String FACET_CACHE_NAME = "facetClassCache";

    private static Logger LOG = Logger.getLogger(FacetClassRegistry.class);

    /**
     * Contains all FacetClass-names (used in cache) for those facets, which
     * were queried without any FacetClasses.
     */
    private Map<String, List<String>> _facetClassMap;

    // private Map<String, FacetClass> _facetClasses;

    private CacheManager _cacheManager;

    private Cache _cache;

    /**
     * @element-type FacetClass
     */
    private FacetClassProducer _facetClassProducer;

    public FacetClassRegistry() {
        // _facetClasses = new HashMap<String, FacetClass>();
        _facetClassMap = new HashMap<String, List<String>>();

        _cacheManager = new CacheManager();

        if (_cacheManager.cacheExists(FACET_CACHE_NAME)) {
            _cache = _cacheManager.getCache(FACET_CACHE_NAME);
        } else {
            _cache = setupDefaultCache();
            _cacheManager.addCache(_cache);
        }
    }

    /**
     * Set up a cache for storing the created bitsets.
     * 
     * @return a configured Cache
     */
    private Cache setupDefaultCache() {
        /*
         * CacheConfiguration cc = new CacheConfiguration();
         * cc.setMemoryStoreEvictionPolicyFromObject
         * (MemoryStoreEvictionPolicy.LFU); cc.setOverflowToDisk(true);
         * cc.setEternal(false); cc.setTimeToLiveSeconds(60);
         * cc.setTimeToIdleSeconds(30); cc.setDiskPersistent(false);
         * cc.setDiskExpiryThreadIntervalSeconds(0);
         */
        return new Cache(FACET_CACHE_NAME, 500, // max elements in cache
                true, // overflow to disk
                true, // eternal, if yes then it never expires (overrides
                // timeToLive and timeToIdle!)
                0, // time to live in seconds
                0 // time to idle in seconds
        );

    }

    public void clear() {
        _facetClassMap.clear();
        _cache.removeAll();
    }

    /**
     * Return all FacetClasses according to a facet definition. If one class
     * does not exist yet, it will be produced by the FacetClassProducer. If no
     * class was given, then all classes will be queried from the index.
     */
    public List<FacetClass> getFacetClasses(FacetDefinition facDef) {
        List<FacetClass> fClasses = new ArrayList<FacetClass>();

        // if no classes have been specified then check the cache or
        // find all values to this facet and put them into the cache
        if (facDef.getClasses() == null) {
            fClasses.addAll(getFacetClassesFromCacheOrCreate(facDef));
        } else {
            for (FacetClassDefinition fcDef : facDef.getClasses()) {
                fClasses.add(getFacetClassFromCacheOrCreate(fcDef));
            }
        }
        return fClasses;
    }

    private List<FacetClass> getFacetClassesFromCacheOrCreate(FacetDefinition facDef) {
        List<FacetClass> facetClasses = null;
        List<String> clazzes = _facetClassMap.get(facDef.getName());
        // if classes must have been produced already
        if (clazzes != null) {
            facetClasses = new ArrayList<FacetClass>();
            for (String facetClassName : clazzes) {
                long start = 0;
                if (LOG.isDebugEnabled()) {
                    start = System.currentTimeMillis();
                }
                facetClasses.add(getFromCache(facetClassName));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Get facet class from cache: " + facetClasses.get(facetClasses.size() - 1) + " in "
                            + (System.currentTimeMillis() - start) + " ms.");
                }
            }
        } else {
            long start = 0;
            if (LOG.isInfoEnabled()) {
                start = System.currentTimeMillis();
            }
            // otherwise produce them now
            facetClasses = _facetClassProducer.produceClasses(facDef);
            // add new dynamically created classes to map so we can remember all
            // classes belonging to a generic facet
            addClassesToCacheAndMap(facDef.getName(), facetClasses);
            if (LOG.isInfoEnabled()) {
                LOG.info("Produce " + facetClasses.size() + " facet classes for facet '" + facDef.getName()
                        + "' and place them into cache within: " + (System.currentTimeMillis() - start) + " ms.");
            }

        }
        return facetClasses;
    }

    private void addClassesToCacheAndMap(String facetName, List<FacetClass> clazzes) {
        List<String> classNames = new ArrayList<String>();
        for (FacetClass facetClass : clazzes) {
            // put facet class into cache
            addToCache(facetClass.getFacetClassName(), facetClass);

            // remember name of facet class
            classNames.add(facetClass.getFacetClassName());
        }

        // remember all facet classes names to a facet for later caching
        _facetClassMap.put(facetName, classNames);
    }

    private FacetClass getFacetClassFromCacheOrCreate(FacetClassDefinition fcDef) {
        // check if facet class is already in cache
        FacetClass fc = getFromCache(fcDef.getName());
        // we have to produce the facet class
        if (fc == null) {
            fc = _facetClassProducer.produceClass(fcDef);
            // remember facet class in cache
            addToCache(fcDef.getName(), fc);
        }
        return fc;
    }

    private FacetClass getFromCache(String key) {
        Element element = _cache.get(key);
        if (element == null) {
            return null;
        } else {
            return (FacetClass) element.getValue();
        }
    }

    public FacetClassProducer getFacetClassProducer() {
        return _facetClassProducer;
    }

    public void setFacetClassProducer(FacetClassProducer facetClassProducer) {
        _facetClassProducer = facetClassProducer;
    }

    private void addToCache(String key, FacetClass fc) {
        _cache.put(new Element(key, (Serializable) fc));
        // System.out.println("Disk store size: " + _cache.getDiskStoreSize() +
        // " and memory store size: " + _cache.getMemoryStoreSize());
        // System.out.println("Memory size: " + _cache.calculateInMemorySize());
    }
}