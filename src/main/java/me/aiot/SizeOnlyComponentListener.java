package me.aiot;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public interface SizeOnlyComponentListener extends ComponentListener {
    @Override
    default void componentMoved(ComponentEvent e) {

    }

    @Override
    default void componentShown(ComponentEvent e) {

    }

    @Override
    default void componentHidden(ComponentEvent e) {

    }
}
