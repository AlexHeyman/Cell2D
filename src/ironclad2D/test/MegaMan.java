package ironclad2D.test;

import ironclad2D.IroncladGame;
import ironclad2D.level.RectangleHitbox;
import ironclad2D.level.ThinkerObject;

public class MegaMan extends ThinkerObject {
    
    public MegaMan(IroncladGame game, double x, double y) {
        super(new RectangleHitbox(x, y, -16, 16, -24, 8), 0);
        setOverlapHitbox(new RectangleHitbox(0, 0, -10, 10, -24, 0));
        setCollisionHitbox(new RectangleHitbox(0, 0, -8, 8, -24, 0));
        setAnimation(game.getAnimation("megaman"));
    }
    
}
