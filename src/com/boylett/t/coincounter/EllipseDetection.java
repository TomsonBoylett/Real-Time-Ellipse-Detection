package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author tomson
 */
public class EllipseDetection {
    private QuadrantSetDetection qsd;
    private EllipseVerify ev;
    private MatchArcs ma;
    private double limit;

    private EllipseDetection(EllipseVerify ev, MatchArcs ma, double limit, QuadrantSetDetection qsd) {
        this.ev = ev;
        this.ma = ma;
        this.limit = limit;
        this.qsd = qsd;
    }
    
    public static class Builder {
        private static final double LIMIT = 0.1;
        
        private QuadrantSetDetection qsd;
        private EllipseVerify ev;
        private MatchArcs ma;
        private double limit;
        
        public Builder() {
            setToDefault();
        }
        
        public final void setToDefault() {
            this.qsd = new QuadrantSetDetection.Builder().build();
            this.ev = new EllipseVerify.Builder().build();
            this.ma = new MatchArcs();
            this.limit = LIMIT;
        }
        
        public Builder setQuadrantSetDetection(QuadrantSetDetection qsd) {
            this.qsd = qsd;
            return this;
        }

        public Builder setEllipseVerify(EllipseVerify ev) {
            this.ev = ev;
            return this;
        }

        public Builder setMatchArcs(MatchArcs ma) {
            this.ma = ma;
            return this;
        }

        public Builder setLimit(double limit) {
            this.limit = limit;
            return this;
        }
        
        public EllipseDetection build() {
            return new EllipseDetection(ev, ma, limit, qsd);
        }
    }
    
    public List<RotatedRect> detect(Mat img) {
        if (img.channels() != 1 && img.channels() != 3) {
            throw new InvalidParameterException("Image must be greyscale or RGB");
        }
        if (img.dims() > 2 || img.rows() == 0 || img.cols() == 0) {
            throw new InvalidParameterException("Image has invalid dimensions");
        }
        if (img.depth() != CvType.CV_8U) {
            throw new InvalidParameterException("Image must have a bit depth of 8 and be unsigned");
        }
        QuadrantSet qs = findCandidateArcs(img);
        List<MatOfPoint2f> arcComb = new ArcPicking(ma).pickArcs(qs);
        List<RotatedRect> ellipses = fitEllipses(arcComb);
        removeDuplicates(ellipses, img.size());
        return ellipses;
    }

    /**
     * As part of the ellipse detection we first have to find 4 candidate arcs.
     * This function returns 4 images that contain edges corresponding to the 4
     * arcs that could make up a full ellipse.
     *
     * @param img image
     * @return 4 quadrant sets
     */
    private QuadrantSet findCandidateArcs(Mat img) {
        return new QuadrantSetDetection.Builder().build().detect(img);
    }
    
    private List<RotatedRect> fitEllipses(List<MatOfPoint2f> arcComb) {
        List<RotatedRect> ellipses = new ArrayList<>();
        for (MatOfPoint2f mop : arcComb) {
            RotatedRect ellipse = Imgproc.fitEllipse(mop);
            if (ev.verify(mop, ellipse)) {
                ellipses.add(ellipse);
            }
        }
        return ellipses;
    }
    
    private void removeDuplicates(List<RotatedRect> ellipses, Size imgSize) {
        for (int i = 0; i < ellipses.size() - 1; i++) {
            for (int j = i + 1; j < ellipses.size(); j++) {
                System.out.println(ellipses.size());
                RotatedRect a = ellipses.get(i);
                RotatedRect b = ellipses.get(j);
                
                if (areSimilar(a, b, imgSize)) {
                    ellipses.remove(j);
                    j--;
                    continue;
                }
                
                RotatedRect rr = combine(a, b);
                if (rr != null) {
                    ellipses.remove(j);
                    ellipses.remove(i);
                    ellipses.add(rr);
                    
                    i--;
                    break;
                }
                
            }
        }
    }
    
    private boolean areSimilar(RotatedRect rect1, RotatedRect rect2, Size imgSize) {
        double Dx = Math.abs(rect1.center.x - rect2.center.x)/imgSize.width;
        double Dy = Math.abs(rect1.center.y - rect2.center.y)/imgSize.height;
        
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
        
        return Dx < limit && Dy < limit && Da < limit && Db < limit && Dt < limit;
    }
    
    private RotatedRect combine(RotatedRect rect1, RotatedRect rect2) {
        if (EllipseVerify.dispFromEllipse(rect1.center, rect2) < 1 ||
                EllipseVerify.dispFromEllipse(rect2.center, rect1) < 1) {
            double x = (rect1.center.x + rect2.center.x) / 2.0;
            double y = (rect1.center.y + rect2.center.y) / 2.0;
            double width = (rect1.size.width + rect2.size.width) / 2.0;
            double height = (rect1.size.height + rect2.size.height) / 2.0;
            double angle = (rect1.angle + rect2.angle) / 2.0;
            return new RotatedRect(new Point(x, y), new Size(width, height), angle);
        }
        
        return null;
    }
}
