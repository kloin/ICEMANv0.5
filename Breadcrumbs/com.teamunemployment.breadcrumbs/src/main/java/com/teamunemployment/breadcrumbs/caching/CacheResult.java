package com.teamunemployment.breadcrumbs.caching;

/**
 * @author Josiah Kendall.
 * Simple class to return the result of a request to the cache. This object is used to handle
 * requests where we may want to update a request if the cache is past a certain age limit. Therefore
 * we need to return an object, not just the String results of the cache.
 */
public class CacheResult {

    public boolean requiresUpdate =false;
    public String result;

    public CacheResult(String result, boolean requiresUpdate) {
        this.requiresUpdate = requiresUpdate;
        this.result = result;
    }


}
