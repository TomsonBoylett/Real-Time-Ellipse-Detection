package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import static org.opencv.core.CvType.CV_8UC1;
import org.opencv.core.Mat;

/**
 * Stores Sobel Derivatives in both the x and y direction.
 * Also provides a method to generate a mask from these derivatives.
 *
 */
public class GradMat {
    private Mat gradX;
    private Mat gradY;

    public GradMat(Mat gradX, Mat gradY) {
        setGradX(gradX);
        setGradY(gradY);
    }
    
    /**
     * Creates mask that splits up image based on gradient angle.
     * Specifically it returns a mask which corresponds to pixels
     * with a positive gradient.
     * 
     * @param gradX Matrix of gradients in the x direction
     * @param gradY Matrix of gradients in the y direction
     * @return binary mask
     */
    public Mat generateMask() {
        int pixels = gradX.cols()*gradX.rows();
        // Create mask initially set to all 0
        Mat mask = Mat.zeros(gradX.rows(), gradX.cols(), CV_8UC1);
        
        byte[] maskB = new byte[pixels];
        short[] gradXB = new short[pixels];
        short[] gradYB = new short[pixels];
        mask.get(0, 0, maskB);
        gradX.get(0, 0, gradXB);
        gradY.get(0, 0, gradYB);
        
        for (int i = 0; i < pixels; i++) {
            if ((gradXB[i] < 0) == (gradYB[i] < 0)) {
                maskB[i] = (byte) 255;
            }
        }
        
        mask.put(0, 0, maskB);
        return mask;
    }

    public Mat getGradX() {
        return gradX;
    }

    public void setGradX(Mat gradX) {
        if (gradX.channels() != 1) {
            throw new InvalidParameterException(
                    "Gradient images should only have 1 channel");
        }
        this.gradX = gradX;
    }

    public Mat getGradY() {
        return gradY;
    }

    public void setGradY(Mat gradY) {
        if (gradY.channels() != 1) {
            throw new InvalidParameterException(
                    "Gradient images should only have 1 channel");
        }
        this.gradY = gradY;
    }   
}
