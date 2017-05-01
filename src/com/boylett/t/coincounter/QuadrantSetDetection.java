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
    private static final int MINIMUM_CONTOUR_LENGTH = 10;
    private static final double STRAIGHT_THRESH = 1.5;
    
    private QuadrantSet qs = new QuadrantSet();

    public QuadrantSetDetection() {
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
    public QuadrantSet findQuadrantSet(Mat img) {
        // Find half sets
        HalfSetDetection hsd = new HalfSetDetection();
        hsd.detectArcHalfSets(img);
        List<MatOfPoint> cntII_IV = hsd.getContoursII_IV();
        List<MatOfPoint> cntI_III = hsd.getContoursI_III();

        // Filter arcs
        filterCandidateArcs(cntII_IV);
        filterCandidateArcs(cntI_III);

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
     * @param halfSet 
     */
    private void filterCandidateArcs(List<MatOfPoint> halfSet) {
        FilterContour.length(halfSet, MINIMUM_CONTOUR_LENGTH);

        // Simplify contours into 3 points
        List<KeyPoints> kpHalfSet = KeyPoints.createKeyPointList(halfSet);
        
        FilterContour.straightness(kpHalfSet, STRAIGHT_THRESH);

        halfSet.clear();

        halfSet.addAll(kpHalfSet);
    }
}
