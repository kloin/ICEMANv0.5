/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.heavylifting.Compression;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Singleton access to our backlog of items to compress.
 * @author jek40
 */
public class SynchronizedCompressionBacklog {
    private boolean COMPRESSION_FLAG = false;
    private static SynchronizedCompressionBacklog instance;
    private BlockingQueue<String> queue = new ArrayBlockingQueue(100);
    
    private SynchronizedCompressionBacklog() {
        
    }
    
    public static SynchronizedCompressionBacklog GetInstance() {
        if (instance == null) {
            instance = new SynchronizedCompressionBacklog();
        }
        return instance;
    }
    
    public BlockingQueue<String> GetQueue() {
        return queue;
    }
    
    public boolean isCompressing() {
        return COMPRESSION_FLAG;
    }
    
    public void setCompressing(boolean flag) {
        COMPRESSION_FLAG = flag;
    }
}
