package com.hansight.springbootmybatis2.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * RedisUtil, common operations with redis, StringRedisTemplate instance created by spring boot framework
 */
@Component
public class RedisUtil {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {

        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(final String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0)
            redisTemplate.delete(keys);
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        Object result = operations.get(key);
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value.toString());
            result = true;
        } catch (Exception e) {
            logger.warn("write {} to redis failed, {}", key, e.getMessage());
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value.toString());
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.warn("write {} to redis failed, {}", key, e.getMessage());
        }
        return result;
    }

    /**
     * Description: get values from redis with key patten
     *
     * @Date: 2018/3/21
     */
    public Set<String> getKeysByPattern(final String pattern){
        try {
            return redisTemplate.keys(pattern);
        }catch (Exception e){
            logger.warn("get {} from redis failed, {}", pattern, e.getMessage());
            return null;
        }
    }

    /**
     * Description: write key with values to redis, if value is empty, sadd throws exception
     *
     * @Date: 2018/3/21
     */
    public Long putSet(final String key, final String[] value){
        if(value.length <=0) return 0L;
        try {
            return redisTemplate.opsForSet().add(key, value);
        }
        catch (Exception e){
            logger.warn("write {} to redis failed, {}", key, e.getMessage());
            return -1L;
        }
    }

    /**
     * Description: get values from redis with key
     *
     * @Date: 2018/3/21
     */
    public Set<String> getSet(final String key){
        try {
            return redisTemplate.opsForSet().members(key);
        }
        catch (Exception e){
            logger.warn("get {} from redis failed, {}", key, e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Description: get values from redis with key patten
     *
     * @Date: 2018/3/21
     */
    public List<Set<String>> getSetByPattern(final String pattern){
        List<Set<String>> retList = new ArrayList<>();
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            keys.forEach(k->{
                Set<String> value = getSet(k);
                if(value != null) {
                    retList.add(value);
                }
            });
        }catch (Exception e){
            logger.warn("get {} from redis failed, {}", pattern, e.getMessage());
        }
        return retList;
    }

    /**
     * Description: get map value from redis with key
     *
     * @Date: 2018/3/21
     */
    public Map<Object, Object> getHash(final String key){
        try {
            return redisTemplate.opsForHash().entries(key);
        }catch (Exception e){
            logger.warn("get {} from redis failed, {}", key, e.getMessage());
            return null;
        }
    }

    /*public List<Map<String, String>> getHashByPattern(final String pattern){
        List<Map<String, String>> retList = new ArrayList<>();
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            keys.forEach(k->{
                Map<Object, Object> value = getHash(k);
                if(value != null) {
                    retList.add(value.toString());
                }
            });
        }catch (Exception e){
            logger.warn(e.getMessage());
        }
        return retList;
    }
*/

    /**
     * Description: get value from redis with key patten and attr name
     *
     * @Date: 2018/3/21
     */
    public String getHashAttr(final String key, final String attr){
        try {
            Object o =  redisTemplate.opsForHash().get(key, attr);
            if(o != null){
                return o.toString();
            }
            else {
                return null;
            }
        } catch (Exception e){
            logger.warn("get {} from redis failed, {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Description: rename key
     *
     * @Date: 2018/3/21
     */
    public void rename(String oldKey, String newKey) {
        try {
            redisTemplate.rename(oldKey, newKey);
        } catch (Exception e){
            logger.warn("rename {} to {} in redis failed, {}", oldKey, newKey, e.getMessage());
        }
    }

    /**
     * Description: publish msg via redis
     *
     * @Date: 2018/3/21
     */
    public void publisher(String channel, String msg) {
        try {
            redisTemplate.convertAndSend(channel, msg);
        } catch (Exception e){
            logger.warn("publish msg {} with channel {} in redis failed, {}", msg, channel, e.getMessage());
        }
    }
}