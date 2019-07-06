package me.uristlikot.advancedban.sponge.listener

import me.uristlikot.advancedban.Universal
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.message.MessageChannelEvent

class ChatListener : EventListener<MessageChannelEvent.Chat> {
    @Listener
    override fun handle(event: MessageChannelEvent.Chat) {
        if (Universal.get().methods.callChat(event.source)){
            event.isCancelled=true
        }
    }

}