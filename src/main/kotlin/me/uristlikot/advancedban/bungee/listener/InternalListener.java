package me.uristlikot.advancedban.bungee.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.uristlikot.advancedban.Punishment;
import me.uristlikot.advancedban.PunishmentType;
import me.uristlikot.advancedban.Universal;
import me.uristlikot.advancedban.bungee.event.PunishmentEvent;
import me.uristlikot.advancedban.bungee.event.RevokePunishmentEvent;
import me.uristlikot.advancedban.manager.TimeManager;
import me.uristlikot.advancedban.manager.UUIDManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Beelzebu
 */
public class InternalListener implements Listener {

    private final Universal universal = Universal.get();

    @EventHandler
    public void onPunish(PunishmentEvent e) {
        sendToBukkit("Punish", Arrays.asList(e.getPunishment().toString()));
    }

    @EventHandler
    public void onUnPunish(RevokePunishmentEvent e) {
        sendToBukkit("Unpunish", Arrays.asList(e.getPunishment().toString()));
    }

    @EventHandler
    public void onPluginMessageEvent(PluginMessageEvent e) {
        if (!e.getTag().equals("AdvancedBan")) {
            return;
        }
        if (e.getSender() instanceof ProxiedPlayer) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        String channel = in.readUTF();
        switch (channel) {
            case "Punish":
                String message = in.readUTF();
                try {
                    JsonObject punishment = universal.getGson().fromJson(message, JsonObject.class);
                    new Punishment(
                            punishment.get("name").getAsString(),
                            UUIDManager.get().getUUID(punishment.get("uuid").getAsString()),
                            punishment.get("reason").getAsString(),
                            punishment.get("operator") != null ? punishment.get("operator").getAsString() : "CONSOLE",
                            PunishmentType.valueOf(punishment.get("punishmenttype").getAsString().toUpperCase()),
                            punishment.get("start") != null ? punishment.get("start").getAsLong() : TimeManager.getTime(),
                            TimeManager.getTime() + punishment.get("end").getAsLong(),
                            punishment.get("calculation") != null ? punishment.get("calculation").getAsString() : null,
                            -1
                    ).create(punishment.get("silent") != null ? punishment.get("silent").getAsBoolean() : false);
                    universal.log("A punishment was created using PluginMessaging listener.");
                    universal.debug(punishment.toString());
                } catch (JsonSyntaxException | NullPointerException ex) {
                    universal.log("An exception as ocurred while reading a punishment from plugin messaging channel.");
                    universal.debug("Message: " + message);
                    universal.log("StackTrace:");
                    ex.printStackTrace();
                }
                break;
            default:
                universal.debug("Unknown channel for tag \"AdvancedBan\"");
                break;
        }
    }

    public void sendToBukkit(String channel, List<String> messages) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        messages.forEach((msg) -> {
            out.writeUTF(msg);
        });
        ProxyServer.getInstance().getServers().keySet().forEach(server -> {
            ProxyServer.getInstance().getServerInfo(server).sendData("AdvancedBan", out.toByteArray(), true);
        });
    }
}
