package cell2D;

public abstract class TimedEvent<T extends CellGameState> {
    
    public abstract void eventActions(CellGame game, T state);
    
}
