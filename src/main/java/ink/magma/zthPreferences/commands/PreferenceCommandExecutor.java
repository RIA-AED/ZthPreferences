package ink.magma.zthPreferences;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    
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
            sender.sendMessage(miniMessage.deserialize("<red>只有玩家可以使用此命令！"));
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
                    player.sendMessage(miniMessage.deserialize("<gray>用法: <white>/pref toggle <aqua><配置项>"));
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
            player.sendMessage(miniMessage.deserialize("<red>你还没有设置任何偏好！"));
            return true;
        }

        player.sendMessage(miniMessage.deserialize("<white>===== <white>你的当前设置 <white>====="));
        prefs.forEach((key, value) -> {
            String displayName = PREFERENCE_DISPLAY_NAMES.getOrDefault(key, key);
            player.sendMessage(miniMessage.deserialize(String.format("<gray>%s: <white>%s", displayName, value)));
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
            player.sendMessage(miniMessage.deserialize("<red>无效的设置项！可用设置："));
            PREFERENCE_DISPLAY_NAMES.forEach(
                    (key, displayName) -> player.sendMessage(miniMessage.deserialize(String.format("<gray>- <white>%s <gray>(%s)", displayName, key))));
            return true;
        }

        boolean currentValue = preferenceManager.getPreference(playerId, internalName);
        boolean newValue = !currentValue;
        preferenceManager.setPreference(playerId, internalName, newValue);

        String displayName = PREFERENCE_DISPLAY_NAMES.get(internalName);
        player.sendMessage(miniMessage.deserialize(String.format("<white>设置 <aqua>%s <white>已切换为：%s",
            displayName, newValue ? "<green>启用" : "<red>禁用")));
        return true;
    }

    /**
     * 显示命令帮助信息
     *
     * @param player 接收帮助信息的玩家
     * @return 总是返回 true
     */
    private boolean showHelp(Player player) {
        player.sendMessage(miniMessage.deserialize("<white>===== <white>偏好设置帮助 <white>====="));
        player.sendMessage(miniMessage.deserialize("<gray>/pref show - 显示当前设置"));
        player.sendMessage(miniMessage.deserialize("<gray>/pref toggle <aqua><setting> <gray>- 切换指定设置"));
        player.sendMessage(miniMessage.deserialize("<gray>/pref help - 显示此帮助信息"));
        player.sendMessage(miniMessage.deserialize("<gray>注意：设置项可以使用显示名称或内部名称"));
        player.sendMessage(miniMessage.deserialize("<gray>可用设置："));
        PREFERENCE_DISPLAY_NAMES
                .forEach((key, displayName) -> player.sendMessage(miniMessage.deserialize(String.format("<gray>- %s (<white>%s<gray>)", displayName, key))));
        player.sendMessage(miniMessage.deserialize("<white>======================"));
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