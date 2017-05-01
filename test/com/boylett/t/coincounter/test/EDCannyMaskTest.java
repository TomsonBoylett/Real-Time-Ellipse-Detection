/*
 * Demonstrates canny edge detection with gradient filtering
 */
package com.boylett.t.coincounter.test;

import com.boylett.t.coincounter.EllipseDetection;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class EDCannyMaskTest {
    
    public static class EDMask implements WebcamDriver.Process {
        @Override
        public Mat process(Mat m) {
            EllipseDetection ed = new EllipseDetection(m);
            
            try {    
                Mat canny = new EDCannyTest.EDCanny().process(m);
                
                
                Mat mask = new Mat();
                m.copyTo(mask);
                mask = new EDGradMaskTest.EDGradMask().process(mask);
                
                Core.bitwise_and(canny, mask, m);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            
            return m;
        }
    }
    
    public static void main(String args[]) {
        // Get webcam driver
        WebcamDriver wd = WebcamDriver.getSingleton();
        
        // Add image processes
        wd.getProcessList().add(new EDGaussianBlurTest.EDGaussianBlur());
        wd.getProcessList().add(new EDMask());
        
        // Start webcam driver
        wd.start();
    }
}
