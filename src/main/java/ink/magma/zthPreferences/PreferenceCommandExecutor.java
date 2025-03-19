package ink.magma.zthPreferences;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PreferenceCommandExecutor implements CommandExecutor, TabCompleter {
    // 偏好设置显示名称映射
    private static final Map<String, String> PREFERENCE_DISPLAY_NAMES = Map.of(
            "drop_items", "丢弃物品",
            "trample_crops", "踩踏耕地");

    private final PlayerPreferenceManager preferenceManager;

    public PreferenceCommandExecutor(PlayerPreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
    }

    /**
     * 处理玩家输入的命令
     *
     * @param sender  命令发送者
     * @param command 命令对象
     * @param label   命令别名
     * @param args    命令参数
     * @return 命令是否处理成功
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String @NotNull [] args) {
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
            player.sendMessage("§c你还没有设置任何偏好！");
            return true;
        }

        player.sendMessage("§6===== 你的当前设置 =====");
        prefs.forEach((key, value) -> {
            String displayName = PREFERENCE_DISPLAY_NAMES.getOrDefault(key, key);
            player.sendMessage(String.format("§a%s: §f%s", displayName, value));
        });
        return true;
    }

    private String getInternalName(String input) {
        // 先尝试直接匹配
        if (PREFERENCE_DISPLAY_NAMES.containsValue(input)) {
            return PREFERENCE_DISPLAY_NAMES.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(input))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElse(input);
        }
        return input;
    }

    /**
     * 切换玩家偏好设置
     *
     * @param player     执行命令的玩家
     * @param playerId   玩家UUID
     * @param preference 偏好设置名称（支持显示名称或内部名称）
     * @return 操作是否成功
     */
    private boolean togglePreference(Player player, UUID playerId, String preference) {
        String internalName = getInternalName(preference);

        if (!PREFERENCE_DISPLAY_NAMES.containsKey(internalName)) {
            player.sendMessage("§c无效的设置项！可用设置：");
            PREFERENCE_DISPLAY_NAMES.forEach(
                    (key, displayName) -> player.sendMessage(String.format("§7- %s (§f%s§7)", displayName, key)));
            return true;
        }

        boolean currentValue = preferenceManager.getPreference(playerId, internalName);
        boolean newValue = !currentValue;
        preferenceManager.setPreference(playerId, internalName, newValue);

        String displayName = PREFERENCE_DISPLAY_NAMES.get(internalName);
        player.sendMessage(String.format("§a设置 %s 已切换为：%s", displayName, newValue ? "§a启用" : "§c禁用"));
        return true;
    }

    /**
     * 显示命令帮助信息
     *
     * @param player 接收帮助信息的玩家
     * @return 总是返回 true
     */
    private boolean showHelp(Player player) {
        player.sendMessage("§6===== 偏好设置帮助 =====");
        player.sendMessage("§a/pref show - 显示当前设置");
        player.sendMessage("§a/pref toggle <setting> - 切换指定设置");
        player.sendMessage("§a/pref help - 显示此帮助信息");
        player.sendMessage("§e注意：设置项可以使用显示名称或内部名称");
        player.sendMessage("§a可用设置：");
        PREFERENCE_DISPLAY_NAMES
                .forEach((key, displayName) -> player.sendMessage(String.format("§7- %s (§f%s§7)", displayName, key)));
        player.sendMessage("§6======================");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            String[] args) {
        if (args.length == 1) {
            return List.of("show", "toggle");
        }

        if (args.length == 2 && "toggle".equalsIgnoreCase(args[0])) {
            return PREFERENCE_DISPLAY_NAMES.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().startsWith(args[1].toLowerCase()) ||
                            entry.getValue().toLowerCase().startsWith(args[1].toLowerCase()))
                    .map(entry -> String.format("%s (%s)", entry.getValue(), entry.getKey()))
                    .toList();
        }

        return List.of();
    }
}