package com.wdxxs2z.gateway.extention.routeStore.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GatewayRouteDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 路由ID
     */
    private String id;

    /**
     * 目标url
     */
    private String hostUrl;

    /**
     * 执行顺序
     */
    @Builder.Default
    private int order = 0;

    /**
     * 断言列表配置
     */
    @Builder.Default
    private List<PredicateDefinition> predicates = new ArrayList<>();

    /**
     * 过滤器列表配置
     */
    @Builder.Default
    private List<FilterDefinition> filters = new ArrayList<>();

}
