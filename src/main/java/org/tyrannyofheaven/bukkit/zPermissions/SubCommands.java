/*
 * Copyright 2011 Allan Saddi <allan@saddi.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tyrannyofheaven.bukkit.zPermissions;

import static org.tyrannyofheaven.bukkit.util.ToHLoggingUtils.log;
import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.colorize;
import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.sendMessage;
import static org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils.requirePermission;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.tyrannyofheaven.bukkit.util.command.Command;
import org.tyrannyofheaven.bukkit.util.command.CommandSession;
import org.tyrannyofheaven.bukkit.util.command.HelpBuilder;
import org.tyrannyofheaven.bukkit.util.command.Option;
import org.tyrannyofheaven.bukkit.util.command.ParseException;
import org.tyrannyofheaven.bukkit.util.command.Require;
import org.tyrannyofheaven.bukkit.util.command.reader.CommandReader;
import org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback;
import org.tyrannyofheaven.bukkit.zPermissions.model.PermissionEntity;

/**
 * Handler for sub-commands of /permissions
 * 
 * @author asaddi
 */
public class SubCommands {

    // Parent plugin
    private final ZPermissionsPlugin plugin;

    // The "/permissions player" handler
    private final PlayerCommands playerCommand;

    // The "/permissions group" handler
    private final GroupCommands groupCommand;

    // The "/permissions cache" handler
    private final CacheCommands cacheCommand;

    SubCommands(ZPermissionsPlugin plugin) {
        this.plugin = plugin;
        playerCommand = new PlayerCommands(plugin);
        groupCommand = new GroupCommands(plugin);
        cacheCommand = new CacheCommands(plugin);
    }

    @Command(value={"player", "pl", "p"}, description="Player-related commands")
    @Require("zpermissions.player")
    public PlayerCommands player(HelpBuilder helpBuilder, CommandSender sender, CommandSession session, @Option(value="player", nullable=true) String playerName, String[] args) {
        if (args.length == 0) {
            // Display sub-command help
            helpBuilder.withCommandSender(sender)
                .withHandler(playerCommand)
                .forCommand("get")
                .forCommand("set")
                .forCommand("unset")
                .forCommand("settemp")
                .forCommand("purge")
                .forCommand("groups")
                .forCommand("setgroup")
                .forCommand("show")
                .show();
            return null;
        }
        
        // Stuff name into session for next handler
        session.setValue("entityName", playerName);
        return playerCommand;
    }

    @Command(value={"group", "gr", "g"}, description="Group-related commands")
    @Require("zpermissions.group")
    public CommonCommands group(HelpBuilder helpBuilder, CommandSender sender, CommandSession session, @Option(value="group", nullable=true) String groupName, String[] args) {
        if (args.length == 0) {
            // Display sub-command help
            helpBuilder.withCommandSender(sender)
                .withHandler(groupCommand)
                .forCommand("create")
                .forCommand("get")
                .forCommand("set")
                .forCommand("unset")
                .forCommand("purge")
                .forCommand("members")
                .forCommand("setparent")
                .forCommand("setpriority")
                .forCommand("add")
                .forCommand("remove")
                .forCommand("show")
                .show();
            return null;
        }

        // Stuff name into session for next handler
        session.setValue("entityName", groupName);
        return groupCommand;
    }

    @Command(value={"list", "ls"}, description="List players or groups in the database")
    @Require("zpermissions.list")
    public void list(CommandSender sender, @Option("what") String what) {
        boolean group;
        if ("groups".startsWith(what)) {
            group = true;
        }
        else if ("players".startsWith(what)) {
            group = false;
        }
        else {
            throw new ParseException("<what> should be 'groups' or 'players'");
        }

        List<PermissionEntity> entities = plugin.getDao().getEntities(group);

        if (entities.isEmpty()) {
            sendMessage(sender, colorize("{YELLOW}No %s found."), group ? "groups" : "players");
        }
        else {
            for (PermissionEntity entity : entities) {
                sendMessage(sender, colorize("{DARK_GREEN}- %s"), entity.getDisplayName());
            }
        }
    }

    @Command(value="check", description="Check against effective permissions")
    @Require("zpermissions.check")
    public void check(CommandSender sender, @Option("permission") String permission, @Option(value="player", optional=true) String playerName) {
        Player player;
        if (playerName == null) {
            // No player specified
            if (!(sender instanceof Player)) {
                sendMessage(sender, colorize("{RED}Cannot check permissions of console."));
                return;
            }
            // Use sender
            player = (Player)sender;
        }
        else {
            // Checking perms for another player
            requirePermission(sender, "zpermissions.check.other");

            player = plugin.getServer().getPlayer(playerName);
            if (player == null) {
                sendMessage(sender, colorize("{RED}Player is not online."));
                return;
            }
        }

        // Scan effective permissions
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if (permission.equalsIgnoreCase(pai.getPermission())) {
                sendMessage(sender, colorize("{AQUA}%s{YELLOW} sets {GOLD}%s{YELLOW} to {GREEN}%s"), player.getName(), pai.getPermission(), pai.getValue());
                return;
            }
        }
        sendMessage(sender, colorize("{AQUA}%s{YELLOW} does not set {GOLD}%s"), player.getName(), permission);
    }

    @Command(value="reload", description="Re-read config.yml")
    @Require("zpermissions.reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        sendMessage(sender, colorize("{WHITE}config.yml{YELLOW} reloaded"));
    }

    // Ensure filename doesn't have any funny characters
    private File sanitizeFilename(File dir, String filename) {
        String[] parts = filename.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
        if (parts.length == 1) {
            if (!parts[0].startsWith("."))
                return new File(dir, filename);
        }
        throw new ParseException("Invalid filename.");
    }

    @Command(value={"import", "restore"}, description="Import a dump of the database")
    @Require("zpermissions.import")
    public void import_command(final CommandSender sender, @Option("filename") String filename) {
        File inFile = sanitizeFilename(plugin.getDumpDirectory(), filename);
        try {
            // Ensure database is empty
            if (!plugin.getTransactionStrategy().execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction() throws Exception {
                    // Check in a single transaction
                    List<PermissionEntity> players = plugin.getDao().getEntities(false);
                    List<PermissionEntity> groups = plugin.getDao().getEntities(true);
                    if (!players.isEmpty() || !groups.isEmpty()) {
                        sendMessage(sender, colorize("{RED}Database is not empty!"));
                        return false;
                    }
                    return true;
                }
            })) {
                return;
            }

            // Execute commands
            if (CommandReader.read(plugin.getServer(), sender, inFile, plugin)) {
                sendMessage(sender, colorize("{YELLOW}Import complete."));
            }
            else {
                sendMessage(sender, colorize("{RED}Import failed."));
            }
        }
        catch (IOException e) {
            sendMessage(sender, colorize("{RED}Error importing; see server log."));
            log(plugin, Level.SEVERE, "Error importing:", e);
        }
    }
    
    @Command(value={"export", "dump"}, description="Export a dump of the database")
    @Require("zpermissions.export")
    public void export(CommandSender sender, @Option("filename") String filename) {
        File outFile = sanitizeFilename(plugin.getDumpDirectory(), filename);
        try {
            if (!plugin.getDumpDirectory().exists()) {
                if (!plugin.getDumpDirectory().mkdirs()) {
                    sendMessage(sender, colorize("{RED}Unable to create dump directory"));
                    return;
                }
            }
            plugin.getModelDumper().dump(outFile);
            sendMessage(sender, colorize("{YELLOW}Export completed."));
        }
        catch (IOException e) {
            sendMessage(sender, colorize("{RED}Error exporting; see server log."));
            log(plugin, Level.SEVERE, "Error exporting:", e);
        }
    }

    @Command(value="cache", description="Cache-related commands")
    @Require("zpermissions.cache")
    public Object cache(HelpBuilder helpBuilder, CommandSender sender, String[] args) {
        if (args.length == 0) {
            helpBuilder.withCommandSender(sender)
                .withHandler(cacheCommand)
                .forCommand("monitor")
                .forCommand("clear")
                .show();
            return null;
        }
        
        return cacheCommand;
    }

}
