package cell2D;

public abstract class BasicGameState extends CellGameState<BasicGameState,BasicThinker,BasicThinkerState> {
    
    /**
     * Creates a BasicGameState of the specified CellGame with the specified ID.
     * @param game 
     * @param id 
     */
    public BasicGameState(CellGame game, int id) {
        super(game, id);
    }
    
    @Override
    public BasicGameState getThis() {
        return this;
    }
    
}
