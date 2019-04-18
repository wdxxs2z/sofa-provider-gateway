package com.wdxxs2z.gateway.extention.routeStore.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wdxxs2z.gateway.adapt.sofa.SofaProtocolAdapt;
import com.wdxxs2z.gateway.extention.routeStore.domain.ResponseResult;
import io.netty.buffer.ByteBufAllocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SofaFilter implements GatewayFilter, Ordered {

    @Autowired
    SofaProtocolAdapt sofaProtocolAdapt;

    // 热点路由，缓存器，不用我们自己实现缓存设计了
    @Autowired
    RouteLocator routeLocator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        if (request.getMethodValue().equals("GET")) {
            ServerHttpResponse response = exchange.getResponse();
            ResponseResult respResult = new ResponseResult();
            respResult.setCode(HttpStatus.BAD_REQUEST.value());
            respResult.setMessage("Get Method not adapt, please use Post");
            byte[] datas = JSON.toJSONString(respResult).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(datas);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            return response.writeWith(Mono.just(buffer));
        }

        // 获取请求参数, interfaceId, 路由的ID，接口注入的唯一标识
        String interfaceId = request.getQueryParams().getFirst("interfaceId");

        // 获取路由信息是否匹配
        Route cacheRoute = routeLocator.getRoutes().toStream().filter(r -> r.getId().equals(interfaceId)).findFirst().get();
        if (cacheRoute == null) {
            ServerHttpResponse response = exchange.getResponse();
            ResponseResult respResult = new ResponseResult();
            respResult.setCode(HttpStatus.NOT_FOUND.value());
            respResult.setMessage("Rpc Service Route Not Found");
            byte[] datas = JSON.toJSONString(respResult).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(datas);
            response.setStatusCode(HttpStatus.NOT_FOUND);
            response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            return response.writeWith(Mono.just(buffer));
        }

        // 获取请求参数并转换
        /**
         * 获取Post请求的json数据
         * {
         *  "interfaceName": "com.demo.provider.CallService",
         *  "method": "message",
         *  "params": [
         *      {"java.lang.String": "hello"},{"java.lang.Integer": 12355093322}
         *  ]
         * }
         * */
        String bodyContent = resolveBodyFromRequest(request);
        Map<String, Object> requestObject = JSON.parseObject(bodyContent, new TypeReference<Map<String, Object>>() {
        });
        String interfaceName = (String)requestObject.get("interfaceName");
        String method = (String)requestObject.get("method");
        List<Map<String, Object>> args = (List<Map<String, Object>>)requestObject.get("params");

        return Mono.defer(() -> {
            // 执行泛化调用
            Object genericInvoke = sofaProtocolAdapt.doGenericInvoke(interfaceName, method, args);
            String jsonString = JSON.toJSONString(genericInvoke);

            // 将返回值填入response
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Flux.just(response.bufferFactory().wrap(jsonString.getBytes())));
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * 从Flux<DataBuffer>中获取字符串的方法
     * @return 请求体
     */
    private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        return bodyRef.get();
    }

    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }
}
