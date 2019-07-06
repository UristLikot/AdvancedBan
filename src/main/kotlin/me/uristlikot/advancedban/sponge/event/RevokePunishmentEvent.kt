package me.uristlikot.advancedban.sponge.event

import me.uristlikot.advancedban.Punishment
import me.uristlikot.advancedban.sponge.event.PunishmentEvent
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.cause.Cause

class RevokePunishmentEvent(val punishment: Punishment, val isMassClear: Boolean) : Event {
    override fun getCause(): Cause {
        return cause
    }

}