package cell2D;

public abstract class ThinkerState<T extends CellGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
    
    public abstract int getDuration();
    
    public abstract V getNextState();
    
    public void enteredActions(CellGame game, T state) {}
    
    public void leftActions(CellGame game, T state) {}
    
    public void timeUnitActions(CellGame game, T state) {}
    
    public void stepActions(CellGame game, T state) {}
    
}
