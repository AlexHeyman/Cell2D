package org.cell2d.basic;

import org.cell2d.CellGame;
import org.cell2d.Event;

/**
 * <p>A BasicEvent is a type of Event that can be involved by BasicStates.</p>
 * @author Andrew Heyman
 */
public interface BasicEvent extends Event<CellGame,BasicState> {}
