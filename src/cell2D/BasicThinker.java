package cell2D;

public abstract class BasicThinker extends Thinker<BasicGameState,BasicThinker,BasicThinkerState> {
    
    @Override
    public final BasicThinker getThis() {
        return this;
    }
    
}
