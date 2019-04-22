package com.wdxxs2z.gateway.extention.routeStore.controller;

import com.wdxxs2z.gateway.extention.routeStore.domain.GatewayRouteDefinition;
import com.wdxxs2z.gateway.extention.routeStore.domain.ResponseResult;
import com.wdxxs2z.gateway.extention.routeStore.routeService.DynamicRouteService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 {
 "id": "com.wdxxs2z.gateway.bundle.callMessage",
 "hostUrl": "127.0.0.1:9987",
 "predicates": [
 {"name": "Path","args": {"pattern": "/jd"}}
 ],
 "filters": [
 {"name": "AddRequestHeader","args": {"_genkey_0": "header","_genkey_1": "addHeader"}},
 {"name": "AddRequestParameter", "args": {"_genkey_0": "param","_genkey_1": "addParam"}}
 ]
 }
 * */
@RestController
public class GatewayRouteController {

    @Resource
    private DynamicRouteService routeService;

    @Resource
    private RouteDefinitionLocator routeDefinitionLocator;

    @Resource
    private RouteLocator routeLocator;

    @PostMapping("/routes")
    public Mono<ResponseResult> add(@RequestBody GatewayRouteDefinition gatewayRouteDefinition) {
        return Mono.just(routeService.add(gatewayRouteDefinition));
    }

    @GetMapping("/routes")
    public Mono<List<Map<String, Object>>> getAllRoutes() {
        Mono<Map<String, RouteDefinition>> routeDefs = this.routeDefinitionLocator.getRouteDefinitions()
                .collectMap(RouteDefinition::getId);
        Mono<List<Route>> routes = this.routeLocator.getRoutes().collectList();
        return Mono.zip(routeDefs, routes).map(tuple -> {
            Map<String, RouteDefinition> defs = tuple.getT1();
            List<Route> routeList = tuple.getT2();
            List<Map<String, Object>> allRoutes = new ArrayList<>();
            routeList.forEach(route -> {
                HashMap<String, Object> r = new HashMap<>();
                r.put("route_id", route.getId());
                r.put("order", route.getOrder());
                if (defs.containsKey(route.getId())) {
                    r.put("route_definition", defs.get(route.getId()));
                } else {
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("predicate", route.getPredicate().toString());
                    if (!route.getFilters().isEmpty()) {
                        ArrayList<String> filters = new ArrayList<>();
                        for (GatewayFilter filter : route.getFilters()) {
                            filters.add(filter.toString());
                        }
                        obj.put("filters", filters);
                    }
                    if (!obj.isEmpty()) {
                        r.put("route_object", obj);
                    }
                }
                allRoutes.add(r);
            });
            return allRoutes;
        });
    }

    @DeleteMapping("/routes/{routeId}")
    public Mono<ResponseResult> delete(@PathVariable(name = "routeId") String routeId) {
        return Mono.just(routeService.delete(routeId));
    }

    @PostMapping(value = "/routes/{routeId}")
    public Mono<ResponseResult> update(@PathVariable(name = "routeId") String routeId,
                                 @RequestBody GatewayRouteDefinition gatewayRouteDefinition) {

        return routeService.getOne(routeId)
                .flatMap(route -> {
                    route.setFilters(gatewayRouteDefinition.getFilters());
                    route.setPredicates(gatewayRouteDefinition.getPredicates());
                    route.setHostUrl(gatewayRouteDefinition.getHostUrl());
                    return Mono.just(routeService.update(route));
                })
                .map(updateRoute -> new ResponseResult(HttpStatus.OK.value(),"更新完成",routeId))
                .defaultIfEmpty(new ResponseResult(HttpStatus.NOT_FOUND.value(), "更新失败", routeId));
    }

    @PostMapping(value = "/routes/cleanCache")
    public boolean cleanCache(@RequestParam String key) {
        return routeService.delAll(key);
    }
}
