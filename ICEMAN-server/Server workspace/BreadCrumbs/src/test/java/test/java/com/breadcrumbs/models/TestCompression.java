/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.heavylifting.Compression.CompressVideo;
import com.breadcrumbs.heavylifting.Compression.CompressionContract;
import com.breadcrumbs.heavylifting.Compression.SynchronizedCompressionBacklog;
import com.breadcrumbs.heavylifting.CompressionManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author jek40
 */
public class TestCompression {
    
    @Test
    public void TestThatWeCanCompressVideo() {
        // File targetFile = new File("/var/lib/tomcat7/webapps/images/"+crumbId+".mp4");
         // Create the compressed versions of the  mp4 file.
//            String crumbId = "C:\\Users\\jek40\\Desktop\\ffmpeg\\ffmpeg-20160703-d5edb6c-win64-static\\bin\\21";
//        try {
//            System.out.println("Starting");
//            Process p = Runtime.getRuntime().exec("cmd /c C:\\Users\\jek40\\Desktop\\ffmpeg\\ffmpeg-20160703-d5edb6c-win64-static\\bin\\ffmpeg.exe -i "+crumbId+
//                    ".mp4 -c:v libx264 -filter:v scale=720:-1 -r 30 "+crumbId+".720.mp4 " +
//                    "-c:v libx264 -filter:v scale=640:-1 -r 30 "+crumbId+".640.mp4 " +
//                    "-c:v libx264 -filter:v scale=450:-1 -r 30 "+crumbId+".450.mp4 " +
//                    "-c:v libx264 -filter:v scale=360:-1 -r 30 "+crumbId+".360.mp4 " +
//                    "-c:v libx264 -filter:v scale=280:-1 -r 30 "+crumbId+".280.mp4");
//            
//            InputStream in = p.getErrorStream();
//            int c;
//            while ((c = in.read()) != -1) {
//                System.out.print((char)c);
//            }
//            in.close();
//            System.out.println("Finished");
//        } catch (IOException ex) {
//            System.out.println("SHit");
//            ex.fillInStackTrace();
//            Logger.getLogger(TestCompression.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    @Test
    public void TestThatCompressingCallsStartCompression() throws InterruptedException {
        final CompressionContract compressor = getSimpleCompressionContract();//Mockito.mock(CompressionContract.class);//getSimpleCompressionContract();
        BlockingQueue<String> ids = SynchronizedCompressionBacklog.GetInstance().GetQueue();
        
        ids.add("1");
        CompressionManager compressionManager = new CompressionManager(compressor, ids);
        new Thread(compressionManager).start();
       new Thread(new Runnable() {
            @Override
            public void run() {
                BlockingQueue<String> newIds = SynchronizedCompressionBacklog.GetInstance().GetQueue();
                newIds.add("5");
                newIds.add("4");
            }
        }).start();
        Thread.sleep(10000);


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                CompressionManager manager = CompressionManager.GetInstance(compressor);
//                manager.addId("1");
//                manager.addId("2");
//                manager.addId("3");
//            }
//        }).start();
//        
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                CompressionManager manager = CompressionManager.GetInstance(compressor);
//                manager.addId("4");
//                manager.addId("5");
//                manager.addId("6");
//            }
//        }).start();
        
    }
    
    @Test
    public void TestSecondItemCompression() throws InterruptedException {
          BlockingQueue<String> ids = SynchronizedCompressionBacklog.GetInstance().GetQueue();
            ids.add("2");
            ids.add("3");
    }
    
    private CompressionContract getSimpleCompressionContract() {
        return new CompressionContract() {
            @Override
            public void Compress(String id) {
                  try {
                        System.out.println("compressing: " + id);
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    Logger.getLogger(CompressVideo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }
}
