package ironclad2D;

public abstract class TimedEvent<T extends IroncladGameState> {
    
    public abstract void eventActions(IroncladGame game, T state);
    
}
