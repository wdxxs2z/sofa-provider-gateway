package com.wdxxs2z.gateway.extention.routeStore.controller;

import com.wdxxs2z.gateway.extention.routeStore.domain.GatewayRouteDefinition;
import com.wdxxs2z.gateway.extention.routeStore.domain.ResponseResult;
import com.wdxxs2z.gateway.extention.routeStore.routeService.DynamicRouteService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class GatewayRouteController {

    @Resource
    private DynamicRouteService routeService;

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
    @RequestMapping(value = "/route/add", method = RequestMethod.POST)
    public ResponseResult add(@RequestBody GatewayRouteDefinition gatewayRouteDefinition) {
        return routeService.add(gatewayRouteDefinition);
    }

    @PostMapping(value = "/route/delete")
    public ResponseResult delete(@RequestParam String routeId) {
        return routeService.delete(routeId);
    }

    @PostMapping(value = "/route/update")
    public ResponseResult update(GatewayRouteDefinition gatewayRouteDefinition) {
        return routeService.update(gatewayRouteDefinition);
    }

    @PostMapping(value = "/route/cleanCache")
    public boolean cleanCache(@RequestParam String key) {
        return routeService.delAll(key);
    }
}
