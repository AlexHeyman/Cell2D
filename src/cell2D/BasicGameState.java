package cell2D;

public abstract class BasicGameState extends CellGameState<BasicGameState,BasicThinker,BasicThinkerState> {

    public BasicGameState(CellGame game, int id) {
        super(game, id);
    }
    
    @Override
    public BasicGameState getThis() {
        return this;
    }
    
}
