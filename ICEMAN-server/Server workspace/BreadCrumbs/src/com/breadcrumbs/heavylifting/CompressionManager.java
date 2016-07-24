// Copyright BreadcrumbsApp 2016
package com.breadcrumbs.heavylifting;

import com.breadcrumbs.heavylifting.Compression.CompressVideo;
import com.breadcrumbs.heavylifting.Compression.CompressionContract;
import com.breadcrumbs.heavylifting.Compression.SynchronizedCompressionBacklog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Multiple threads need to access the list of of files - If we have multiple people
 * saving multiple videos at the same time, multiple threads will need to access the bit
 * of memory that holds the arraylist.
 * @author Josiah Kendall
 */
public class CompressionManager implements Runnable {
    
    BlockingQueue<String> queue;
    CompressionContract contract;

    public CompressionManager(CompressionContract contract, BlockingQueue<String> queue) {
        this.queue = queue;
        this.contract = contract;
    }

    @Override
    public void run() {
        while (queue.size() > 0) {
            try {
                String id = queue.take();
                contract.Compress(id);
            } catch(InterruptedException ex) {
                System.out.println("Error taking item to compress");
                ex.fillInStackTrace();
            }
        }
        
        SynchronizedCompressionBacklog.GetInstance().setCompressing(false);
    }
    
}
