/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.heavylifting.Compression;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jek40
 */
public class CompressVideo implements CompressionContract{
    
    /**
     * Compress a 1080p video into 720p, 640p, 450p, 360p, 280p
     * @param id The id of the video to compress.
     */
    @Override
    public void Compress(String id) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/var/lib/tomcat7/compress_single.sh", id, "640");
            Process p = pb.start();     // Start the process.
            p.waitFor();                // Wait for the process to finish.
            System.out.println("Finished 720 compression");
           // pb = new ProcessBuilder(
           //         "/var/lib/tomcat7/compress_single.sh", id, "640");
          //  p = pb.start();
            // Currently using wait for because the server this runs on is too shit to compress them all at once and im too poor to buy a better server
           // p.waitFor();
            System.out.println("Finished 640 compression");
            
            pb = new ProcessBuilder(
                    "/var/lib/tomcat7/compress_single.sh", id, "450");
            p = pb.start();     // Start the process.
            p.waitFor();                // Wait for the process to finish.
            pb = new ProcessBuilder(
                    "/var/lib/tomcat7/compress_single.sh", id, "360");
            p = pb.start();     // Start the process.
            p.waitFor();
            pb = new ProcessBuilder(
                    "/var/lib/tomcat7/compress_single.sh", id, "280");
            p = pb.start();
        } catch (InterruptedException ex) {
            System.out.println("Faile to compress due to interrupted exception");
            Logger.getLogger(CompressVideo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Failed to compress due to IO Exception");
            Logger.getLogger(CompressVideo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
