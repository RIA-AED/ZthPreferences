package ink.magma.zthPreferences;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;

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
        // 检查是否踩踏耕地
        if (event.getClickedBlock() != null &&
            event.getClickedBlock().getType() == Material.FARMLAND) {
            
            UUID playerId = event.getPlayer().getUniqueId();
            boolean allowTrample = preferenceManager.getPreference(playerId, PreferenceType.TRAMPLE_CROPS.getKey());
            
            if (!allowTrample) {
                event.setCancelled(true);
                Component message = Component.text()
                    .content("您已关闭耕地踩踏")
                    .color(PreferenceType.TRAMPLE_CROPS.getColor())
                    .decoration(TextDecoration.ITALIC, false)
                    .build();
                plugin.getServer().getPlayer(playerId).sendActionBar(message);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 初始化玩家默认设置
        UUID playerId = event.getPlayer().getUniqueId();
        Map<String, String> prefs = preferenceManager.getAllPreferences(playerId);
        
        if (prefs.isEmpty()) {
            // 设置默认值
            preferenceManager.setPreference(playerId, PreferenceType.DROP_ITEMS.getKey(), true);
            preferenceManager.setPreference(playerId, PreferenceType.TRAMPLE_CROPS.getKey(), true);
        }
    }
}