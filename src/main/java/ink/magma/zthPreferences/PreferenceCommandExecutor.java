package ink.magma.zthPreferences;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PreferenceCommandExecutor implements CommandExecutor {
    private static final List<String> VALID_PREFERENCES = Arrays.asList(
            "drop_items",  // 是否允许丢弃物品
            "trample_crops" // 是否允许踩坏耕地
    );

    private final PlayerPreferenceManager preferenceManager;

    public PreferenceCommandExecutor(PlayerPreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此命令！");
            return true;
        }

        if (args.length == 0) {
            return showHelp(player);
        }

        UUID playerId = player.getUniqueId();
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "show" -> {
                return showPreferences(player, playerId);
            }
            case "toggle" -> {
                if (args.length < 2) {
                    player.sendMessage("用法: /pref toggle <setting>");
                    return true;
                }
                return togglePreference(player, playerId, args[1]);
            }
            default -> {
                return showHelp(player);
            }
        }
    }

    private boolean showPreferences(Player player, UUID playerId) {
        Map<String, String> prefs = preferenceManager.getAllPreferences(playerId);
        if (prefs.isEmpty()) {
            player.sendMessage("你还没有设置任何偏好！");
            return true;
        }

        player.sendMessage("§6===== 你的当前设置 =====");
        prefs.forEach((key, value) ->
                player.sendMessage(String.format("§a%s: §f%s", key, value))
        );
        return true;
    }

    private boolean togglePreference(Player player, UUID playerId, String preference) {
        if (!VALID_PREFERENCES.contains(preference)) {
            player.sendMessage("无效的设置项！可用设置：");
            VALID_PREFERENCES.forEach(p -> player.sendMessage(" - " + p));
            return true;
        }

        boolean currentValue = preferenceManager.getPreference(playerId, preference);
        boolean newValue = !currentValue;
        preferenceManager.setPreference(playerId, preference, newValue);

        player.sendMessage(String.format("§a设置 %s 已切换为：%s", preference, newValue));
        return true;
    }

    private boolean showHelp(Player player) {
        player.sendMessage("§6===== 偏好设置帮助 =====");
        player.sendMessage("§a/pref show - 显示当前设置");
        player.sendMessage("§a/pref toggle <setting> - 切换指定设置");
        player.sendMessage("§a可用设置：");
        VALID_PREFERENCES.forEach(p -> player.sendMessage(" - " + p));
        return true;
    }
}