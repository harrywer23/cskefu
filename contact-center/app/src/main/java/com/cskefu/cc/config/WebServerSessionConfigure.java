/*
 * Copyright (C) 2019-2022 Chatopera Inc, <https://www.chatopera.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cskefu.cc.config;

import com.cskefu.cc.basic.auth.AuthRedisTemplate;
import com.cskefu.cc.cache.RedisKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.FlushMode;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.time.Duration;


/**
 * maxInactiveIntervalInSeconds: 设置 Session 失效时间，
 * 使用 Redis Session 之后，原 Spring Boot 的 server.session.timeout 属性不再生效。
 * http://www.ityouknow.com/springboot/2016/03/06/spring-boot-redis.html
 * 86400 代表一天
 * maxInactiveIntervalInSeconds = 86400 * 30
 */

@Configuration
public class WebServerSessionConfigure {

    /**
     * spring在多长时间后强制使redis中的session失效,默认是1800.(单位/秒)
     */
    @Value("${server.session-timeout}")
    private long maxInactiveIntervalInSeconds;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String pass;

    @Value("${spring.redis.session.db}")
    private int sessionDb;

    @Value("${spring.redis.token.db}")
    private int tokenDb;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Primary
    @Bean
    // TODO lecjy
    public RedisSessionRepository sessionRepository(RedisTemplate<String, Object> sessionRedisTemplate) {
        RedisSessionRepository sessionRepository = new RedisSessionRepository(sessionRedisTemplate);
        sessionRepository.setDefaultMaxInactiveInterval(Duration.ofSeconds(maxInactiveIntervalInSeconds));
        sessionRepository.setFlushMode(FlushMode.IMMEDIATE);
        sessionRepository.setRedisKeyNamespace(RedisKey.CACHE_SESSIONS);
        return sessionRepository;
    }

    @Bean
    public RedisTemplate<String, Object> sessionRedisTemplate() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(host);
        factory.setPort(port);
        factory.setDatabase(sessionDb);
        if (StringUtils.isNotBlank(pass)) {
            factory.setPassword(pass);
        }
        factory.setTimeout(timeout);
        factory.afterPropertiesSet();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setConnectionFactory(factory);
        return template;
    }


    /**
     * 存储AuthToken
     * @return
     */
    @Bean
    public AuthRedisTemplate authRedisTemplate() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(host);
        factory.setPort(port);
        factory.setDatabase(tokenDb);
        if (StringUtils.isNotBlank(pass)) {
            factory.setPassword(pass);
        }
        factory.setTimeout(timeout);
        factory.afterPropertiesSet();

        AuthRedisTemplate template = new AuthRedisTemplate();
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setConnectionFactory(factory);
        return template;
    }

}
