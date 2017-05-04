/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import static org.opencv.core.CvType.CV_8UC1;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author tomson
 */
public class QuadrantSetDetection {
    private int minContLen;
    private double straightThresh;
    private HalfSetDetection hsd;

    private QuadrantSetDetection() {}
    
    private QuadrantSetDetection(int minContLen, double straightThresh, HalfSetDetection hsd) {
       this.minContLen = minContLen;
       this.straightThresh = straightThresh;
       this.hsd = hsd;
    }
    
    public static class Builder {
        private static final int MINIMUM_CONTOUR_LENGTH = 10;
        private static final double STRAIGHT_THRESH = 1.5;
        
        private int minContLen;
        private double straightThresh;
        private HalfSetDetection hsd;
        
        public Builder() {
            setToDefault();
        }
        
        public final Builder setToDefault() {
            minContLen = MINIMUM_CONTOUR_LENGTH;
            straightThresh = STRAIGHT_THRESH;
            hsd = new HalfSetDetection.Builder().build();
            return this;
        }
        
        public Builder setMinContLen(int minContLen) {
            if (minContLen < KeyPoint.LENGTH) {
                throw new InvalidParameterException("Minimum contour length must be atleast " + KeyPoint.LENGTH);
            }
            this.minContLen = minContLen;
            return this;
        }

        public Builder setStraightThresh(double straightThresh) {
            if (straightThresh < 0) {
                throw new InvalidParameterException("Straightness threshold must be positive");
            }
            this.straightThresh = straightThresh;
            return this;
        }
        
        public Builder setHalfSetDetection(HalfSetDetection hsd) {
            this.hsd = hsd;
            return this;
        }
        
        public QuadrantSetDetection build() {
            return new QuadrantSetDetection(minContLen, straightThresh, hsd);
        }
    }
    
    /**
     * As part of the ellipse detection we first have to find 4 candidate arcs.
     * This function returns an object containing contours corresponding to
     * the four quadrants of an ellipse.<br>
     * <br>
     * Related:<br>
     * http://i.imgur.com/RZJHVQ2.png
     * 
     * @param img image
     * @return Quadrant set
     */
    public QuadrantSet detect(Mat img) {
        if (img.channels() != 1 && img.channels() != 3) {
            throw new InvalidParameterException("Image must be greyscale or RGB");
        }
        if (img.dims() > 2 || img.rows() == 0 || img.cols() == 0) {
            throw new InvalidParameterException("Image has invalid dimensions");
        }
        if (img.depth() != CvType.CV_8U) {
            throw new InvalidParameterException("Image must have a bit depth of 8 and be unsigned");
        }
        // Find half sets
        hsd.detectArcHalfSets(img);
        List<MatOfPoint> cntII_IV = hsd.getContoursII_IV();
        List<MatOfPoint> cntI_III = hsd.getContoursI_III();

        // Filter arcs
        filterCandidateArcs(cntII_IV);
        filterCandidateArcs(cntI_III);
        
        QuadrantSet qs = new QuadrantSet();

        // Split half sets into quadrant sets;
        splitContoursByPixelCount(cntII_IV, qs.getArcIV(), qs.getArcII());
        splitContoursByPixelCount(cntI_III, qs.getArcIII(), qs.getArcI());

        return qs;
    }
    
    /**
     * Splits contours that come from the top half or bottom half of an
     * ellipse. This is achieved by counting the number of pixels above and
     * below the contour.<br>
     * <br>
     * Related:<br>
     * http://i.imgur.com/LXWSTJz.png
     * 
     * @param src Contours to be split
     * @param dst1 Destination for contours with more pixels above.
     * @param dst2 Destination for contours with more pixels below (or equal).
     */
    private void splitContoursByPixelCount(List<MatOfPoint> src,
            List<MatOfPoint> dst1, List<MatOfPoint> dst2) {
        for (MatOfPoint contour : src) {
            if (morePixelsAbove(contour)) {
                dst1.add(contour);
            } else {
                dst2.add(contour);
            }
        }
    }
    
    /**
     * Returns true if there are more pixels above a contour and returns
     * false otherwise.<br>
     * <br>
     * Related:<br>
     * http://i.imgur.com/LXWSTJz.png
     * 
     * @param contour
     * @return 
     */
    private boolean morePixelsAbove(MatOfPoint contour) {
        // Find bounding box
        // This is used to create a new mat the size of the contour
        Rect boundBox = Imgproc.boundingRect(contour);
        
        // Create mask that does nothing
        Mat mask = Mat.zeros(boundBox.height + 2, boundBox.width + 2, CV_8UC1);
             
        // Create mat the size of the bounding box all white
        Mat testImg = new Mat(boundBox.height, boundBox.width, CV_8UC1, Color.WHITE);
        
        // Translate the contour so it is centered
        // in the new mat and draw it in black
        Imgproc.polylines(testImg,
                Arrays.asList(translate(contour, 0 - boundBox.x, 0 - boundBox.y)),
                false, Color.BLACK, 1);

        // Count number of pixels above and below
        int pixelsAbove = countPixels(true, testImg, mask);
        int pixelsBelow = countPixels(false, testImg, mask);

        return pixelsAbove > pixelsBelow;
    }
    
    /**
     * @param above true if counting pixels above contour, false if counting below
     * @param img white image with single black contour
     * @param mask
     * @return number of pixels above or below
     */
    private int countPixels(boolean above, Mat img, Mat mask) {
        Mat copy = new Mat();
        img.copyTo(copy); 
        
        Point seed = new Point(
                img.width() / 2,
                above ? img.height() - 1 : 0
        );

        Imgproc.floodFill(copy, mask, seed, Color.BLACK);
        
        return Core.countNonZero(copy);
    }
    
    /**
     * 
     * @param contour contour to translate
     * @param x how far to translate in x direction
     * @param y how far to translate in y direction
     * @return 
     */
    private MatOfPoint translate(MatOfPoint contour, double x, double y) {
        List<Point> translated = new ArrayList<>();
        Point[] points = contour.toArray();
        for (Point point : points) {
            translated.add(new Point(point.x + x, point.y + y));
        }
        MatOfPoint toReturn = new MatOfPoint();
        toReturn.fromList(translated);
        return toReturn;
    }
    
    /**
     * Calls multiple methods that filter the contours
     * 
     * This method is destructive and reduces each contour in halfSet
     * to 3 points: start, middle and end.
     * 
     * @param halfSet 
     */
    private void filterCandidateArcs(List<MatOfPoint> halfSet) {
        FilterContour.length(halfSet, minContLen);

        // Simplify contours into 3 points
        List<MatOfPoint> kpHalfSet = KeyPoint.createKeyPointList(halfSet);
        
        FilterContour.straightness(kpHalfSet, straightThresh);
        
        halfSet.clear();
        
        halfSet.addAll(kpHalfSet);
    }
}
