package ink.magma.zthPreferences;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;

public class PreferenceListener implements Listener {
    private final PlayerPreferenceManager preferenceManager;

    public PreferenceListener(PlayerPreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        boolean allowDrop = preferenceManager.getPreference(playerId, "drop_items");
        
        if (!allowDrop) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c你已禁用物品丢弃功能！");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检查是否踩踏耕地
        if (event.getClickedBlock() != null && 
            event.getClickedBlock().getType() == Material.FARMLAND) {
            
            UUID playerId = event.getPlayer().getUniqueId();
            boolean allowTrample = preferenceManager.getPreference(playerId, "trample_crops");
            
            if (!allowTrample) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§c你已禁用踩坏耕地功能！");
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
            preferenceManager.setPreference(playerId, "drop_items", true);
            preferenceManager.setPreference(playerId, "trample_crops", true);
        }
    }
}