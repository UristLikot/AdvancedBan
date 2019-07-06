package me.uristlikot.advancedban.sponge.event

import me.uristlikot.advancedban.Punishment
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.Cause.Builder
import org.spongepowered.api.event.cause.EventContext

class PunishmentEvent(
        val punishment: Punishment) : Event {
    override fun getCause(): Cause {
        return cause
    }
}
