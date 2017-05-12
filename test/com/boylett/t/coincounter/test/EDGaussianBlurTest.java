/*
 * Demonstrates gaussian blur
 */
package com.boylett.t.coincounter.test;

import com.boylett.t.coincounter.EllipseDetection;
import com.boylett.t.coincounter.HalfSetDetection;
import java.lang.reflect.Method;
import org.opencv.core.Mat;
import org.opencv.core.Size;

public class EDGaussianBlurTest {
    
    public static class EDGaussianBlur implements WebcamDriver.Process {
        @Override
        public Mat process(Mat m) {
            HalfSetDetection hsd = new HalfSetDetection.Builder().build();
            
            try {
                Method removeNoise = null;
                removeNoise = hsd.getClass().getDeclaredMethod("removeNoise", Mat.class, Size.class);
                removeNoise.setAccessible(true);
                m = (Mat) removeNoise.invoke(hsd, m, new Size());
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
