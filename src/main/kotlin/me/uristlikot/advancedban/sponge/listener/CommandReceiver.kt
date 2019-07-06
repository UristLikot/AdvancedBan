package me.uristlikot.advancedban.sponge.listener

import me.uristlikot.advancedban.manager.CommandManager
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.event.Listener


class CommandReceiver : CommandExecutor {
    @Listener
    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        CommandManager.get().onCommand(src, args.toString(), arrayOf(src.name))
        return CommandResult.success()
    }

    companion object {
        var instance: CommandReceiver? = null
        fun get(): CommandReceiver {
            return (if (instance == null) {
                instance = CommandReceiver()
            } else instance) as CommandReceiver
        }


    }
}