package com.wdxxs2z.gateway.extention.routeStore.config;

import com.wdxxs2z.gateway.extention.routeStore.controller.GatewayRouteController;
import com.wdxxs2z.gateway.extention.routeStore.filter.ApiKeyResolver;
import com.wdxxs2z.gateway.extention.routeStore.routeService.DynamicRouteService;
import com.wdxxs2z.gateway.extention.routeStore.storeRepository.RedisRouteDefinitionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class storeConfig {

    @ConditionalOnProperty(prefix = "gateway.store.redis", name = "enable", havingValue = "true")
    @Bean(name = {"redisTemplate", "stringRedisTemplate"})
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }

    @Bean
    @ConditionalOnProperty(prefix = "gateway.store.redis", name = "enable", havingValue = "true")
    public GatewayRouteController gatewayRouteController() {
        return new GatewayRouteController();
    }

    @Bean
    @ConditionalOnProperty(prefix = "gateway.store.redis", name = "enable", havingValue = "true")
    public ApiKeyResolver apiKeyResolver() {
        return new ApiKeyResolver();
    }

    @Bean
    @ConditionalOnProperty(prefix = "gateway.store.redis", name = "enable", havingValue = "true")
    public DynamicRouteService dynamicRouteService() {
        return new DynamicRouteService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "gateway.store.redis", name = "enable", havingValue = "true")
    public RedisRouteDefinitionRepository redisRouteDefinitionRepository(){
        return new RedisRouteDefinitionRepository();
    }
}
