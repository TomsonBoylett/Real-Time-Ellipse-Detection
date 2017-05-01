/*
 * Demonstrates gaussian blur
 */
package com.boylett.t.coincounter.test;

import com.boylett.t.coincounter.EllipseDetection;
import java.lang.reflect.Method;
import org.opencv.core.Mat;

public class EDGaussianBlurTest {
    
    public static class EDGaussianBlur implements WebcamDriver.Process {
        @Override
        public Mat process(Mat m) {
            EllipseDetection ed = new EllipseDetection();
            
            try {
                Method removeNoise = null;
                removeNoise = ed.getClass().getDeclaredMethod("removeNoise", Mat.class);
                removeNoise.setAccessible(true);
                m = (Mat) removeNoise.invoke(ed, m);
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
        wd.getProcessList().add(new EDGaussianBlur());;
        
        // Start webcam driver
        wd.start();
    }
}
