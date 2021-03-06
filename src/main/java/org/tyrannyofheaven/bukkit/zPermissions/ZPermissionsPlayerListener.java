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

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * PlayerListener for zPermissions. Simply updates or removes the zPermissions
 * attachment as appropriate.
 * 
 * @author asaddi
 */
class ZPermissionsPlayerListener implements Listener {

    private final ZPermissionsPlugin plugin;
    
    ZPermissionsPlayerListener(ZPermissionsPlugin plugin) {
        this.plugin = plugin;
    }

    void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        plugin.updateAttachment(event.getPlayer(), event.getPlayer().getLocation(), true, true);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removeAttachment(event.getPlayer().getName());
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.updateAttachment(event.getPlayer(), event.getPlayer().getLocation(), false, false);
    }

}
