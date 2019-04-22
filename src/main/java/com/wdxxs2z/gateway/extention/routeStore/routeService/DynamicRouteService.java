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
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
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
    public ResponseResult add(GatewayRouteDefinition gatewayRouteDefinition){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(gatewayRouteDefinition.getId());
        routeDefinition.setUri(
                UriComponentsBuilder.fromUriString(
                        gatewayRouteDefinition.getHostUrl()
                ).build().toUri()
        );
        routeDefinition.setPredicates(gatewayRouteDefinition.getPredicates());
        routeDefinition.setFilters(gatewayRouteDefinition.getFilters());
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        notifyChanged();
        return new ResponseResult(HttpStatus.OK.value(), "路由创建成功", gatewayRouteDefinition);
    }

    // 删除路由
    public ResponseResult delete(String routeId) {
        routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        return new ResponseResult(HttpStatus.OK.value(), "路由删除成功", routeId);
    }

    // 更新路由
    public ResponseResult update(GatewayRouteDefinition gatewayRouteDefinition) {
        this.delete(gatewayRouteDefinition.getId());
        this.add(gatewayRouteDefinition);
        return new ResponseResult(HttpStatus.OK.value(), "路由更新成功", gatewayRouteDefinition);
    }

    // 获取一条路由
    public Mono<GatewayRouteDefinition> getOne(String routeId){
        Mono<RouteDefinition> RouteDefinition = routeDefinitionWriter.findOne(Mono.just(routeId));
        return RouteDefinition.filter(r -> r!=null).flatMap(r -> {
            GatewayRouteDefinition gatewayRouteDefinition = new GatewayRouteDefinition();
            gatewayRouteDefinition.setId(routeId);
            gatewayRouteDefinition.setFilters(r.getFilters());
            gatewayRouteDefinition.setHostUrl(r.getUri().toString());
            gatewayRouteDefinition.setPredicates(r.getPredicates());
            return Mono.just(gatewayRouteDefinition);
        });
    }

    // 获取所有路由
    public Flux<RouteDefinition> getRoutes() {
        return routeDefinitionWriter.getRouteDefinitions();
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
