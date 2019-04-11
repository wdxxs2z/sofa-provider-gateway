package com.wdxxs2z.gateway.adapt.sofa;

import com.alipay.hessian.generic.model.GenericObject;
import com.alipay.sofa.rpc.api.GenericService;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.wdxxs2z.gateway.adapt.ProtocolAdapt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SofaProtocolAdapt implements ProtocolAdapt {

    private final static Logger LOGGER = LoggerFactory.getLogger(SofaProtocolAdapt.class);

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private RegistryConfig registryConfig;

    private static Map<String, GenericService> cache = new HashMap<>();

    /**
     * [{"java.lang.String":"hello"},{"com.alipay.demo.Person":{"age":10,"username":"tony"}}]
     * */
    @Override
    public Object doGenericInvoke(String interfaceClass, String methodName, List<Map<String, Object>> params) {

        if (applicationConfig == null || registryConfig == null) {
            return "配置为空";
        }

        GenericService genericService;

        // 服务接口引用
        if (cache.get(interfaceClass) != null) {
            genericService = cache.get(interfaceClass);
        }else {
            ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>()
                    .setInterfaceId(interfaceClass)
                    .setApplication(applicationConfig)
                    .setGeneric(true)
                    .setTimeout(10000)
                    .setRegistry(registryConfig);

            genericService = consumerConfig.refer();
            cache.put(interfaceClass, genericService);
        }

        List<String> types = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        // 参数转换
        for (Map<String, Object> param : params) {
            for (Map.Entry<String, Object> paramMap : param.entrySet()) {
                GenericObject genericObject = new GenericObject(paramMap.getKey());
                if (paramMap.getValue() instanceof Map){
                    Map<String, Object> attrubits = (Map)paramMap.getValue();
                    for (Map.Entry<String, Object> attrubit : attrubits.entrySet()) {
                        genericObject.putField(attrubit.getKey(), attrubit.getValue());
                    }
                    types.add(paramMap.getKey());
                    args.add(genericObject);
                }else {
                    types.add(paramMap.getKey());
                    args.add(paramMap.getValue());
                }
            }
        }

        // 泛化执行
        Object genericObjectInvoke = null;
        String[] genericTypes =new String[types.size()];
        try {
            genericObjectInvoke = genericService.$genericInvoke(methodName,
                    types.toArray(genericTypes),
                    args.toArray());
        }catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        return genericObjectInvoke;
    }
}
