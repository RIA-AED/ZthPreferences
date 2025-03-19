package ink.magma.zthPreferences;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class PreferenceListener implements Listener {
    private final ZthPreferences plugin;
    private final PlayerPreferenceManager preferenceManager;

    public PreferenceListener(ZthPreferences plugin, PlayerPreferenceManager preferenceManager) {
        this.plugin = plugin;
        this.preferenceManager = preferenceManager;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        boolean allowDrop = preferenceManager.getPreference(playerId, PreferenceType.DROP_ITEMS.getKey());

        if (!allowDrop) {
            event.setCancelled(true);
            Component message = Component.text()
                    .content("物品丢弃已被您关闭, 使用 /drop 切换")
                    .color(PreferenceType.DROP_ITEMS.getColor())
                    .decoration(TextDecoration.ITALIC, false)
                    .build();
            plugin.getServer().getPlayer(playerId).sendActionBar(message);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检查是否是耕地变成泥土
        if (event.getAction() == Action.PHYSICAL) {

            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            boolean allowTrample = preferenceManager.getPreference(playerId, PreferenceType.TRAMPLE_CROPS.getKey());

            if (!allowTrample && event.getClickedBlock().getType() == Material.FARMLAND) {
                event.setCancelled(true);
                Component message = Component.text()
                        .content("您已关闭耕地踩踏")
                        .color(PreferenceType.TRAMPLE_CROPS.getColor())
                        .decoration(TextDecoration.ITALIC, false)
                        .build();
                player.sendActionBar(message);
            }

        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerId = player.getUniqueId();
            boolean allowPickup = preferenceManager.getPreference(playerId, PreferenceType.PICKUP_ITEMS.getKey());

            if (!allowPickup) {
                event.setCancelled(true);
                Component message = Component.text()
                        .content("物品拾取已被您关闭")
                        .color(PreferenceType.PICKUP_ITEMS.getColor())
                        .decoration(TextDecoration.ITALIC, false)
                        .build();
                player.sendActionBar(message);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 初始化玩家默认设置
        UUID playerId = event.getPlayer().getUniqueId();
        Map<String, String> prefs = preferenceManager.getAllPreferences(playerId);

        if (prefs.isEmpty()) {
            // 使用循环设置所有偏好设置的默认值
            for (PreferenceType preference : PreferenceType.values()) {
                preferenceManager.setPreference(playerId, preference.getKey(), preference.getDefaultValue());
            }
        }
    }
}