/*
 * Demonstrates generating a gradient mask
 */
package com.boylett.t.coincounter.test;

import com.boylett.t.coincounter.EllipseDetection;
import java.lang.reflect.Method;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class EDGradMaskTest {
    
    public static class EDGradMask implements WebcamDriver.Process {
        @Override
        public Mat process(Mat m) {
            EllipseDetection ed = new EllipseDetection(m);
            
            try {
                Method sobelDerivative = null;
                sobelDerivative = ed.getClass().getDeclaredMethod("sobelDerivative", Mat.class);
                sobelDerivative.setAccessible(true);
                Mat[] grad = (Mat[]) sobelDerivative.invoke(ed, m);
                
                Method gradMask = null;
                gradMask = ed.getClass().getDeclaredMethod("gradMask", Mat.class, Mat.class);
                gradMask.setAccessible(true);
                m = (Mat) gradMask.invoke(ed, grad[0], grad[1]);
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
        wd.getProcessList().add(new EDGradMask());
        
        // Start webcam driver
        wd.start();
    }
}
