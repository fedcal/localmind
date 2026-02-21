package com.localmind.domain.common.event;

/**
 * Evento emesso quando cambia lo stato di un plugin.
 * Event emitted when a plugin's state changes.
 */
public class PluginStateChangedEvent extends DomainEvent {

    private final String pluginId;
    private final String previousState;
    private final String newState;

    public PluginStateChangedEvent(String pluginId, String previousState, String newState) {
        super("PLUGIN_STATE_CHANGED");
        this.pluginId = pluginId;
        this.previousState = previousState;
        this.newState = newState;
    }

    public String getPluginId() { return pluginId; }
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
}
