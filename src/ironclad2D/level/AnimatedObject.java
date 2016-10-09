package ironclad2D.level;

import ironclad2D.Animation;
import ironclad2D.AnimationInstance;
import java.util.HashMap;
import java.util.Map;
import org.newdawn.slick.Graphics;

public abstract class AnimatedObject extends LevelObject {
    
    private AnimationInstance animInstance = AnimationInstance.BLANK;
    private final Map<Integer,AnimationInstance> extraAnimInstances = new HashMap<>();
    
    public AnimatedObject(Hitbox locatorHitbox, int drawLayer) {
        super(locatorHitbox, drawLayer);
    }
    
    @Override
    void addActions() {
        super.addActions();
        levelState.addAnimInstance(animInstance);
        for (AnimationInstance instance : extraAnimInstances.values()) {
            levelState.addAnimInstance(instance);
        }
    }
    
    @Override
    void removeActions() {
        super.removeActions();
        levelState.removeAnimInstance(animInstance);
        for (AnimationInstance instance : extraAnimInstances.values()) {
            levelState.removeAnimInstance(instance);
        }
    }
    
    @Override
    void setTimeFactorActions(double timeFactor) {
        super.setTimeFactorActions(timeFactor);
        animInstance.setTimeFactor(timeFactor);
        for (AnimationInstance instance : extraAnimInstances.values()) {
            instance.setTimeFactor(timeFactor);
        }
    }
    
    public final AnimationInstance getAnimInstance() {
        return animInstance;
    }
    
    public final Animation getAnimation() {
        return animInstance.getAnimation();
    }
    
    public final AnimationInstance setAnimation(Animation animation) {
        if (animation == Animation.BLANK) {
            if (levelState != null) {
                levelState.removeAnimInstance(animInstance);
            }
            animInstance = AnimationInstance.BLANK;
            return AnimationInstance.BLANK;
        }
        AnimationInstance oldInstance = animInstance;
        animInstance = new AnimationInstance(animation);
        if (levelState != null) {
            levelState.addAnimInstance(animInstance);
            levelState.removeAnimInstance(oldInstance);
        }
        animInstance.setTimeFactor(getTimeFactor());
        return animInstance;
    }
    
    public final AnimationInstance getExtraAnimInstance(int id) {
        AnimationInstance instance = extraAnimInstances.get(id);
        if (instance == null) {
            return AnimationInstance.BLANK;
        }
        return instance;
    }
    
    public final Animation getExtraAnimation(int id) {
        return getExtraAnimInstance(id).getAnimation();
    }
    
    public final AnimationInstance setExtraAnimation(int id, Animation animation) {
        if (animation == Animation.BLANK) {
            AnimationInstance oldInstance = extraAnimInstances.remove(id);
            if (levelState != null && oldInstance != null) {
                levelState.removeAnimInstance(oldInstance);
            }
            return AnimationInstance.BLANK;
        }
        AnimationInstance instance = new AnimationInstance(animation);
        AnimationInstance oldInstance = extraAnimInstances.put(id, instance);
        if (levelState != null) {
            levelState.addAnimInstance(instance);
            if (oldInstance != null) {
                levelState.removeAnimInstance(oldInstance);
            }
        }
        instance.setTimeFactor(getTimeFactor());
        return instance;
    }
    
    public final void clearExtraAnimInstances() {
        if (levelState != null) {
            for (AnimationInstance instance : extraAnimInstances.values()) {
                levelState.removeAnimInstance(instance);
            }
        }
        extraAnimInstances.clear();
    }
    
    @Override
    public void draw(Graphics g, int x, int y) {
        animInstance.getCurrentSprite().draw(g, x, y, getXFlip(), getYFlip(), getAngle(), getAlpha(), getFilter());
    }
    
}
