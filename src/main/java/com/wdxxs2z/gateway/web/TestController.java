package com.wdxxs2z.gateway.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wdxxs2z.gateway.adapt.sofa.SofaProtocolAdapt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private SofaProtocolAdapt sofaProtocolAdapt;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * {
     *   "interfaceName": "cloud.provider.facade.CallerService",
     *   "method": "datasource",
     *   "content": {
     *     "java.lang.String": "hello"
     *   }
     * }
     * */
    @RequestMapping(value = "/gateway", method = RequestMethod.POST)
    public Object revertRequest(@RequestBody String requestJson){

        Map<String, Object> requestObject = JSON.parseObject(requestJson, new TypeReference<Map<String, Object>>() {
        });

        String interfaceName = (String)requestObject.get("interfaceName");

        String method = (String)requestObject.get("method");

        List<Map<String, Object>> args = (List<Map<String, Object>>)requestObject.get("content");

        Object response = sofaProtocolAdapt.doGenericInvoke(interfaceName, method, args);

        return response;
    }

    @RequestMapping(value = "/test")
    public String testRedis() {
        RouteDefinition definition = new RouteDefinition();
        definition.setId("id");
        URI uri = UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:8888/header").build().toUri();
        // URI uri = UriComponentsBuilder.fromHttpUrl("http://baidu.com").build().toUri();
        definition.setUri(uri);

        //定义第一个断言
        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");

        Map<String, String> predicateParams = new HashMap<>(8);
        predicateParams.put("pattern", "/jd/**");
        predicate.setArgs(predicateParams);

        //定义Filter
        FilterDefinition filter = new FilterDefinition();
        filter.setName("AddRequestHeader");
        Map<String, String> filterParams = new HashMap<>(8);
        //该_genkey_前缀是固定的，见org.springframework.cloud.gateway.support.NameUtils类
        filterParams.put("_genkey_0", "header");
        filterParams.put("_genkey_1", "addHeader");
        filter.setArgs(filterParams);

        FilterDefinition filter1 = new FilterDefinition();
        filter1.setName("StripPrefix");
        Map<String, String> filter1Params = new HashMap<>(8);
        filter1Params.put("_genkey_0", "1");
        filter1.setArgs(filter1Params);

        definition.setFilters(Arrays.asList(filter, filter1));
        definition.setPredicates(Arrays.asList(predicate));

        System.out.println("definition:" + JSON.toJSONString(definition));
        try {
            redisTemplate.opsForHash().put("sofa_gateway_router", definition.getId(), JSON.toJSONString(definition));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "ok";
    }
}
