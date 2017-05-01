package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import math.geom2d.Point2D;
import math.geom2d.conic.Ellipse2D;
import math.geom2d.point.PointSet2D;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author tomson
 */
public class EllipseDetection {
    private static final double MAX_DISTANCE_FACTOR = 2.0;
    private static final double LIMIT = 0.1;
    private static final double PERIMETER_RATIO_MIN = 0.1;
    private static final double ELLIPSE_THRESH = 0.09;
    private static final int MIN_COUNT = 9;
    
    List<MatOfPoint> arcComb;
    List<RotatedRect> ellipses;
    QuadrantSet qs;
    Mat img;

    public EllipseDetection() {
        arcComb = new ArrayList<>();
    }

    /**
     * As part of the ellipse detection we first have to find 4 candidate arcs.
     * This function returns 4 images that contain edges corresponding to the 4
     * arcs that could make up a full ellipse.
     *
     * @param img image
     * @return 4 quadrant sets
     */
    public QuadrantSet findQuadrantSet(Mat img) {
        this.img = img;
        QuadrantSetDetection qsd = new QuadrantSetDetection();
        qs = qsd.findQuadrantSet(img);
        return qs;
    }
    
    public List<MatOfPoint> getArcComb() {
        return arcComb;
    }
    
    public List<RotatedRect> getEllipses() {
        return ellipses;
    }
    
    public void pickArcs() {
        for (int i = 0; i < 4; i++) {
            arcPicking(i);
        }
    }
    
    public void fitEllipses() {
        ellipses = new ArrayList<>();
        for (MatOfPoint mop : arcComb) {
            RotatedRect ellipse = Imgproc.fitEllipse(new MatOfPoint2f(mop.toArray()));
            double totalLength = Imgproc.arcLength(new MatOfPoint2f(Arrays.copyOfRange(mop.toArray(), 0, 3)), false) +
                                 Imgproc.arcLength(new MatOfPoint2f(Arrays.copyOfRange(mop.toArray(), 3, 6)), false) +
                                 Imgproc.arcLength(new MatOfPoint2f(Arrays.copyOfRange(mop.toArray(), 6, 9)), false);
            if (totalLength / ellipsePerimeter(ellipse) >= PERIMETER_RATIO_MIN &&
                    verifyEllipse(mop, ellipse)) {
                ellipses.add(ellipse);
            }
        }
    }
    
    public boolean verifyEllipse(MatOfPoint mop, RotatedRect ellipse) {
        Point[] points = mop.toArray();
        double count = 0;
        for (Point p : points) {
            if (Math.abs(thing(p, ellipse) - 1) < ELLIPSE_THRESH) {
                count++;
            }
        }
        return count == MIN_COUNT;
    }
    
    public double thing(Point p, RotatedRect e) {
        double h = e.center.x;
        double k = e.center.y;
        double A = (e.angle * Math.PI) / 180.0;
        double a = e.size.width / 2.0;
        double b = e.size.height / 2.0;
        
        return (Math.pow((p.x - h) * Math.cos(A) + (p.y - k) * Math.sin(A), 2.0) / Math.pow(a, 2.0)) +
               (Math.pow((p.x - h) * Math.sin(A) - (p.y - k) * Math.cos(A), 2.0) / Math.pow(b, 2.0));
    }
    
    public void removeDuplicates() {
        for (int i = 0; i < ellipses.size() - 1; i++) {
            for (int j = i + 1; j < ellipses.size(); j++) {
                RotatedRect a = ellipses.get(i);
                RotatedRect b = ellipses.get(j);
                if (areSimilar(a, b)) {
                    ellipses.remove(j);
                    j--;
                }
            }
        }
    }
    
    public double ellipsePerimeter(RotatedRect e) {
        return 2.0 * Math.PI * Math.sqrt(
                                (Math.pow(e.size.width, 2.0) + Math.pow(e.size.height, 2.0))
                                / 2.0
        );
    }
    
    public boolean areSimilar(RotatedRect rect1, RotatedRect rect2) {
        double Dx = Math.abs(rect1.center.x - rect2.center.x)/img.width();
        double Dy = Math.abs(rect1.center.y - rect2.center.y)/img.height();
        
        double a1 = rect1.size.width;
        double a2 = rect2.size.width;
        
        double b1 = rect1.size.height;
        double b2 = rect2.size.height;
        
        double Da = Math.abs(a1 - a2) / Double.max(a1, a2);
        double Db = Math.abs(b1 - b2) / Double.min(b1, b2);
        
        double Dt = (b1/a1 >= 0.9 && b2/a2 >= 0.9) ? 0.0 :
                    (b1/a1 >= 0.9 && b2/a2 <  0.9) ? 1.0 :
                    (b1/a1 <  0.9 && b2/a2 >= 0.9) ? 1.0 :
                    Math.abs((double) (rect1.angle - rect2.angle)) / Math.PI;
        
        return Dx < LIMIT && Dy < LIMIT && Da < LIMIT && Db < LIMIT && Dt < LIMIT;
    }
    
    public void arcPicking(int i) {
        List<MatOfPoint> arcs1 = qs.get(i);
        List<MatOfPoint> arcs2 = qs.get(Math.floorMod(i - 1, 4));
        List<MatOfPoint> arcs3 = qs.get(Math.floorMod(i - 2, 4));
        
        for (int i2 = 0; i2 < arcs2.size(); i2++) {
            for (int i1 = 0; i1 < arcs1.size(); i1++) {
                if (!meetsCoordCon(arcs1.get(i1), arcs2.get(i2), i) ||
                    !MatchArcs.isMatch(arcs1.get(i1), arcs2.get(i2)) ||
                    !meetsDistCon(arcs1.get(i1), arcs2.get(i2))) {
                    continue;
                }
                for (int i3 = 0; i3 < arcs3.size(); i3++) {
                    if (!meetsCoordCon(arcs2.get(i2), arcs3.get(i3), Math.floorMod(i - 1, 4)) ||
                        !meetsDistCon(arcs2.get(i2), arcs3.get(i3))) {
                        continue;
                    }
                    MatOfPoint pointSet = new MatOfPoint();
                    pointSet.alloc(arcs1.get(i1).rows() + arcs2.get(i2).rows() + arcs3.get(i3).rows());
                    
                    int[] buffer = new int[2 * arcs1.get(i1).rows()];
                    arcs1.get(i1).get(0, 0, buffer);
                    pointSet.put(0, 0, buffer);
                    
                    buffer = new int[2 * arcs2.get(i2).rows()];
                    arcs2.get(i2).get(0, 0, buffer);
                    pointSet.put(arcs1.get(i1).rows(), 0, buffer);
                    
                    buffer = new int[2 * arcs3.get(i3).rows()];
                    arcs3.get(i3).get(0, 0, buffer);
                    pointSet.put(arcs1.get(i1).rows() + arcs2.get(i2).rows(), 0, buffer);
                    
                    arcComb.add(pointSet);
                }
            }
        }
    }
    
    private boolean meetsDistCon(MatOfPoint arc1, MatOfPoint arc2) {
        return MatchArcs.calcMinDistance(arc1, arc2) * MAX_DISTANCE_FACTOR <
                Imgproc.arcLength(new MatOfPoint2f(arc1.toArray()), false) +
                Imgproc.arcLength(new MatOfPoint2f(arc2.toArray()), false);
    }
    
    /**
     * 
     * @param arc1
     * @param arc2 must be in the quadrant 90 degrees clockwise from arc1
     * @param inc the quadrant arc1 is in where 0 is top right going clockwise
     * @return 
     */
    private boolean meetsCoordCon(MatOfPoint arc1, MatOfPoint arc2, int inc) {
        if (inc < 0 || inc > 3) {
            throw new InvalidParameterException("inc must be between 0 and 3");
        }
        
        switch(inc) {
            case 0:
                return maxY(arc1) < minY(arc2);
                
            case 3:
                return minX(arc1) > maxX(arc2);
                
            case 2:
                return minY(arc1) > maxY(arc2);
                
            case 1:
                return maxX(arc1) < minX(arc2);
        }
        
        throw new RuntimeException("This should never be executed");
    }
    
    private double minX(MatOfPoint arc) {
        return arc.get(0, 0)[0] < arc.get(2, 0)[0] ? arc.get(0, 0)[0] : arc.get(2, 0)[0];
    }
    
    private double maxX(MatOfPoint arc) {
        return arc.get(0, 0)[0] > arc.get(2, 0)[0] ? arc.get(0, 0)[0] : arc.get(2, 0)[0];
    }
    
    private double minY(MatOfPoint arc) {
        return arc.get(0, 0)[1] < arc.get(2, 0)[1] ? arc.get(0, 0)[1] : arc.get(2, 0)[1];
    }
    
    private double maxY(MatOfPoint arc) {
        return arc.get(0, 0)[1] > arc.get(2, 0)[1] ? arc.get(0, 0)[1] : arc.get(2, 0)[1];
    }
}
