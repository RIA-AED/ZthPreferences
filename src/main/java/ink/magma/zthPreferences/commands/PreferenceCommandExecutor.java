package ink.magma.zthPreferences.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import ink.magma.zthPreferences.PlayerPreferenceManager;
import ink.magma.zthPreferences.PreferenceType;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PreferenceCommandExecutor implements CommandExecutor, TabCompleter {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    
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
                    player.sendMessage(miniMessage.deserialize("<gray>用法: <white>/pref toggle <配置项>"));
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
            String displayName = Arrays.stream(PreferenceType.values())
                    .filter(type -> type.getKey().equals(key))
                    .findFirst()
                    .map(PreferenceType::getDisplayName)
                    .orElse(key);
            player.sendMessage(miniMessage.deserialize(String.format("<gray>%s: <white>%s", displayName, value)));
        });
        return true;
    }

    private PreferenceType getInternalName(String input) {
        return Arrays.stream(PreferenceType.values())
                .filter(type -> type.getDisplayName().equals(input) || type.getKey().equals(input))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid preference: " + input));
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
        try {
            PreferenceType preferenceType = getInternalName(preference);
            
            boolean currentValue = preferenceManager.getPreference(playerId, preferenceType);
            boolean newValue = !currentValue;
            preferenceManager.setPreference(playerId, preferenceType, newValue);

            player.sendMessage(miniMessage.deserialize(String.format("<white>设置 %s 已切换为：%s",
                preferenceType.getDisplayName(), newValue ? "<green>启用" : "<red>禁用")));
            return true;
        } catch (IllegalArgumentException e) {
            player.sendMessage(miniMessage.deserialize("<red>无效的设置项！可用设置："));
            Arrays.stream(PreferenceType.values()).forEach(type ->
                player.sendMessage(miniMessage.deserialize(String.format("<gray>- <white>%s <gray>(%s)",
                    type.getDisplayName(), type.getKey()))));
            return true;
        }
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
        player.sendMessage(miniMessage.deserialize("<gray>/pref toggle <配置项> - 切换指定设置"));
        player.sendMessage(miniMessage.deserialize("<gray>/pref help - 显示此帮助信息"));
        player.sendMessage(miniMessage.deserialize("<gray>注意：设置项可以使用显示名称或内部名称"));
        player.sendMessage(miniMessage.deserialize("<gray>可用设置："));
        Arrays.stream(PreferenceType.values())
                .forEach(type -> player.sendMessage(miniMessage.deserialize(String.format("<gray>- %s (<white>%s<gray>)",
                    type.getDisplayName(), type.getKey()))));
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
            return Arrays.stream(PreferenceType.values())
                    .filter(type -> type.getKey().toLowerCase().startsWith(args[1].toLowerCase()) ||
                            type.getDisplayName().toLowerCase().startsWith(args[1].toLowerCase()))
                    .map(type -> String.format("%s (%s)", type.getDisplayName(), type.getKey()))
                    .toList();
        }

        return List.of();
    }
}