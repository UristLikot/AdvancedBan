package me.uristlikot.advancedban.bungee.event;

import me.uristlikot.advancedban.Punishment;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event fired when a punishment is created
 */
public class PunishmentEvent extends Event {
    private final Punishment punishment;

    public PunishmentEvent(Punishment punishment) {
        this.punishment = punishment;
    }

    /**
     * Returns the punishment involved in this event
     *
     * @return Punishment
     */
    public Punishment getPunishment() {
        return punishment;
    }
}