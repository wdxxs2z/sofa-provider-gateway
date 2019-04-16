package com.wdxxs2z.gateway.extention.routeStore.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ResponseResult {

    @Builder.Default
    private String      message = "设置完成";

    @JsonInclude
    private Object      content;

}
