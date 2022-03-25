/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.plugin.cache.base.redis;

import org.apache.shenyu.common.utils.Singleton;
import org.apache.shenyu.plugin.cache.base.ICache;
import org.apache.shenyu.plugin.cache.base.config.CacheConfig;
import org.apache.shenyu.plugin.cache.base.redis.serializer.ShenyuRedisSerializationContext;
import org.apache.shenyu.spi.Join;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.time.Duration;
import java.util.Objects;

/**
 * RedisCache.
 */
@Join
public final class RedisCache implements ICache {

    private ReactiveRedisTemplate<String, byte[]> redisTemplate;

    public RedisCache() {
        this.prepareRedis();
    }

    /**
     * init redis connection
     */
    private void prepareRedis() {
        final CacheConfig cacheConfig = Singleton.INST.get(CacheConfig.class);
        final RedisConnectionFactory redisConnectionFactory = new RedisConnectionFactory(cacheConfig);
        this.redisTemplate = new ReactiveRedisTemplate<>(redisConnectionFactory.getLettuceConnectionFactory()
                , ShenyuRedisSerializationContext.bytesSerializationContext());
    }

    /**
     * Cache the data with the key.
     * @param key the cache key
     * @param bytes the data
     * @param timeoutSeconds value valid time
     * @return success or not
     */
    @Override
    public boolean cacheData(final String key, final byte[] bytes, final long timeoutSeconds) {
        return Boolean.TRUE.equals(this.redisTemplate.opsForValue().set(key, bytes, Duration.ofSeconds(timeoutSeconds)).block());
    }

    /**
     * Check the cache is exist or not.
     * @param key the cache key
     * @return true exist
     */
    @Override
    public boolean isExist(final String key) {
        return Boolean.TRUE.equals(this.redisTemplate.hasKey(key).block());
    }

    /**
     * Get data with the key.
     * @param key the cache key
     * @return the data
     */
    @Override
    public byte[] getData(final String key) {
        return this.redisTemplate.opsForValue().get(key).block();
    }

    /**
     * close the redis cache.
     */
    private void close() {

        if (Objects.isNull(this.redisTemplate)) {
            return;
        }
        final ReactiveRedisConnectionFactory connectionFactory = this.redisTemplate.getConnectionFactory();
        try {
            ReactiveRedisConnection connection = connectionFactory.getReactiveConnection();
            connection.close();
            connection = connectionFactory.getReactiveClusterConnection();
            connection.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * refresh the cache.
     */
    @Override
    public void refresh() {

        this.close();
        prepareRedis();
    }
}
