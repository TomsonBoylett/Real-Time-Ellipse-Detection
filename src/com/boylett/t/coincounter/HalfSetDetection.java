package com.boylett.t.coincounter;

import com.atul.JavaOpenCV.ImShow;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_8UC1;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * As part of ellipse detection we detect and group contours into
 * two half sets II,IV and I,III.<br>
 * <br>
 * Related:<br>
 * http://i.imgur.com/RZJHVQ2.png
 *
 * @author tomson
 */
public class HalfSetDetection {
    private static final int SOBEL_DEPTH = CV_16S;
    private static final int CONT_RETR_MODE = Imgproc.RETR_LIST;
    private static final int CONT_CHAIN_APPROX = Imgproc.CHAIN_APPROX_NONE;

    private List<MatOfPoint> contoursII_IV = null;
    private List<MatOfPoint> contoursI_III = null;
    private Size ksize;
    private int cannyLower;
    private int cannyUpper;
    
    private HalfSetDetection(){}
    
    private HalfSetDetection(Size ksize, int cannyLower, int cannyUpper) {
        this.ksize = ksize;
        this.cannyLower = cannyLower;
        this.cannyUpper = cannyUpper;
    }
    
    public static class Builder {
        private static final Size KSIZE = new Size(5, 5); // For gaussian blur
        private static final int CANNY_LOWER_THRESH = 50;
        private static final int CANNY_UPPER_THRESH = 150;
    
        private Size ksize;
        private int cannyLower;
        private int cannyUpper;
        
        public Builder() {
            setToDefault();
        }
        
        public final Builder setToDefault() {
            ksize = KSIZE;
            cannyLower = CANNY_LOWER_THRESH;
            cannyUpper = CANNY_UPPER_THRESH;
            return this;
        }
        
        public Builder setKSize(Size ksize) {
            if ((ksize.width  < 1 || ksize.width  % 2 == 0) ||
                (ksize.height < 1 || ksize.height % 2 == 0)) {
                throw new InvalidParameterException("ksize dimensions must be positive, odd integers");
            }
            this.ksize = ksize;
            return this;
        }
        
        public Builder setCannyLower(int cannyLower) {
            if (cannyLower < 0) {
                throw new InvalidParameterException("Canny lower threshold must be positive");
            }
            this.cannyLower = cannyLower;
            return this;
        }
        
        public Builder setCannyUpper(int cannyUpper) {
            if (cannyUpper < 0) {
                throw new InvalidParameterException("Canny upper threshold must be positive");
            }
            this.cannyUpper = cannyUpper;
            return this;
        }
        
        public HalfSetDetection build() {
            return new HalfSetDetection(ksize, cannyLower, cannyUpper);
        }
    }

    public List<MatOfPoint> getContoursII_IV() {
        if (contoursII_IV == null) {
            throw new IllegalStateException("Must call detectArcHalfSets(Mat img) first");
        }
        return contoursII_IV;
    }

    public List<MatOfPoint> getContoursI_III() {
        if (contoursI_III == null) {
            throw new IllegalStateException("Must call detectArcHalfSets(Mat img) first");
        }
        return contoursI_III;
    }
    
    /**
     * Detect and group contours into
     * two half sets II,IV and I,III.<br>
     * <br>
     * Related:<br>
     * http://i.imgur.com/RZJHVQ2.png
     * 
     * @param img RGB Image
     */
    public void detectArcHalfSets(Mat img) {
        if (img.channels() != 1 && img.channels() != 3) {
            throw new InvalidParameterException("Image must be greyscale or RGB");
        }
        if (img.dims() > 2 || img.rows() == 0 || img.cols() == 0) {
            throw new InvalidParameterException("Image has invalid dimensions");
        }
        if (img.depth() != CvType.CV_8U) {
            throw new InvalidParameterException("Image must have a bit depth of 8 and be unsigned");
        }
        
        // Remove noise
        Mat img2 = removeNoise(img, ksize);
        
        // Convert image to greyscale (if needed)
        if (img2.channels() == 3) {
            Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGB2GRAY);
        }

        // Create image mask that corresponds to pixels with a
        // positive sobel derivative gradient
        Mat mask = sobelDerivative(img2).generateMask();

        // Apply canny edge detection
        Mat canny = canny(img2);

        // Split up canny edges in 2 images using mask
        Mat cannyII_IV = new Mat();
        Mat cannyI_III = new Mat();
        splitImage(canny, mask, cannyII_IV, cannyI_III);

        // Find contours in each subsequent image
        contoursII_IV = findContours(cannyII_IV);
        contoursI_III = findContours(cannyI_III);
    }
    
    /**
     * Calculates the x and y derivative of each pixel.<br>
     * <br>
     * Related:<br>
     * http://docs.opencv.org/3.2.0/d2/d2c/tutorial_sobel_derivatives.html<br>
     * http://docs.opencv.org/3.2.0/d4/d86/group__imgproc__filter.html#gaa13106761eedf14798f37aa2d60404c9
     *
     * @param img
     * @return Mat array where 0 is gradX and 1 is gradY
     */
    private GradMat sobelDerivative(Mat img) {
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Scharr(img, gradX, SOBEL_DEPTH, 1, 0);
        Imgproc.Scharr(img, gradY, SOBEL_DEPTH, 0, 1);
        return new GradMat(gradX, gradY);
    }
    
    /**
     * Removes noise from image using Gaussian blur<br>
     * <br>
     * Related:<br>
     * http://docs.opencv.org/3.2.0/d4/d86/group__imgproc__filter.html#gaabe8c836e97159a9193fb0b11ac52cf1
     *
     * @param img
     * @return less noisy img
     */
    private Mat removeNoise(Mat img, Size ksize) {
        Mat noNoise = new Mat();
        Imgproc.GaussianBlur(img, noNoise, ksize, 0, 0);
        return noNoise;
    }
    
    /**
     * Applies canny edge detection.<br>
     * This function does not modify the source image. <br>
     * <br>
     * Related:<br>
     * http://docs.opencv.org/3.2.0/da/d22/tutorial_py_canny.html<br>
     * http://docs.opencv.org/3.2.0/da/d5c/tutorial_canny_detector.html<br>
     * http://docs.opencv.org/3.2.0/dd/d1a/group__imgproc__feature.html#ga04723e007ed888ddf11d9ba04e2232de
     * 
     * @param m Source image
     * @return A binary image highlighting edges
     */
    private Mat canny(Mat m) {
        Mat edges = new Mat();
        Imgproc.Canny(m, edges, cannyLower, cannyUpper);
        return edges;
    }
    
    /**
     * Takes a source image, bit ANDs it with a mask and stores the result in
     * dst1 and bit NANDs it with the mask and stores the result in dst2. <br>
     * This results in the source image effectively being split in two. <br>
     * <br>
     * Related:<br>
     * http://docs.opencv.org/3.2.0/d2/de8/group__core__array.html#ga60b4d04b251ba5eb1392c34425497e14
     * 
     * @param src Source image
     * @param mask Binary mask
     * @param dst1 Bit AND destination image
     * @param dst2  Bit NAND destination image
     */
    private void splitImage(Mat src, Mat mask, Mat dst1, Mat dst2) {
        Core.bitwise_and(src, mask, dst1);
        Core.bitwise_not(mask, mask); // Reverse mask for the 2nd image
        Core.bitwise_and(src, mask, dst2);
    }
    
    /**
     * Uses edges previously found by canny to find contours in an image. In
     * each contour only the row dimension is used.<br>
     * <br>
     * Related:<br>
     * http://docs.opencv.org/3.2.0/d3/dc0/group__imgproc__shape.html#ga17ed9f5d79ae97bd4c7cf18403e1689a
     *
     * @param edges Output from canny edge detection
     * @return contours
     */
    private List<MatOfPoint> findContours(Mat edges) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
                edges, contours, hierarchy, CONT_RETR_MODE, CONT_CHAIN_APPROX);
        return contours;
    }
}
