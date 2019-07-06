package me.uristlikot.advancedban.sponge


import me.uristlikot.advancedban.Universal
import me.uristlikot.advancedban.sponge.listener.ChatListener
import me.uristlikot.advancedban.sponge.listener.CommandListener
import me.uristlikot.advancedban.sponge.listener.ConnectionListener
import me.uristlikot.advancedban.sponge.listener.InternalListener
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.game.state.GameStartingServerEvent
import org.spongepowered.api.plugin.Plugin

@Plugin(id = "abplugin", name = "abplugin", version = "0.0.1", description = "My first plugin")
class abplugin{
    @Listener
    fun onServerStart(event:GameStartingServerEvent){
        instance=this
        Sponge.getEventManager().registerListeners(this,ChatListener())
        Sponge.getEventManager().registerListeners(this,CommandListener())
        Sponge.getEventManager().registerListeners(this,ConnectionListener())
        Sponge.getEventManager().registerListeners(this,InternalListener())



    }
    companion object {
        private var instance: abplugin? = null

        fun get(): abplugin? {
            return instance
        }
    }
}