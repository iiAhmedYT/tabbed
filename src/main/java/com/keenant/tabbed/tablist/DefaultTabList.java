package com.keenant.tabbed.tablist;

import com.comphenix.protocol.events.PacketContainer;
import com.keenant.tabbed.Tabbed;
import com.keenant.tabbed.TabbedPlugin;
import com.keenant.tabbed.item.PlayerTabItem;
import com.keenant.tabbed.item.TabItem;
import gnu.trove.impl.hash.TByteIntHash;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class DefaultTabList extends CustomTabList implements Listener {
    private Map<Player,String> names = new HashMap<>();

    private int taskId;

    public DefaultTabList(Tabbed tabbed, Player player, int maxItems) {
        super(tabbed, player, maxItems, -1, -1);
    }

    @Override
    @Deprecated
    public void add(TabItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void add(int index, TabItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void set(int index, TabItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DefaultTabList enable() {
        super.enable();
        this.tabbed.getPlugin().getServer().getPluginManager().registerEvents(this, this.tabbed.getPlugin());

        for (Player target : Bukkit.getOnlinePlayers())
            addPlayer(target);

        // Because there is no PlayerListNameUpdateEvent in Bukkit
        this.taskId = this.tabbed.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(this.tabbed.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!names.containsKey(target))
                        continue;

                    String prevName = names.get(target);
                    String currName = target.getPlayerListName();

                    if (prevName.equals(currName))
                        continue;

                    int index = getTabItemIndex(target);
                    update(index);
                    names.put(target, currName);
                }
            }
        }, 0, 5);

        return this;
    }

    @Override
    public DefaultTabList disable() {
        super.disable();
        HandlerList.unregisterAll(this);
        this.tabbed.getPlugin().getServer().getScheduler().cancelTask(this.taskId);
        return this;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        super.remove(getTabItemIndex(event.getPlayer()));
    }

    private void addPlayer(Player player) {
        super.add(getInsertLocation(player), new PlayerTabItem(player));
        this.names.put(player, player.getPlayerListName());
    }

    private int getTabItemIndex(Player player) {
        for (Entry<Integer,TabItem> item : this.items.entrySet()) {
            // items will always be players in this case, cast is safe
            PlayerTabItem tabItem = (PlayerTabItem) item.getValue();
            if (tabItem.getPlayer().equals(player))
                return item.getKey();
        }
        return -1;
    }

    private int getInsertLocation(Player player) {
        for (Entry<Integer,TabItem> item : this.items.entrySet()) {
            // items will always be players in this case, cast is safe
            PlayerTabItem tabItem = (PlayerTabItem) item.getValue();

            if (player.getName().compareTo(tabItem.getPlayer().getName()) < 0)
                return item.getKey();
        }
        return getNextIndex();
    }
}