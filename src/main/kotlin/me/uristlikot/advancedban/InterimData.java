package me.uristlikot.advancedban;

import me.uristlikot.advancedban.manager.PunishmentManager;

import java.util.Set;

/**
 * Created by Leo on 04.08.2017.
 */
public class InterimData {

    private final String uuid, name, ip;
    private final Set<Punishment> punishments, history;

    public InterimData(String uuid, String name, String ip, Set<Punishment> punishments, Set<Punishment> history) {
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        this.punishments = punishments;
        this.history = history;
    }

    public Punishment getBan() {
        for (Punishment pt : punishments) {
            if (pt.getType().getBasic() == PunishmentType.BAN && !pt.isExpired()) {
                return pt;
            }
        }
        return null;
    }

    public void accept() {
        PunishmentManager.get().getLoadedPunishments(false).addAll(punishments);
        PunishmentManager.get().getLoadedHistory().addAll(history);
        PunishmentManager.get().addCached(name);
        PunishmentManager.get().addCached(ip);
        PunishmentManager.get().addCached(uuid);
    }
}