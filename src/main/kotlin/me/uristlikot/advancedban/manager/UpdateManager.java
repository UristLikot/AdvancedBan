package me.uristlikot.advancedban.manager;

import me.uristlikot.advancedban.MethodInterface;
import me.uristlikot.advancedban.Universal;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Leo on 07.08.2017.
 */
public class UpdateManager {

    private static UpdateManager instance = null;

    public static UpdateManager get() {
        return instance == null ? instance = new UpdateManager() : instance;
    }

    public void setup() {
        MethodInterface mi = Universal.get().getMethods();

        if(mi.isUnitTesting()) return;

        if (!mi.contains(mi.getMessages(), "Check.MuteReason")) {
            try {
                File file = new File(mi.getDataFolder(), "Messages.yml");
                List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
                int index = lines.indexOf("Check:");
                lines.add(index + 1, "  MuteReason: \"  &cReason &8\\xbb &7%REASON%\"");
                FileUtils.writeLines(file, lines);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!mi.contains(mi.getMessages(), "Check.BanReason")) {
            try {
                File file = new File(mi.getDataFolder(), "Messages.yml");
                List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
                int index = lines.indexOf("Check:");
                lines.add(index + 1, "  BanReason: \"  &cReason &8\\xbb &7%REASON%\"");
                FileUtils.writeLines(file, lines);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!mi.contains(mi.getMessages(), "Tempipban")) {
            try {
                FileUtils.writeLines(new File(mi.getDataFolder(), "Messages.yml"), Arrays.asList(
                        "",
                        "Tempipban:",
                        "  Usage: \"&cUsage &8\\xbb &7&o/tempipban [Name/IP] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]\"",
                        "  MaxDuration: \"&cYou are not able to ban more than %MAX%sec\"",
                        "  Layout:",
                        "  - '%PREFIX% &7Temporarily banned'",
                        "  - '&7'",
                        "  - '&7'",
                        "  - \"&cReason &8\\xbb &7%REASON%\"",
                        "  - \"&cDuration &8\\xbb &7%DURATION%\"",
                        "  - '&7'",
                        "  - '&8Unban application in TS or forum'",
                        "  - \"&eTS-Ip &8\\xbb &c&ncoming soon\"",
                        "  - \"&eForum &8\\xbb &c&ncoming soon\"",
                        "  Notification:",
                        "  - \"&c&o%NAME% &7got banned by &e&o%OPERATOR%\"",
                        "  - \"&7For the reason &o%REASON%\"",
                        "  - \"&7&oThis player got banned for &e&o%DURATION%\"",
                        "",
                        "ChangeReason:",
                        "  Usage: \"&cUsage &8\\xbb &7&o/change-reason [ID or ban/mute USER] [New reason]\"",
                        "  Done: \"&7Punishment &a&o#%ID% &7has successfully been updated!\"",
                        "  NotFound: \"&cSorry we have not been able to find this punishment\""), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            File file = new File(mi.getDataFolder(), "config.yml");
            List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
            if (!mi.contains(mi.getConfig(), "EnableAllPermissionNodes")) {

                lines.remove("  # Disable for cracked servers");

                int indexOf = lines.indexOf("UUID-Fetcher:");
                if (indexOf != -1) {
                    lines.addAll(indexOf + 1, Arrays.asList(
                            "  # If dynamic it set to true it will override the 'enabled' and 'intern' settings",
                            "  # and automatically detect the best possible uuid fetcher settings for your server.",
                            "  # Our recommendation: don't set dynamic to false if you don't have any problems.",
                            "  Dynamic: true"));
                }

                lines.addAll(Arrays.asList("",
                        "# This is useful for bungeecord servers or server with permission systems which do not support *-Perms",
                        "# So if you enable this you can use ab.all instead of ab.* or ab.ban.all instead of ab.ban.*",
                        "# This does not work with negative permissions! e.g. -ab.all would not block all commands for that user.",
                        "EnableAllPermissionNodes: false"));
            }
            if (!mi.contains(mi.getConfig(), "Debug")) {
                lines.addAll(Arrays.asList(
                        "",
                        "# With this active will show more information in the console, such as errors, if",
                        "# the plugin works correctly is not recommended to activate it since it is",
                        "# designed to find bugs.",
                        "Debug: false"));
            }
            if (mi.contains(mi.getConfig(), "Logs Purge Days")) {
                lines.removeAll(Arrays.asList(
                        "",
                        "# This is the amount of days that we should keep plugin logs in the plugins/AdvancedBan/logs folder.",
                        "# By default is set to 10 days.",
                        "Logs Purge Days: 10"
                ));
            }
            if (!mi.contains(mi.getConfig(), "Log Purge Days")) {
                lines.addAll(Arrays.asList(
                        "",
                        "# This is the amount of days that we should keep plugin logs in the plugins/AdvancedBan/logs folder.",
                        "# By default is set to 10 days.",
                        "Log Purge Days: 10"
                ));
            }
            if (!mi.contains(mi.getConfig(), "Disable Prefix")) {
                lines.addAll(Arrays.asList(
                        "",
                        "# Removes the prefix of the plugin in every message.",
                        "Disable Prefix: false"
                ));
            }
            FileUtils.writeLines(file, lines);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}
