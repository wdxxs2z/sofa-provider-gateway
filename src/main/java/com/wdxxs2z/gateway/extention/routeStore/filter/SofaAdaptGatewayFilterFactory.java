package com.wdxxs2z.gateway.extention.routeStore.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wdxxs2z.gateway.adapt.sofa.SofaProtocolAdapt;
import com.wdxxs2z.gateway.extention.routeStore.domain.ResponseResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class SofaAdaptGatewayFilterFactory extends AbstractGatewayFilterFactory<SofaAdaptGatewayFilterFactory.Config> {

    private static final Log logger = LogFactory.getLog(SofaAdaptGatewayFilterFactory.class);

    @Autowired
    SofaProtocolAdapt sofaProtocolAdapt;

    public SofaAdaptGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("enabled", "interfaceName", "method");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            if (!config.isEnabled()) {
                return chain.filter(exchange);
            }

            logger.info(config.getInterfaceName());

            // 获取请求
            ServerHttpRequest request = exchange.getRequest();

            // 判断该请求是否为post，只支持post
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

            final String interfaceName = config.getInterfaceName();
            final String method = config.getMethod();

            // 获取请求参数并转换
            /**
             * 获取Post请求的json数据
             * {
             *  "params": [
             *      {"java.lang.String": "hello"},{"java.lang.Integer": 12355093322}
             *  ]
             * }
             * */
            ServerRequest serverRequest = new DefaultServerRequest(exchange);
            return serverRequest.bodyToMono(String.class).flatMap(body -> {
                Map<String, Object> requestObject = JSON.parseObject(body, new TypeReference<Map<String, Object>>() {
                });
                List<Map<String, Object>> args = (List<Map<String, Object>>)requestObject.get("params");
                Object genericInvoke = sofaProtocolAdapt.doGenericInvoke(interfaceName, method, args);
                String jsonString = JSON.toJSONString(genericInvoke);
                // 将返回值填入response
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.OK);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return response.writeWith(Flux.just(response.bufferFactory().wrap(jsonString.getBytes())));
            });
        };
    }

    // 配置
    public static class Config {

        private boolean enabled;

        private String interfaceName;

        private String method;

        public Config() {}

        public String getInterfaceName() {
            return interfaceName;
        }

        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public boolean isEnabled() {
            return enabled;
        }
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
