package ht.treeplant.server.event;

public interface TickHandler {
    TickHandler INSTANCE = null;

    void activate();

    void deactivate();
}
