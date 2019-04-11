package com.wdxxs2z.gateway.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wdxxs2z.gateway.adapt.sofa.SofaProtocolAdapt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private SofaProtocolAdapt sofaProtocolAdapt;

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
}
