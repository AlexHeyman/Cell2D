package cell2D;

import java.util.Iterator;

public interface SafeIterator<E> extends Iterator<E> {
    
    public boolean isFinished();
    
    public void finish();
    
}
