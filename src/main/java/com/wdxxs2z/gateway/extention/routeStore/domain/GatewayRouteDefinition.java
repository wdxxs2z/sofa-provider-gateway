package com.wdxxs2z.gateway.extention.routeStore.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.util.MultiValueMap;


@Data
@AllArgsConstructor
public class GatewayRouteDefinition extends RouteDefinition{

    private String serviceName;

    private String uniqueId;

    private String method;

    private String hostUrl;

    @Builder.Default
    private MultiValueMap<String, String> mappingField;

}
