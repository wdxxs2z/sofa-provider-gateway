package com.wdxxs2z.gateway.adapt.sofa;

import com.alipay.sofa.rpc.api.GenericService;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SofaServiceCache {

    private static final Logger logger = LoggerFactory.getLogger(SofaServiceCache.class);

    private static Cache<String, GenericService> sofaServiceCache =
            CacheBuilder.newBuilder()
            .concurrencyLevel(8)
            .expireAfterWrite(1,TimeUnit.DAYS)
            .initialCapacity(50)
            .maximumSize(300)
            .recordStats()
            .removalListener((notification) -> {

            }).build();

    // 获取服务, 传递的参数可以封装成bean
    public static GenericService getService(String key, String interfaceClass,
                                            ApplicationConfig applicationConfig,
                                            RegistryConfig registryConfig
    ) {
        GenericService genericService = null;
        try{
            genericService = sofaServiceCache.get(key, () -> {
                    logger.info("服务: {} 没有在缓存中发现，开始创建", interfaceClass);
                    ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>()
                            .setInterfaceId(interfaceClass)
                            .setApplication(applicationConfig)
                            .setGeneric(true)
                            .setTimeout(10000)
                            .setRegistry(registryConfig);
                    GenericService s = consumerConfig.refer();
                    return s;
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return genericService;
    }
}
