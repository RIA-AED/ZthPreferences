package ink.magma.zthPreferences;

import ink.magma.zthPreferences.commands.DropCommandExecutor;
import ink.magma.zthPreferences.commands.PreferenceCommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class ZthPreferences extends JavaPlugin {
    private JedisPool jedisPool;
    private PlayerPreferenceManager preferenceManager;
    private PreferenceListener preferenceListener;

    @Override
    public void onEnable() {
        load();
        getLogger().info("ZthPreferences 插件已启用！");
    }

    @Override
    public void onDisable() {
        unload();
        getLogger().info("ZthPreferences 插件已禁用！");
    }

    public void load() {
        // 保存默认配置
        saveDefaultConfig();

        // 初始化 Redis 连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(getConfig().getInt("redis.maxTotal", 128));
        jedisPool = new JedisPool(
                poolConfig,
                getConfig().getString("redis.host", "localhost"),
                getConfig().getInt("redis.port", 6379),
                getConfig().getInt("redis.timeout", 2000)
        );

        // 初始化设置管理器
        preferenceManager = new PlayerPreferenceManager(jedisPool);

        // 注册命令处理器
        this.getCommand("preferences").setExecutor(new PreferenceCommandExecutor(preferenceManager, this));
        this.getCommand("drop").setExecutor(new DropCommandExecutor(preferenceManager));

        // 注册事件监听器
        this.preferenceListener = new PreferenceListener(this, preferenceManager);
        getServer().getPluginManager().registerEvents(this.preferenceListener, this);
    }

    public void unload() {
        // 注销事件监听器
        if (this.preferenceListener != null) {
            org.bukkit.event.HandlerList.unregisterAll(this.preferenceListener);
        }

        // 关闭 Redis 连接池
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

    public void reload() {
        unload();
        reloadConfig();
        load();
        getLogger().info("ZthPreferences 插件已重载！");
    }
}
