package ink.magma.zthPreferences;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.UUID;

public class PlayerPreferenceManager {
    private final JedisPool jedisPool;
    private static final String PREFIX = "player_prefs:";

    public PlayerPreferenceManager(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 获取玩家设置
     *
     * @param playerId   玩家UUID
     * @param preference 设置名称
     * @return 设置值，如果不存在则返回false
     */
    public boolean getPreference(UUID playerId, String preference) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = PREFIX + playerId.toString();
            return "true".equals(jedis.hget(key, preference));
        }
    }

    /**
     * 设置玩家偏好
     *
     * @param playerId   玩家UUID
     * @param preference 设置名称
     * @param value      设置值
     */
    public void setPreference(UUID playerId, String preference, boolean value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = PREFIX + playerId.toString();
            jedis.hset(key, preference, String.valueOf(value));
        }
    }

    /**
     * 获取玩家所有设置
     *
     * @param playerId 玩家UUID
     * @return 包含所有设置的Map
     */
    public Map<String, String> getAllPreferences(UUID playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = PREFIX + playerId.toString();
            return jedis.hgetAll(key);
        }
    }
}