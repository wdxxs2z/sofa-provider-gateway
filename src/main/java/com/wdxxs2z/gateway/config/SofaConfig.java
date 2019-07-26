package com.wdxxs2z.gateway.config;

import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.antcloud.antvip.client.AntVipConfigure;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SofaConfig {

    @Value("${spring.application.name}")
    String appName;

    @Value("${com.alipay.env:shared}")
    String alipayEnv;

    @Value("${com.alipay.instanceid}")
    String alipayInstanceId;

    @Value("${com.antcloud.antvip.endpoint}")
    String antcloudEndpoint;

    @Value("${com.antcloud.mw.access}")
    String antcloudAccess;

    @Value("${com.antcloud.mw.secret}")
    String antcloudSecret;

    @Value("${run.mode}")
    String runMode;

    @Value("${sofa.registry.address}")
    String registryAddress;

    @Bean
    public ApplicationConfig applicationConfig() {

        if (runMode == null) {
            System.setProperty("run.mode", "DEV");
        }else if (runMode != null && runMode == "DEV") {
            System.setProperty("run.mode", runMode);
        }else {
            AntVipConfigure config = new AntVipConfigure();

            System.setProperty("run.mode", runMode);
            System.setProperty("com.alipay.env", alipayEnv);
            System.setProperty("com.alipay.instanceid", alipayInstanceId);
            System.setProperty("com.antcloud.antvip.endpoint", antcloudEndpoint);

            config.setInstanceId(alipayInstanceId);
            config.setEndPoint(antcloudEndpoint);
            config.setAppName(appName);

            if (antcloudAccess!=null) {
                System.setProperty("com.antcloud.mw.access", antcloudAccess);
                config.setAccessKey(antcloudAccess);
            }
            if (antcloudSecret !=null) {
                System.setProperty("com.antcloud.mw.secret", antcloudSecret);
                config.setAccessSecret(antcloudSecret);
            }
        }

        ApplicationConfig appConfiguration = new ApplicationConfig();
        appConfiguration.setAppName(appName);

        return appConfiguration;
    }

    @Bean
    public RegistryConfig registryConfig() {

        RegistryConfig registryConfig = new RegistryConfig();
        String cloudRunMode = System.getProperty("run.mode");

        if (cloudRunMode.equalsIgnoreCase("DEV")) {//dev环境，直接走local
            String usrHome = System.getProperty("user.home");
            registryConfig.setProtocol("local");
            registryConfig.setFile(usrHome + "/localFileRegistry/localRegistry.reg");
        } else if (cloudRunMode.equalsIgnoreCase("NORMAL")
                || cloudRunMode.equalsIgnoreCase("TEST")) {
            if (StringUtils.isNotEmpty(registryAddress)) {// 显示指定注册中心
                int startIndex = registryAddress.indexOf("://");
                String protocol = registryAddress.substring(0, startIndex);
                String address = registryAddress.substring(startIndex + 3);
                registryConfig.setAddress(address);
                registryConfig.setProtocol(protocol);
            } else {// 企业版注册中心
                registryConfig.setProtocol("dsr");
            }
        }

        registryConfig.setSubscribe(true);
        registryConfig.setConnectTimeout(5000);
        return registryConfig;
    }
}
