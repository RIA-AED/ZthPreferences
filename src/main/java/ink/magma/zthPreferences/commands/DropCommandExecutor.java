package ink.magma.zthPreferences.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ink.magma.zthPreferences.PlayerPreferenceManager;
import ink.magma.zthPreferences.PreferenceType;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DropCommandExecutor implements TabExecutor {
    private final PlayerPreferenceManager preferenceManager;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DropCommandExecutor(PlayerPreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            Boolean result = this.preferenceManager.togglePreference(player.getUniqueId(), PreferenceType.DROP_ITEMS);
            String displayName = PreferenceType.DROP_ITEMS.getDisplayName();
            player.sendMessage(miniMessage.deserialize(String.format("<white>设置 <aqua>%s <white>已切换为：%s",
                    displayName, result ? "<green>启用" : "<red>禁用")));
        } else {
            sender.sendMessage(miniMessage.deserialize("<red>只有玩家可以使用此命令！"));
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
