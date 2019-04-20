package com.wdxxs2z.gateway.extention.routeStore.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SofaRpcReferenceDefinition {

    private String id;

    @Builder.Default
    private String uniqueId = "default.uniqueId";

    private String interfaceId;

    private Integer timeout;

    private String invokeType;

    private String loadBalancer;

    private Boolean sticky;

    private Integer retries;

    @Builder.Default
    private String protocol = "bolt";

    public String buildKey(){return protocol + "://" + interfaceId + ":" + uniqueId;}
}
