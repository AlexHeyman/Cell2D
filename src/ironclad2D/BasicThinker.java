package ironclad2D;

public abstract class BasicThinker extends Thinker<BasicGameState,BasicThinker,BasicThinkerState> {
    
    @Override
    public BasicThinker getThis() {
        return this;
    }
    
}
