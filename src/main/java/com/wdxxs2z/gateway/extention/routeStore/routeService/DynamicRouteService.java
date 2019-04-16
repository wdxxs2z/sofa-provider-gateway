package com.wdxxs2z.gateway.extention.routeStore.routeService;

import com.wdxxs2z.gateway.extention.routeStore.domain.GatewayRouteDefinition;
import com.wdxxs2z.gateway.extention.routeStore.domain.ResponseResult;
import com.wdxxs2z.gateway.extention.routeStore.storeRepository.RedisRouteDefinitionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@ConditionalOnProperty(prefix = "gateway.store.redis", name = "enable", havingValue = "true")
public class DynamicRouteService implements ApplicationEventPublisherAware {

    @Resource
    private RedisRouteDefinitionRepository routeDefinitionWriter;

    @Resource
    private RedisTemplate redisTemplate;

    private ApplicationEventPublisher publisher;

    // 通知刷新路由表
    private void notifyChanged() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    // 添加路由
    public ResponseResult add(GatewayRouteDefinition routeDefinition){
        RouteDefinition rd = new RouteDefinition();
        rd.setId(routeDefinition.getId());
        rd.setUri(UriComponentsBuilder.fromUriString(
                routeDefinition.getHostUrl()).build().toUri()
        );
        rd.setPredicates(routeDefinition.getPredicates());
        rd.setFilters(routeDefinition.getFilters());
        routeDefinitionWriter.save(Mono.just(rd)).subscribe();
        notifyChanged();
        return new ResponseResult();
    }

    // 删除路由
    public ResponseResult delete(String routeId) {
        routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        return new ResponseResult();
    }

    // 更新路由
    public ResponseResult update(GatewayRouteDefinition routeDefinition) {
        this.delete(routeDefinition.getId());
        this.add(routeDefinition);
        return new ResponseResult();
    }

    // 删除所有路由
    public boolean delAll(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
