package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import org.opencv.core.CvType;
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
        setGradXY(gradX, gradY);
    }
    
    /**
     * Creates mask that splits up image based on gradient angle.
     * Specifically it returns a mask which corresponds to pixels
     * with a positive gradient.
     * 
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
    
    public final void setGradXY(Mat gradX, Mat gradY) {
        checkGrad(gradX);
        checkGrad(gradY);
        if (gradX.rows() != gradY.rows() ||
            gradX.cols() != gradY.cols()) {
            throw new InvalidParameterException("Gradient images must be the same size");
        }
        this.gradX = gradX;
        this.gradY = gradY;
    }

    public Mat getGradY() {
        return gradY;
    }
    
    private void checkGrad(Mat grad) {
        if (grad.channels() != 1) {
            throw new InvalidParameterException("Gradent image must have 1 channel");
        }
        if (grad.dims() > 2 || grad.rows() == 0 || grad.cols() == 0) {
            throw new InvalidParameterException("Gradent image has invalid dimensions");
        }
        if (grad.depth() != CvType.CV_16S) {
            throw new InvalidParameterException("Image must have a bit depth of 16 and be signed");
        }
    }
}
