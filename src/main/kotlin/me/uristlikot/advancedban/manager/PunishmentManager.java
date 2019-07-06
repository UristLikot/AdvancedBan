package me.uristlikot.advancedban.manager;

import me.uristlikot.advancedban.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
public class PunishmentManager {

    private static PunishmentManager instance = null;
    private final Universal universal = Universal.get();
    private final Set<Punishment> punishments = Collections.synchronizedSet(new HashSet<>());
    private final Set<Punishment> history = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> cached = Collections.synchronizedSet(new HashSet<>());

    public static PunishmentManager get() {
        return instance == null ? instance = new PunishmentManager() : instance;
    }

    public void setup() {
        MethodInterface mi = universal.getMethods();
        DatabaseManager.get().executeStatement(SQLQuery.DELETE_OLD_PUNISHMENTS, TimeManager.getTime());
        for (Object player : mi.getOnlinePlayers()) {
            String name = mi.getName(player).toLowerCase();
            load(name, UUIDManager.get().getUUID(name), mi.getIP(player));
        }
    }

    public InterimData load(String name, String uuid, String ip) {
	Set<Punishment> punishments = new HashSet<>();
	Set<Punishment> history = new HashSet<>();
        try {
            ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_WITH_IP, uuid, ip);
            while (rs.next()) {
                punishments.add(getPunishmentFromResultSet(rs));
            }
            rs.close();

            rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP, uuid, ip);
            while (rs.next()) {
                history.add(getPunishmentFromResultSet(rs));
            }
            rs.close();
        } catch (SQLException ex) {
            universal.log("An error has ocurred loading the punishments from the database.");
            universal.debug(ex);
        }
        return new InterimData(uuid, name, ip, punishments, history);
    }

    public void discard(String name) {
        name = name.toLowerCase();
        String ip = Universal.get().getIps().get(name);
        String uuid = UUIDManager.get().getUUID(name);
        cached.remove(name);
        cached.remove(uuid);
        cached.remove(ip);

        Iterator<Punishment> iterator = punishments.iterator();
        while (iterator.hasNext()) {
            Punishment punishment = iterator.next();
            if (punishment.getUuid().equals(uuid) || punishment.getUuid().equals(ip)) {
                iterator.remove();
            }
        }

        iterator = history.iterator();
        while (iterator.hasNext()) {
            Punishment punishment = iterator.next();
            if (punishment.getUuid().equals(uuid) || punishment.getUuid().equals(ip)) {
                iterator.remove();
            }
        }
    }

    public List<Punishment> getPunishments(String uuid, PunishmentType put, boolean current) {
        List<Punishment> ptList = new ArrayList<>();

        if (isCached(uuid)) {
            for (Iterator<Punishment> iterator = (current ? punishments : history).iterator(); iterator.hasNext();) {
                Punishment pt = iterator.next();
                if ((put == null || put == pt.getType().getBasic()) && pt.getUuid().equals(uuid)) {
                    if (!current || !pt.isExpired()) {
                        ptList.add(pt);
                    } else {
                        pt.delete(null, false, false);
                        iterator.remove();
                    }
                }
            }
        } else {
            ResultSet rs = DatabaseManager.get().executeResultStatement(current ? SQLQuery.SELECT_USER_PUNISHMENTS : SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY, uuid);
            try {
                while (rs.next()) {
                    Punishment punishment = getPunishmentFromResultSet(rs);
                    if ((put == null || put == punishment.getType().getBasic()) && (!current || !punishment.isExpired())) {
                        ptList.add(punishment);
                    }
                }
                rs.close();
            } catch (SQLException ex) {
                universal.log("An error has ocurred getting the punishments for " + uuid);
                universal.debug(ex);
            }
        }
        return ptList;
    }

    public List<Punishment> getPunishments(SQLQuery sqlQuery, Object... parameters) {
        List<Punishment> ptList = new ArrayList<>();

        ResultSet rs = DatabaseManager.get().executeResultStatement(sqlQuery, parameters);
        try {
            while (rs.next()) {
                Punishment punishment = getPunishmentFromResultSet(rs);
                ptList.add(punishment);
            }
            rs.close();
        } catch (SQLException ex) {
            universal.log("An error has ocurred executing a query in the database.");
            universal.debug("Query: \n" + sqlQuery);
            universal.debug(ex);
        }
        return ptList;
    }

    public Punishment getPunishment(int id) {
        ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_PUNISHMENT_BY_ID, id);
        Punishment pt = null;
        try {
            if (rs.next()) {
                pt = getPunishmentFromResultSet(rs);
            }
            rs.close();
        } catch (SQLException ex) {
            universal.log("An error has ocurred getting a punishment by his id.");
            universal.debug("Punishment id: '" + id + "'");
            universal.debug(ex);
        }
        return pt == null || pt.isExpired() ? null : pt;
    }

    public Punishment getWarn(int id) {
        Punishment punishment = getPunishment(id);
        return punishment.getType().getBasic() == PunishmentType.WARNING ? punishment : null;
    }

    public List<Punishment> getWarns(String uuid) {
        return getPunishments(uuid, PunishmentType.WARNING, true);
    }

    public Punishment getBan(String uuid) {
        List<Punishment> punishments = getPunishments(uuid, PunishmentType.BAN, true);
        return punishments.isEmpty() ? null : punishments.get(0);
    }

    public Punishment getMute(String uuid) {
        List<Punishment> punishments = getPunishments(uuid, PunishmentType.MUTE, true);
        return punishments.isEmpty() ? null : punishments.get(0);
    }

    public boolean isBanned(String uuid) {
        return getBan(uuid) != null;
    }

    public boolean isMuted(String uuid) {
        return getMute(uuid) != null;
    }

    public boolean isCached(String name) {
        return cached.contains(name);
    }

    public void addCached(String name) {
        cached.add(name);
    }

    public int getCalculationLevel(String uuid, String layout) {
        if (isCached(uuid)) {
            return (int) history.stream().filter(pt -> pt.getUuid().equals(uuid) && layout.equalsIgnoreCase(pt.getCalculation())).count();
        } else {
            ResultSet resultSet = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION, uuid, layout);
            int i = 0;
            try {
                while (resultSet.next()) {
                    i++;
                }
                resultSet.close();
            } catch (SQLException ex) {
                universal.log("An error has ocurred getting the level for the layout '" + layout + "' for '" + uuid + "'");
                universal.debug(ex);
            }
            return i;
        }
    }

    public int getCurrentWarns(String uuid) {
        return getWarns(uuid).size();
    }

    public Set<Punishment> getLoadedPunishments(boolean checkExpired) {
        if (checkExpired) {
            List<Punishment> toDelete = new ArrayList<>();
            for (Punishment pu : punishments) {
                if (pu.isExpired()) {
                    toDelete.add(pu);
                }
            }
            for (Punishment pu : toDelete) {
                pu.delete();
            }
        }
        return punishments;
    }

//    public long getCalculation(String layout, String name, String uuid) {
//        long end = TimeManager.getTime();
//        MethodInterface mi = Universal.get().getMethods();
//
//        int i = getCalculationLevel(name, uuid);
//
//        List<String> timeLayout = mi.getStringList(mi.getLayouts(), "Time." + layout);
//        String time = timeLayout.get(timeLayout.size() <= i ? timeLayout.size() - 1 : i);
//        long toAdd = TimeManager.toMilliSec(time.toLowerCase());
//        end += toAdd;
//
//        return end;
//    }
    public Punishment getPunishmentFromResultSet(ResultSet rs) throws SQLException {
        return new Punishment(
                rs.getString("name"),
                rs.getString("uuid"), rs.getString("reason"),
                rs.getString("operator"),
                PunishmentType.valueOf(rs.getString("punishmentType")),
                rs.getLong("start"),
                rs.getLong("end"),
                rs.getString("calculation"),
                rs.getInt("id"));
    }

    public Set<Punishment> getLoadedHistory() {
        return history;
    }
}