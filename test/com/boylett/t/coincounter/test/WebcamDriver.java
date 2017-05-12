/*
 * Provides an interface for running image processing
 * repeatedly on a webcam stream.
 */
package com.boylett.t.coincounter.test;

import com.atul.JavaOpenCV.ImShow;
import com.github.sarxos.webcam.Webcam;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author tomson
 */
public class WebcamDriver {
    
    private static final WebcamDriver INSTANCE = new WebcamDriver();
    
    public static WebcamDriver getSingleton() {
        return INSTANCE;
    }
    
    // List of processes to be execute on each frame of webcam
    private List<Process> processes = new ArrayList<>();
    
    private WebcamDriver(){};
    
    /**
     * A process takes a Mat, processes it in some way and returns a new Mat.
     */
    public interface Process {
        public Mat process(Mat m);
    }
    
   /**
    * @return process list
    */
    public List getProcessList() {
        return processes;
    }
    
    /**
     * Starts webcam stream
     */
    public void start() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Webcam webcam = Webcam.getWebcams().get(0);
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();
        ImShow im = new ImShow("WebcamMain");
        while (true) {
            Mat m = bufToMat(webcam.getImage());
            for (Process p : processes) {
                m = p.process(m);
            }
            im.showImage(m);
        }
    }
    
    /**
     * Converts 3 channel 8 bit BufferedImage to a Mat
     * 
     * @param bi - Java RGB Buffered Image
     * @return RGB OpenCV Mat
     */
    public Mat bufToMat(BufferedImage bi) {
        // Convert buffered image to byte data
        byte[] bData = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        
        // Create 3 BGR mat the same size as buffered image
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        
        // Copy over data
        mat.put(0, 0, bData);
        
        // Convert mat to RGB
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        
        return mat;
    }
}
