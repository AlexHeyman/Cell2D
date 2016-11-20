package ironclad2D;

public abstract class ThinkerState<T extends IroncladGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
    
    public abstract int getDuration();
    
    public abstract V getNextState();
    
    public void enteredActions(IroncladGame game, T state) {}
    
    public void leftActions(IroncladGame game, T state) {}
    
    public void timeUnitActions(IroncladGame game, T state) {}
    
    public void stepActions(IroncladGame game, T state) {}
    
}
