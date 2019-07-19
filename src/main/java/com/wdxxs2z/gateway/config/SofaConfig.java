package com.wdxxs2z.gateway.config;

import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.antcloud.antvip.client.AntVipConfigure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SofaConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${com.alipay.env:shared}")
    private String alipayEnv;

    @Value("${com.alipay.instanceid}")
    private String alipayInstanceId;

    @Value("${com.antcloud.antvip.endpoint}")
    private String antcloudEndpoint;

    @Value("${com.antcloud.mw.access}")
    private String antcloudAccess;

    @Value("${com.antcloud.mw.secret}")
    private String antcloudSecret;

    @Value("${run.mode}")
    private String runMode;

    @Value("${sofa.registry.address}")
    private String registryAddress;

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

        int startIndex = registryAddress.indexOf("://");
        String protocol = registryAddress.substring(0, startIndex);
        String address = registryAddress.substring(startIndex + 3);

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setProtocol(protocol);
        if (protocol.equals("local")) {
            registryConfig.setFile(address);
        }else if (protocol.contains("dsr")){
        }else {
            registryConfig.setAddress(address);
        }
        registryConfig.setSubscribe(true);
        registryConfig.setConnectTimeout(5000);

        return registryConfig;
    }
}
