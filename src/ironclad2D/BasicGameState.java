package ironclad2D;

public abstract class BasicGameState extends IroncladGameState<BasicGameState,BasicThinker,BasicThinkerState> {

    public BasicGameState(IroncladGame game, int id) {
        super(game, id);
    }
    
    @Override
    public BasicGameState getThis() {
        return this;
    }
    
}
