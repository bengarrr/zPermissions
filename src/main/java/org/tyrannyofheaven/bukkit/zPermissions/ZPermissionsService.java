/*
 * Copyright 2012 Allan Saddi <allan@saddi.com>
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Programmatic interface of read-only operations for things that are not easily
 * done solely by command line (i.e. would involve parsing command output, which
 * would be messy).
 * 
 * @author asaddi
 */
public interface ZPermissionsService {

    /**
     * Retrieve names of all players with permissions defined.
     * 
     * @return set of players known by zPermissions, not including those who are
     *   only group members
     */
    public Set<String> getAllPlayers();

    /**
     * Resolve a player's permissions for the given world and region set. The
     * returned map is ultimately what zPermissions uses to create the player's
     * Bukkit attachment.
     * 
     * <p>Note that Bukkit does some additional magic behind the scenes, specifically
     * regarding default permission values (which are determined by the
     * e.g. plugin.yml of the plugin that owns the permission).
     * 
     * <p>In other words, just looking at the returned effective permissions will
     * <strong>not</strong> replicate the behavior of hasPermissions()! You
     * must take defaults into account. I leave that to the caller...
     * 
     * @param worldName the name of the target world
     * @param regionNames set of region names. May be <code>null</code>.
     * @param playerName the player's name
     * @return effective permissions for this player
     */
    public Map<String, Boolean> getPlayerPermissions(String worldName, Set<String> regionNames, String playerName);
    
    /**
     * Retrieve groups which a player is explicitly assigned. The groups are
     * returned in priority order, with the highest priority first. (This can
     * possibly be considered the player's "primary" group.)
     * 
     * @param playerName the player's name
     * @return the names of groups which the player is assigned to
     */
    public List<String> getPlayerAssignedGroups(String playerName);

    /**
     * Retrieve groups which a player is a member of. This includes all
     * assigned groups as well as their ancestor groups.
     * 
     * @param playerName the player's name
     * @return the names of groups which the player is a member of
     */
    public Set<String> getPlayerGroups(String playerName);

    /**
     * Retrieve names of all groups.
     * 
     * @return names of all groups known by zPermissions
     */
    public Set<String> getAllGroups();

    /**
     * Resolve a group's permissions for the given world and region set.
     * 
     * <p>The returned map contains permissions which are explicitly defined by
     * the group or its ancestors.
     * 
     * <p>You <strong>must</strong> properly handle defaults do something like
     * Bukkit's hasPermissions() method. See {@link #getPlayerPermissions(String, Set, String)}
     * for further explanation.
     * 
     * @param worldName the name of the target world
     * @param regionNames set of region names. May be <code>null</code>.
     * @param groupName the group's name
     * @return effective permissions for this group
     */
    public Map<String, Boolean> getGroupPermissions(String worldName, Set<String> regionNames, String groupName);

    /**
     * Retrieve the names of the players that are members of the given group.
     * 
     * @param groupName the group's name
     * @return the group's members
     */
    public Set<String> getGroupMembers(String groupName);

}
