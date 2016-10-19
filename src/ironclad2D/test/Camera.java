package ironclad2D.test;

import ironclad2D.level.PointHitbox;
import ironclad2D.level.ThinkerObject;

public class Camera extends ThinkerObject {

    public Camera(double x, double y) {
        super(new PointHitbox(x, y), 0);
    }
    
}
