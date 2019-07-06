package me.uristlikot.advancedban.sponge.listener

import me.uristlikot.advancedban.Universal
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.command.TabCompleteEvent


class CommandListener :EventListener<TabCompleteEvent.Chat> {
    @Listener
    override fun handle(event: TabCompleteEvent.Chat) {
        if (Universal.get().methods.callCMD(event.source,event.rawMessage)){
            event.isCancelled=true
        }
    }
}