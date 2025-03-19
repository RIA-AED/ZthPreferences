package ink.magma.zthPreferences;

import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class ZthPreferences extends JavaPlugin {
    private JedisPool jedisPool;
    private PlayerPreferenceManager preferenceManager;

    @Override
    public void onEnable() {
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
        this.getCommand("pref").setExecutor(new PreferenceCommandExecutor(preferenceManager));

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PreferenceListener(this, preferenceManager), this);

        getLogger().info("ZthPreferences 插件已启用！");
    }

    @Override
    public void onDisable() {
        // 关闭 Redis 连接池
        if (jedisPool != null) {
            jedisPool.close();
        }
        getLogger().info("ZthPreferences 插件已禁用！");
    }
}
