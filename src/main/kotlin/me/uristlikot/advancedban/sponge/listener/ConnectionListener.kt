package me.uristlikot.advancedban.sponge.listener

import me.uristlikot.advancedban.Universal
import me.uristlikot.advancedban.manager.PunishmentManager
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.network.ClientConnectionEvent


class ConnectionListener : EventListener<ClientConnectionEvent.Login> {
    @Listener
    override fun handle(event: ClientConnectionEvent.Login) {
        val result = Universal.get().callConnection(event.profile.name.toString(), event.connection.address.toString())
        if (result==null){
            event.targetUser.player.get().kick()
        }
    }
    @Listener
    fun onDisconnect(event:ClientConnectionEvent.Disconnect){
        PunishmentManager.get().discard(event.source.toString())
    }


}