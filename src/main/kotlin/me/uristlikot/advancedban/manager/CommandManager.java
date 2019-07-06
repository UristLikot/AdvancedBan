package me.uristlikot.advancedban.manager;

import me.uristlikot.advancedban.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Leoko @ dev.skamps.eu on 12.07.2016.
 */
public class CommandManager {

    private static CommandManager instance = null;

    public static CommandManager get() {
        return instance == null ? instance = new CommandManager() : instance;
    }

    public void onCommand(final Object sender, final String cmd, final String[] args) {
        Universal.get().getMethods().runAsync(() -> {
            String lowerArg = "";
            if (args.length > 0) {
                lowerArg = args[0].toLowerCase();
            }

            PunishmentType pt = PunishmentType.fromCommandName(cmd);
            MethodInterface mi = Universal.get().getMethods();
            if (pt != null) {
                if (Universal.get().hasPerms(sender, pt.getPerms())) {
                    boolean isTemp = pt.isTemp();
                    boolean silent = false;
                    int argsLength = (isTemp ? 2 : 1);
                    if (args.length >= argsLength) {

                        if (!isTemp || (args[1].toLowerCase().matches("[1-9][0-9]*([wdhms]|mo)") || args[1].toLowerCase().matches("#.+"))) {
                            StringBuilder reason = null;

                            int reasonBegin = argsLength;
                            if (args.length > argsLength && args[argsLength].equalsIgnoreCase("-s")) {
                                reasonBegin++;
                                silent = true;
                            }

                            if (args.length > reasonBegin) {
                                reason = buildReason(args, reasonBegin, sender);
                                if (reason == null) {
                                    return;
                                }
                            }

                            String uuid;
                            if (pt != PunishmentType.IP_BAN && pt != PunishmentType.TEMP_IP_BAN) {
                                uuid = UUIDManager.get().getUUID(lowerArg);
                                if (uuid == null) {
                                    MessageManager.sendMessage(sender, "General.FailedFetch", true, "NAME", args[0]);
                                    return;
                                }
                            } else {
                                if (lowerArg.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                                    uuid = lowerArg;
                                } else {
                                    if (Universal.get().getIps().containsKey(lowerArg.toLowerCase())) {
                                        uuid = Universal.get().getIps().get(lowerArg.toLowerCase());
                                    } else {
                                        MessageManager.sendMessage(sender, "Ipban.IpNotCashed", true, "NAME", args[0]);
                                        return;
                                    }
                                }
                            }

                            long end = -1;
                            if (isTemp) {
                                end = TimeManager.getTime();
                                if (args[1].matches("#.+")) {
                                    if (!mi.contains(mi.getLayouts(), "Time." + args[1].substring(1))) {
                                        MessageManager.sendMessage(sender, "General.LayoutNotFound", true, "NAME", args[1].substring(1));
                                        return;
                                    }
                                    int i = PunishmentManager.get().getCalculationLevel(uuid, args[1].substring(1));
                                    List<String> timeLayout = mi.getStringList(mi.getLayouts(), "Time." + args[1].substring(1));

                                    String time = timeLayout.get(timeLayout.size() <= i ? timeLayout.size() - 1 : i);
                                    end = time.equalsIgnoreCase("perma") ? -1 : end + TimeManager.toMilliSec(time.toLowerCase());
                                } else {
                                    long toAdd = TimeManager.toMilliSec(args[1].toLowerCase());
                                    end += toAdd;
                                    if (!Universal.get().hasPerms(sender, "ab." + pt.getName() + ".dur.max")) {
                                        long max = -1;
                                        for (int i = 10; i >= 1; i--) {
                                            if (Universal.get().hasPerms(sender, "ab." + pt.getName() + ".dur." + i) && mi.contains(mi.getConfig(), "TempPerms." + i)) {
                                                max = mi.getLong(mi.getConfig(), "TempPerms." + i) * 1000;
                                                break;
                                            }
                                        }
                                        if (max != -1 && toAdd > max) {
                                            MessageManager.sendMessage(sender, pt.getConfSection() + ".MaxDuration", true, "MAX", max / 1000 + "");
                                            return;
                                        }
                                    }
                                }
                            }

                            if (!mi.isOnline(lowerArg) && pt == PunishmentType.KICK) {
                                MessageManager.sendMessage(sender, "Kick.NotOnline", true, "NAME", args[0]);
                                return;
                            }

                            if ((mi.isOnline(lowerArg) && Universal.get().hasPerms(mi.getPlayer(lowerArg), "ab." + pt.getName() + ".exempt"))
                                    || Universal.get().isExemptPlayer(lowerArg)) {
                                MessageManager.sendMessage(sender, pt.getBasic().getConfSection() + ".Exempt", true, "NAME", args[0]);
                                return;
                            }

                            if ((pt.getBasic() == PunishmentType.MUTE && PunishmentManager.get().isMuted(uuid))
                                    || (pt.getBasic() == PunishmentType.BAN && PunishmentManager.get().isBanned(uuid))) {
                                MessageManager.sendMessage(sender, pt.getBasic().getConfSection() + ".AlreadyDone", true, "NAME", args[0]);
                                return;
                            }

                            new Punishment(args[0], uuid, reason != null ? reason.toString() : null, mi.getName(sender), isTemp && end == -1 ? PunishmentType.BAN : pt, TimeManager.getTime(), end, isTemp && args[1].matches("#.+") ? args[1].substring(1) : null, -1).create(silent);
                            MessageManager.sendMessage(sender, pt.getBasic().getConfSection() + ".Done", true, "NAME", args[0]);
                        } else {
                            MessageManager.sendMessage(sender, pt.getConfSection() + ".Usage", true);
                        }
                    } else {
                        MessageManager.sendMessage(sender, pt.getConfSection() + ".Usage", true);
                    }
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true);
                }
            } else if (cmd.toLowerCase().matches("un.+")) {
                pt = PunishmentType.fromCommandName(cmd.toLowerCase().substring(2));
                if (Universal.get().hasPerms(sender, "ab." + (pt == null ? "all" : pt.getName()) + ".undo")) {
                    if (args.length == 1 || (pt == PunishmentType.WARNING && args.length == 2)) {
                        if (pt != null && pt != PunishmentType.WARNING) {
                            String uuid = lowerArg;
                            if (!lowerArg.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                                uuid = UUIDManager.get().getUUID(lowerArg);
                                if (uuid == null) {
                                    MessageManager.sendMessage(sender, "General.FailedFetch", true, "NAME", args[0]);
                                    return;
                                }
                            }

                            Punishment pnt = pt == PunishmentType.MUTE ? PunishmentManager.get().getMute(uuid) : PunishmentManager.get().getBan(uuid);
                            if (pnt != null) {
                                pnt.delete();
                                MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".Done", true, "NAME", args[0]);
                            } else {
                                MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".NotPunished", true, "NAME", args[0]);
                            }
                        } else {
                            if (pt != null && lowerArg.equalsIgnoreCase("clear") && args.length == 2) {
                                String uuid = UUIDManager.get().getUUID(args[1]);
                                if (uuid == null) {
                                    MessageManager.sendMessage(sender, "General.FailedFetch", true, "NAME", args[0]);
                                    return;
                                }
                                List<Punishment> ptn = PunishmentManager.get().getWarns(uuid);
                                if (!ptn.isEmpty()) {
                                    for (Punishment punishment : ptn) {
                                        punishment.delete(mi.getName(sender), true, true);
                                    }
                                    MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".Clear.Done", true, "COUNT", String.valueOf(ptn.size()));
                                } else {
                                    MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".Clear.Empty", true, "NAME", args[1]);
                                }
                            } else if (lowerArg.matches("[0-9]+")) {
                                Punishment pnt = pt == null ? PunishmentManager.get().getPunishment(Integer.valueOf(lowerArg)) : PunishmentManager.get().getWarn(Integer.valueOf(lowerArg));
                                if (pnt != null) {
                                    pnt.delete();
                                    MessageManager.sendMessage(sender, "Un" + (pt == null ? "Punish" : pt.getConfSection()) + ".Done", true, "ID", lowerArg);
                                } else {
                                    MessageManager.sendMessage(sender, "Un" + (pt == null ? "Punish" : pt.getConfSection()) + ".NotFound", true, "ID", lowerArg);
                                }
                            } else {
                                MessageManager.sendMessage(sender, "Un" + (pt == null ? "Punish" : pt.getConfSection()) + ".Usage", true);
                            }
                        }
                    } else {
                        MessageManager.sendMessage(sender, "Un" + (pt == null ? "Punish" : pt.getConfSection()) + ".Usage", true);
                    }
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true);
                }
            } else if (cmd.equalsIgnoreCase("change-reason")) {
                if (Universal.get().hasPerms(sender, "ab.changeReason")) {
                    Punishment punishment;
                    int reasonStart;
                    if (args.length > 1 && lowerArg.matches("[0-9]*")) {
                        punishment = PunishmentManager.get().getPunishment(Integer.parseInt(lowerArg));
                        reasonStart = 1;
                    } else if (args.length > 2 && lowerArg.toLowerCase().matches("mute|ban")) {
                        reasonStart = 2;
                        if (!args[1].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                            args[1] = UUIDManager.get().getUUID(args[1]);
                        }

                        if (lowerArg.equalsIgnoreCase("ban")) {
                            punishment = PunishmentManager.get().getBan(args[1]);
                        } else {
                            punishment = PunishmentManager.get().getMute(args[1]);
                        }
                    } else {
                        MessageManager.sendMessage(sender, "ChangeReason.Usage", true);
                        return;
                    }

                    StringBuilder reason = buildReason(args, reasonStart, sender);
                    if (reason != null) {
                        if (punishment != null) {
                            punishment.updateReason(reason.toString());
                            MessageManager.sendMessage(sender, "ChangeReason.Done", true, "ID", String.valueOf(punishment.getId()));
                        } else {
                            MessageManager.sendMessage(sender, "ChangeReason.NotFound", true);
                        }
                    }
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true);
                }
            } else if (cmd.equalsIgnoreCase("banlist")) {
                if (Universal.get().hasPerms(sender, "ab.banlist")) {
                    if (args.length == 0 || (args.length == 1 && lowerArg.matches("[1-9][0-9]*"))) {
                        performList(sender, args.length == 0 ? 1 : Integer.valueOf(lowerArg), "Banlist", PunishmentManager.get().getPunishments(SQLQuery.SELECT_ALL_PUNISHMENTS_LIMIT, 150), "nope", false);
                    } else {
                        MessageManager.sendMessage(sender, "Banlist.Usage", true);
                    }
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true);
                }
            } else if (cmd.equalsIgnoreCase("history")) {
                if (Universal.get().hasPerms(sender, "ab.history")) {
                    if (args.length == 1 || (args.length == 2 && args[1].matches("[1-9][0-9]*"))) {
                        String uuid = UUIDManager.get().getUUID(lowerArg);
                        if (uuid == null) {
                            MessageManager.sendMessage(sender, "General.FailedFetch", true, "NAME", args[0]);
                            return;
                        }
                        performList(sender, args.length == 1 ? 1 : Integer.valueOf(args[1]), "History", PunishmentManager.get().getPunishments(uuid, null, false), lowerArg, true);
                    } else {
                        MessageManager.sendMessage(sender, "History.Usage", true);
                    }
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true);
                }
            } else if (cmd.equalsIgnoreCase("warns")) {
                String name = mi.getName(sender);
                int page = 1;
                if (args.length == 0 || (args.length == 1 && lowerArg.matches("[1-9][0-9]*"))) {
                    if (!Universal.get().hasPerms(sender, "ab.warns.own")) {
                        MessageManager.sendMessage(sender, "General.NoPerms", true);
                        return;
                    } else if (args.length == 1) {
                        page = Integer.valueOf(lowerArg);
                    }
                } else if (args.length == 1 || (args.length == 2 && args[1].matches("[1-9][0-9]*"))) {
                    if (!Universal.get().hasPerms(sender, "ab.warns.other")) {
                        MessageManager.sendMessage(sender, "General.NoPerms", true);
                        return;
                    } else if (args.length == 2) {
                        page = Integer.valueOf(args[1]);
                    }
                    name = lowerArg;
                } else {
                    MessageManager.sendMessage(sender, "Warns.Usage", true);
                    return;
                }
                String uuid = UUIDManager.get().getUUID(name);
                if (uuid == null) {
                    MessageManager.sendMessage(sender, "General.FailedFetch", true, "NAME", args[0]);
                    return;
                }
                performList(sender, page, "Warns", PunishmentManager.get().getPunishments(uuid, PunishmentType.WARNING, true), name, false);
            } else if (cmd.equalsIgnoreCase("check")) {
                if (Universal.get().hasPerms(sender, "ab.check")) {
                    if (args.length == 1) {
                        try {
                            String uuid;
                            Punishment punishment = getPunishment(lowerArg);
                            if (punishment != null && !punishment.isExpired()) {
                                uuid = UUIDManager.get().getUUID(PunishmentManager.get().getPunishment(Integer.parseInt(lowerArg)).getName());
                            } else {
                                uuid = UUIDManager.get().getUUID(lowerArg.toLowerCase());
                            }
                            if (uuid == null) {
                                MessageManager.sendMessage(sender, "General.FailedFetch", true, "NAME", args[0]);
                                return;
                            }
                            String ip = Universal.get().getIps().containsKey(lowerArg.toLowerCase()) ? Universal.get().getIps().get(lowerArg).toLowerCase() : "none cashed";
                            String loc = mi.getFromUrlJson("http://freegeoip.net/json/" + ip, "country_name");
                            Punishment mute = PunishmentManager.get().getMute(uuid);
                            Punishment ban = PunishmentManager.get().getBan(uuid);

                            MessageManager.sendMessage(sender, "Check.Header", true, "NAME", punishment != null ? punishment.getName() : lowerArg);
                            MessageManager.sendMessage(sender, "Check.UUID", false, "UUID", uuid);
                            if (Universal.get().hasPerms(sender, "ab.check.ip")) {
                                MessageManager.sendMessage(sender, "Check.IP", false, "IP", ip);
                            }
                            MessageManager.sendMessage(sender, "Check.Geo", false, "LOCATION", loc == null ? "failed!" : loc);
                            MessageManager.sendMessage(sender, "Check.Mute", false, "DURATION", mute == null ? "§anone" : mute.getType().isTemp() ? "§e" + mute.getDuration(false) : "§cperma");
                            if (mute != null) {
                                MessageManager.sendMessage(sender, "Check.MuteReason", false, "REASON", mute.getReason());
                            }
                            MessageManager.sendMessage(sender, "Check.Ban", false, "DURATION", ban == null ? "§anone" : ban.getType().isTemp() ? "§e" + ban.getDuration(false) : "§cperma");
                            if (ban != null) {
                                MessageManager.sendMessage(sender, "Check.BanReason", false, "REASON", ban.getReason());
                            }
                            MessageManager.sendMessage(sender, "Check.Warn", false, "COUNT", PunishmentManager.get().getCurrentWarns(uuid) + "");
                        } catch (NullPointerException exc) {
                            MessageManager.sendMessage(sender, "Check.NotFound", true, "NAME", args[0]);
                        }
                    } else {
                        MessageManager.sendMessage(sender, "Check.Usage", true);
                    }
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true);
                }
            } else if (cmd.equalsIgnoreCase("systemPrefs")) {
                if (Universal.get().hasPerms(sender, "ab.systemprefs")) {
                    Calendar calendar = new GregorianCalendar();
                    mi.sendMessage(sender, "§c§lAdvancedBan v2 §cSystemPrefs");
                    mi.sendMessage(sender, "§cServer-Time §8» §7" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
                    mi.sendMessage(sender, "§cYour UUID (Intern) §8» §7" + mi.getInternUUID(sender));
                    if (args.length == 1) {
                        mi.sendMessage(sender, "§c" + args[0] + "'s UUID (Intern) §8» §7" + mi.getInternUUID(lowerArg));
                        mi.sendMessage(sender, "§c" + args[0] + "'s UUID (Fetched) §8» §7" + UUIDManager.get().getUUID(lowerArg));
                    }
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true);
                }
            } else if (cmd.equalsIgnoreCase("advancedban")) {
                if (args.length == 0) {
                    mi.sendMessage(sender, "§8§l§m-=====§r §c§lAdvancedBan v2 §8§l§m=====-§r ");
                    mi.sendMessage(sender, "  §cDev §8• §7Leoko");
                    mi.sendMessage(sender, "  §cStatus §8• §a§oStable");
                    mi.sendMessage(sender, "  §cVersion §8• §7" + mi.getVersion());
                    mi.sendMessage(sender, "  §cLicense §8• §7Public");
                    mi.sendMessage(sender, "  §cStorage §8• §7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)"));
                    mi.sendMessage(sender, "  §cServer §8• §7" + (Universal.get().isBungee() ? "Bungeecord" : "Spigot/Bukkit"));
                    if (Universal.get().isBungee()) {
                        mi.sendMessage(sender, "  §cRedisBungee §8• §7" + (Universal.get().useRedis() ? "true" : "false"));
                    }
                    mi.sendMessage(sender, "  §cUUID-Mode §8• §7" + UUIDManager.get().getMode());
                    mi.sendMessage(sender, "  §cPrefix §8• §7" + (mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix")));
                    mi.sendMessage(sender, "§8§l§m-=========================-§r ");
                } else if (lowerArg.equalsIgnoreCase("reload")) {
                    if (Universal.get().hasPerms(sender, "ab.reload")) {
                        mi.loadFiles();
                        mi.sendMessage(sender, "§a§lAdvancedBan §8§l» §7Reloaded!");
                    } else {
                        MessageManager.sendMessage(sender, "General.NoPerms", true);
                    }
                } else if (lowerArg.equalsIgnoreCase("help")) {
                    if (Universal.get().hasPerms(sender, "ab.help")) {
                        mi.sendMessage(sender, "§8");
                        mi.sendMessage(sender, "§c§lAdvancedBan §7Command-Help");
                        mi.sendMessage(sender, "§8");
                        mi.sendMessage(sender, "§c/ban [Name] [Reason/@Layout]");
                        mi.sendMessage(sender, "§8» §7Ban a user permanently");
                        mi.sendMessage(sender, "§c/banip [Name/IP] [Reason/@Layout]");
                        mi.sendMessage(sender, "§8» §7Ban a user by IP");
                        mi.sendMessage(sender, "§c/tempban [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
                        mi.sendMessage(sender, "§8» §7Ban a user temporary");
                        mi.sendMessage(sender, "§c/mute [Name] [Reason/@Layout]");
                        mi.sendMessage(sender, "§8» §7Mute a user permanently");
                        mi.sendMessage(sender, "§c/tempmute [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
                        mi.sendMessage(sender, "§8» §7Mute a user temporary");
                        mi.sendMessage(sender, "§c/warn [Name] [Reason/@Layout]");
                        mi.sendMessage(sender, "§8» §7Warn a user permanently");
                        mi.sendMessage(sender, "§c/tempwarn [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
                        mi.sendMessage(sender, "§8» §7Warn a user temporary");
                        mi.sendMessage(sender, "§c/kick [Name] [Reason/@Layout]");
                        mi.sendMessage(sender, "§8» §7Kick a user");
                        mi.sendMessage(sender, "§c/unban [Name/IP]");
                        mi.sendMessage(sender, "§8» §7Unban a user");
                        mi.sendMessage(sender, "§c/unmute [Name]");
                        mi.sendMessage(sender, "§8» §7Unmute a user");
                        mi.sendMessage(sender, "§c/unwarn [ID] or /unwarn clear [Name]");
                        mi.sendMessage(sender, "§8» §7Deletes a warn");
                        mi.sendMessage(sender, "§c/change-reason [ID or ban/mute USER] [New reason]");
                        mi.sendMessage(sender, "§8» §7Changes the reason of a punishment");
                        mi.sendMessage(sender, "§c/unpunish [ID]");
                        mi.sendMessage(sender, "§8» §7Deletes a punishment by ID");
                        mi.sendMessage(sender, "§c/banlist <Page>");
                        mi.sendMessage(sender, "§8» §7See all punishments");
                        mi.sendMessage(sender, "§c/history [Name/IP] <Page>");
                        mi.sendMessage(sender, "§8» §7See a users history");
                        mi.sendMessage(sender, "§c/warns [Name] <Page>");
                        mi.sendMessage(sender, "§8» §7See your or a users wa");
                        mi.sendMessage(sender, "§c/check [Name]");
                        mi.sendMessage(sender, "§8» §7Get all information about a user");
                        mi.sendMessage(sender, "§c/AdvancedBan <reload/help>");
                        mi.sendMessage(sender, "§8» §7Reloads the plugin or shows help page");
                        mi.sendMessage(sender, "§8");
                    } else {
                        MessageManager.sendMessage(sender, "General.NoPerms", true);
                    }
                }
            } else {
                mi.sendMessage(sender, "§cHm wired :/");
            }
        });
    }

    private StringBuilder buildReason(String[] args, int reasonBegin, Object sender) {
        MethodInterface mi = Universal.get().getMethods();
        StringBuilder reason = new StringBuilder();
        for (int i = reasonBegin; i < args.length; i++) {
            reason.append(" ").append(args[i]);
        }
        if (!args[reasonBegin].matches("@.+") && !args[reasonBegin].matches("~.+")) {
            return new StringBuilder(reason.substring(1));
        } else {
            if (!mi.contains(mi.getLayouts(), "Message." + args[reasonBegin].substring(1))) {
                MessageManager.sendMessage(sender, "General.LayoutNotFound", true, "NAME", args[reasonBegin].substring(1));
                return null;
            }
            return new StringBuilder(reason.substring(1));
        }
    }

    private void performList(Object sender, int cPage, String confName, List<Punishment> pnts, String name, boolean history) {
        MethodInterface mi = Universal.get().getMethods();
        if (pnts.isEmpty()) {
            MessageManager.sendMessage(sender, confName + ".NoEntries", true, "NAME", name);
            return;
        }
        for (Punishment pnt : pnts) {
            if (pnt.isExpired()) {
                pnt.delete();
            }
        }
        if (pnts.size() / 5.0 + 1 > cPage) {
            for (String str : MessageManager.getLayout(mi.getMessages(), confName + ".Header", "PREFIX", mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix"), "NAME", name)) {
                mi.sendMessage(sender, str);
            }

            SimpleDateFormat format = new SimpleDateFormat(mi.getString(mi.getConfig(), "DateFormat", "dd.MM.yyyy-HH:mm"));
            for (int i = (cPage - 1) * 5; i < cPage * 5 && pnts.size() > i; i++) {
                Punishment pnt = pnts.get(i);
                for (String str : MessageManager.getLayout(mi.getMessages(), confName + ".Entry",
                        "PREFIX", mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix"),
                        "NAME", pnt.getName(),
                        "DURATION", pnt.getDuration(history),
                        "OPERATOR", pnt.getOperator(),
                        "REASON", pnt.getReason(),
                        "TYPE", pnt.getType().getConfSection(),
                        "ID", pnt.getId() + "",
                        "DATE", format.format(new Date(pnt.getStart())))) {
                    mi.sendMessage(sender, str);
                }
            }
            MessageManager.sendMessage(sender, confName + ".Footer", false,
                    "CURRENT_PAGE", cPage + "",
                    "TOTAL_PAGES", (pnts.size() / 5 + (pnts.size() % 5 != 0 ? 1 : 0)) + "",
                    "COUNT", pnts.size() + "");
            if (pnts.size() / 5.0 + 1 > cPage + 1) {
                MessageManager.sendMessage(sender, confName + ".PageFooter", false,
                        "NEXT_PAGE", (cPage + 1) + "", "NAME", name);
            }
        } else {
            MessageManager.sendMessage(sender, confName + ".OutOfIndex", true, "PAGE", cPage + "");
        }
    }

    private Punishment getPunishment(String id) {
        try {
            Integer.parseInt(id);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
        return PunishmentManager.get().getPunishment(Integer.parseInt(id));
    }
}
