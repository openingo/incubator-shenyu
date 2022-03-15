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

package org.apache.shenyu.plugin.cache.write.handler;
import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.convert.rule.impl.CacheWriteRuleHandle;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.plugin.base.cache.CommonHandleCache;
import org.apache.shenyu.plugin.base.handler.PluginDataHandler;
import org.apache.shenyu.plugin.base.utils.BeanHolder;
import org.apache.shenyu.plugin.base.utils.CacheKeyUtils;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The type Cache write plugin data handler.
 */
public class CacheWritePluginDataHandler implements PluginDataHandler {
    
    public static final Supplier<CommonHandleCache<String, CacheWriteRuleHandle>> CACHED_HANDLE = new BeanHolder<>(CommonHandleCache::new);

    @Override
    public void handlerRule(final RuleData ruleData) {
        Optional.ofNullable(ruleData.getHandle()).ifPresent(json -> {
            final CacheWriteRuleHandle cacheWriteRuleHandle = GsonUtils.getInstance().fromJson(json, CacheWriteRuleHandle.class);
            CACHED_HANDLE.get().cachedHandle(CacheKeyUtils.INST.getKey(ruleData), cacheWriteRuleHandle);
        });
    }

    @Override
    public void removeRule(final RuleData ruleData) {
        Optional.ofNullable(ruleData.getHandle()).ifPresent(json -> CACHED_HANDLE.get().removeHandle(CacheKeyUtils.INST.getKey(ruleData)));
    }

    @Override
    public String pluginNamed() {
        return PluginEnum.CACHE_WRITE.getName();
    }
}
