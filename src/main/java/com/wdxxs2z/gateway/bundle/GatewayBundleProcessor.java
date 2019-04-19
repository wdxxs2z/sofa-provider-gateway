package com.wdxxs2z.gateway.bundle;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Component
public class GatewayBundleProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        if (methods != null) {
            for (Method method : methods) {
                GatewayOperationType gatewayOperationType = AnnotationUtils.findAnnotation(method, GatewayOperationType.class);
                // process
                if (gatewayOperationType != null) {
                    Class<?> clazz = bean.getClass();
                    Class<?>[] interfaces = clazz.getInterfaces();

                    System.out.println("接口名称：" + interfaces[0].getName() +
                            " ,接口对象：" + interfaces[0] +
                            " ,方法名称：" + method.getName() +
                            " ,方法参数：" + method.getParameterTypes() +
                            " ,注解定义：" + gatewayOperationType.value() +
                            " ,注解名称：" + gatewayOperationType.name() +
                            " ,注解描述：" + gatewayOperationType.desc());
                }
            }
        }
        return bean;
    }


}
