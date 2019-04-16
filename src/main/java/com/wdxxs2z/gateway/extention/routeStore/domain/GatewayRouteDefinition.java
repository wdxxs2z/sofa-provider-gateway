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

    private String              id;

    private String              hostUrl;

    private String              path;

    @Builder.Default
    private int                 order = 0;

    @Builder.Default
    private List<PredicateDefinition> predicates = new ArrayList<>();

    @Builder.Default
    private List<FilterDefinition> filters = new ArrayList<>();

}
