package com.custom.app.monitor

/**
 * An abstract base class for a "monitor" which is a class that takes listeners and automatically
 * starts and stops monitoring when it has listeners.
 *
 * @param L The type of listener this Monitor takes.
 */
abstract class AbstractMonitor<L> {
    /** Listeners to be notified of changes. */
    protected val listeners = mutableSetOf<L>()
    
    /**
     * Add a listener to be notified of changes.
     */
    fun addListener(listener: L) {
        // Start if this is the first listener
        if (listeners.isEmpty()) {
            start()
        }
        
        listeners.add(listener)
    }
    
    /**
     * Remove a listener.
     */
    fun removeListener(listener: L) {
        listeners.remove(listener)
        
        // Stop if this was the last listener
        if (listeners.isEmpty()) {
            stop()
        }
    }
    
    /**
     * Called when the first listener is added to start monitoring for changes.
     */
    protected abstract fun start()
    
    /**
     * Called when the last listener is removed to stop monitoring for changes.
     */
    protected abstract fun stop()
}