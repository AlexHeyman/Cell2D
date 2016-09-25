package ironclad2D;

public interface Animatable {
    
    public int getLevel();
    
    public int getLength();
    
    public Animatable getFrame(int index);
    
    public boolean framesAreCompatible(int index1, int index2);
    
    public double getFrameDuration(int index);
    
}
