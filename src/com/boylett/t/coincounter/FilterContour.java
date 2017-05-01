package com.boylett.t.coincounter;

import java.util.List;
import java.util.ListIterator;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

/**
 * Provides static methods for filtering contours based on different criteria.
 */
public class FilterContour {
    
    /**
     * Removes contours from a list with a length less than minLength.<br>
     * This method modifies the original list.<br>
     * <br>
     * The length is approximated by the number of points in the contour.
     * 
     * @param contours List of contours
     * @param minLength Minimum length required
     */
    public static void length(List<MatOfPoint> contours, int minLength) {
        ListIterator<MatOfPoint> it = contours.listIterator();
        while (it.hasNext()) {
            MatOfPoint contour = it.next();
            if (contour.rows() < minLength) {
                it.remove();
            }
        }
    }
    
    /**
     * Removes contours from a list which are too straight.
     * Contours must only contain their key points meaning
     * their start, middle and end.<br>
     * <br>
     * The area of the contour is divided my its length. Approximately, the
     * closer this value is to 1, the closer the points are to being co-linear.
     * straightThresh is how close this value needs to be to 1.
     * 
     * @param keyPointList List of contours containing three points - start, middle, end.
     * @param straightThresh Straightness threshold
     */
    public static void straightness(List<KeyPoints> keyPointList, double straightThresh) {
        for (int i = 0; i < keyPointList.size(); i++) {
            double straightness
                    = Imgproc.contourArea(keyPointList.get(i))
                    / Imgproc.arcLength(new MatOfPoint2f(keyPointList.get(i).toArray()), false);
            if (Math.abs(straightness - 1) < straightThresh) {
                keyPointList.remove(i);
                i--;
            }
        }
    }
}
