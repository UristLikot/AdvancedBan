package me.uristlikot.advancedban.sponge


import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import me.uristlikot.advancedban.MethodInterface
import me.uristlikot.advancedban.Universal
import me.uristlikot.advancedban.sponge.event.PunishmentEvent
import me.uristlikot.advancedban.sponge.event.RevokePunishmentEvent
import me.uristlikot.advancedban.sponge.listener.CommandReceiver
import me.uristlikot.advancedban.manager.DatabaseManager
import me.uristlikot.advancedban.manager.PunishmentManager
import me.uristlikot.advancedban.manager.UUIDManager
import me.uristlikot.advancedban.Punishment
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.config.YamlConfiguration
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import org.bstats.bungeecord.Metrics
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.EventManager
import org.spongepowered.api.network.Message

import org.spongepowered.api.text.Text
import com.google.gson.JsonObject as JSONObject
import com.google.gson.JsonParser as JSONParser
import com.google.gson.JsonParseException as ParseException
import org.spongepowered.api.plugin.Plugin

import org.spongepowered.plugin.meta.version.ArtifactVersion

abstract class SpongeMethods : MethodInterface {
    val loader=YAMLConfigurationLoader.builder()
    val messageFile = File (dataFolder, "Messages.yml")
    val layoutFile = File(dataFolder, "Layouts.yml")
    var config: YamlConfiguration? = null
    var configFile = File(dataFolder, "config.yml")
    var messages: YamlConfiguration? = null
    var layouts: YamlConfiguration? = null
    var mysql: YamlConfiguration? = null

//    override fun loadFiles() {
//        if (!configFile.exists()) {
//            loader
//                    .("config.yml", true)
//        }
//        if (!messageFile.exists()) {
//            (plugin as Plugin).saveResource("Messages.yml", true)
//        }
//        if (!layoutFile.exists()) {
//            (plugin as Plugin).saveResource("Layouts.yml", true)
//        }
//
//        config = YamlConfiguration.loadConfiguration(configFile)
//        messages = YamlConfiguration.loadConfiguration(messageFile)
//        layouts = YamlConfiguration.loadConfiguration(layoutFile)
//
//        if (!config!!.contains("UUID-Fetcher")) {
//
//            configFile.renameTo(File(dataFolder, "oldConfig.yml"))
//            configFile = File(dataFolder, "config.yml")
//            (plugin as Plugin).saveResource("config.yml", true)
//            config = YamlConfiguration.loadConfiguration(configFile)
//        }
//    }

    override fun getFromUrlJson(url: String, key: String): String? {
        try {
            val request = URL(url).openConnection() as HttpURLConnection
            request.connect()

            val jp = JSONParser()
            var json = jp.parse(InputStreamReader(request.inputStream)) as JSONObject

            val keys = key.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in 0 until keys.size - 1) {
                json = json[keys[i]] as JSONObject
            }

            return json[keys[keys.size - 1]].toString()
        } catch (exc: Exception) {
            return null
        }

    }

//    override fun getKeys(file: Any, path: String): Array<String> {
//        val ss = arrayOfNulls<String>(0)
//        return (file as YamlConfiguration).getConfigurationSection(path).getKeys(false).toTypedArray()
//    }

    override fun getConfig(): Any? {
        return config
    }

    override fun getMessages(): Any? {
        return messages
    }

    override fun getLayouts(): Any? {
        return layouts
    }

    override fun setupMetrics() {
        val metrics = Metrics(plugin as net.md_5.bungee.api.plugin.Plugin)
        metrics.addCustomChart(Metrics.SimplePie("MySQL", { if (DatabaseManager.get().isUseMySQL()) "yes" else "no" }))
    }

//    override fun setCommandExecutor(cmd: String) {
//        val command = Sponge.getCommandManager().get(cmd)
//        if (command != null) {
//            command = CommandReceiver.get()
//        } else {
//            println("AdvancedBan >> Failed to register command $cmd")
//        }
//    }

    override fun sendMessage(player: Any, msg: String) {
        (player as CommandSender).sendMessage(msg)
    }

    override fun hasPerms(player: Any, perms: String): Boolean {
        return (player as CommandSender).hasPermission(perms)
    }

    override fun isOnline(name: String): Boolean {
        return Sponge.getServer().getPlayer(name).isPresent
    }

    override fun getPlayer(name: String): Any {
        return Sponge.getServer().getPlayer(name)
    }

    override fun kickPlayer(player: String, reason: String) {
        if (Sponge.getServer().getPlayer(player) != null && Sponge.getServer().getPlayer(player).isPresent) {
            Sponge.getServer().getPlayer(player).get().kick(Text.of(reason))
        }
    }

    override fun scheduleAsyncRep(rn: Runnable, l1: Long, l2: Long) {
        Sponge.getScheduler().createAsyncExecutor(plugin)
    }

    override fun scheduleAsync(rn: Runnable, l1: Long) {
        Sponge.getScheduler().createAsyncExecutor(plugin)
    }

    override fun runAsync(rn: Runnable) {
        Sponge.getScheduler().run { rn }
    }

    override fun runSync(rn: Runnable) {
        Sponge.getScheduler().run { rn }

    }

    override fun executeCommand(cmd: String) {
        Sponge.getServiceManager().provide(ConsoleSource::class.java).get().sendMessage(Text.of(cmd))
    }

    override fun getName(player: Any): String {
        return (player as CommandSender).name
    }

    override fun getName(uuid: String): String {
        return Sponge.getServer().getPlayer(uuid).toString()
    }

    override fun getIP(player: Any): String {
        return Sponge.getServer().getPlayer(player.toString()).toString()
    }
//
override fun getInternUUID(player: Any): String {
        return Sponge.getServer().getPlayer(player.toString()).get().profile.name.toString()
    }

    override fun getInternUUID(player: String): String {
        return Sponge.getServer().getPlayer(player).get().profile.name.toString()
    }

    override fun callChat(player: Any): Boolean {
        val pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))
        if (pnt != null) {
            for (str in pnt!!.getLayout()) {
                sendMessage(player, str)
            }
            return true
        }
        return false
    }

    override fun callCMD(player: Any, cmd: String): Boolean {
        val pnt=PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))
        if (Universal.get().isMuteCommand(cmd.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].substring(1)) && pnt!= null) {
            for (str in pnt!!.getLayout()) {
                sendMessage(player, str)
            }
            return true
        }
        return false
    }

//    override fun loadMySQLFile(f: File) {
//        mysql = YamlConfiguration.loadConfiguration(f)
//    }

//    override fun createMySQLFile(f: File) {
//        mysql!!.set("MySQL.IP", "localhost")
//        mysql!!.set("MySQL.DB-Name", "YourDatabase")
//        mysql!!.set("MySQL.Username", "root")
//        mysql!!.set("MySQL.Password", "pw123")
//        mysql!!.set("MySQL.Port", 3306)
//        try {
//            mysql!!.save(f)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//    }

    override fun parseJSON(json: InputStreamReader, key: String): String? {
        try {
            return (JSONParser().parse(json) as JSONObject)[key].toString()
        } catch (e: ParseException) {
            println("Error -> " + e.message)
            return null
        } catch (e: IOException) {
            println("Error -> " + e.message)
            return null
        }

    }

    override fun parseJSON(json: String, key: String): String? {
        try {
            return (JSONParser().parse(json) as JSONObject)[key].toString()
        } catch (e: ParseException) {
            return null
        }

    }

    override fun getBoolean(file: Any, path: String): Boolean? {
        return true
    }

    override fun getString(file: Any, path: String): String {
        return (file as YamlConfiguration).toString()
    }

    override fun getLong(file: Any, path: String): Long? {
        return 1.2.toLong()
    }

    override fun getInteger(file: Any, path: String): Int? {
        return 1
    }

    override fun getStringList(file: Any, path: String): List<String> {
        return listOf("""""")
    }

    override fun getBoolean(file: Any, path: String, def: Boolean): Boolean {
        return true
    }

    override fun getString(file: Any, path: String, def: String): String {
        return (file as YamlConfiguration).toString()
    }

    override fun getLong(file: Any, path: String, def: Long): Long {
        return 1.2f.toLong()
    }

    override fun getInteger(file: Any, path: String, def: Int): Int {
        return 1
    }

    override fun contains(file: Any, path: String): Boolean {
        return true
    }

    override fun getFileName(file: Any): String {
        return (file as YamlConfiguration).toString()
    }

    override fun callPunishmentEvent(punishment: Punishment) {
        Sponge.getServiceManager().provide(EventManager::class.java).get().post(PunishmentEvent(punishment))
    }

    override fun callRevokePunishmentEvent(punishment: Punishment, massClear: Boolean) {
        Sponge.getServiceManager().provide(EventManager::class.java).get().post(RevokePunishmentEvent(punishment,massClear))
    }


    override fun notify(perm: String, notification: List<String>) {
        for (p in Sponge.getServer().getOnlinePlayers()) {
            if (hasPerms(p, perm)) {
                for (str in notification) {
                    sendMessage(p, str)
                }
            }
        }
    }

    override fun log(msg: String) {
        Sponge.getServiceManager().provide(ConsoleSource::class.java).get().sendMessage(Text.of(msg))
    }
}