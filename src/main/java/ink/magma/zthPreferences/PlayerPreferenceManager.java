package ink.magma.zthPreferences;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.UUID;

public class PlayerPreferenceManager {
    private final JedisPool jedisPool;
    private static final String PREFIX = "zth-preferences:";

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
            String value = jedis.hget(key, preference);
            return value != null ? "true".equals(value) : PreferenceType.fromKey(preference)
                .map(PreferenceType::getDefaultValue)
                .orElse(false);
        }
    }

    /**
     * 获取玩家设置
     *
     * @param playerId   玩家UUID
     * @param preference 偏好类型
     * @return 设置值，如果不存在则返回false
     */
    public boolean getPreference(UUID playerId, PreferenceType preference) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = PREFIX + playerId.toString();
            String value = jedis.hget(key, preference.getKey());
            return value != null ? "true".equals(value) : preference.getDefaultValue();
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
     * 设置玩家偏好
     *
     * @param playerId   玩家UUID
     * @param preference 偏好类型
     * @param value      设置值
     */
    public void setPreference(UUID playerId, PreferenceType preference, boolean value) {
        setPreference(playerId, preference.getKey(), value);
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
    /**
     * 切换玩家设置
     *
     * @param playerId   玩家UUID
     * @param preference 设置名称
     * @return 切换后的设置值
     */
    public boolean togglePreference(UUID playerId, String preference) {
        boolean currentValue = getPreference(playerId, preference);
        setPreference(playerId, preference, !currentValue);
        return !currentValue;
    }

    /**
     * 切换玩家设置
     *
     * @param playerId   玩家UUID
     * @param preference 偏好类型
     * @return 切换后的设置值
     */
    public boolean togglePreference(UUID playerId, PreferenceType preference) {
        return togglePreference(playerId, preference.getKey());
    }

    /**
     * 取消玩家偏好设置，恢复默认值
     *
     * @param playerId   玩家UUID
     * @param preference 偏好设置名称
     */
    public void unsetPreference(UUID playerId, String preference) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = PREFIX + playerId.toString();
            jedis.hdel(key, preference);
        }
    }

    /**
     * 取消玩家偏好设置，恢复默认值
     *
     * @param playerId   玩家UUID
     * @param preference 偏好类型
     */
    public void unsetPreference(UUID playerId, PreferenceType preference) {
        unsetPreference(playerId, preference.getKey());
    }
}