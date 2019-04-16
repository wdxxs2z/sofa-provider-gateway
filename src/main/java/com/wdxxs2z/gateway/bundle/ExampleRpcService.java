package com.wdxxs2z.gateway.bundle;

public interface ExampleRpcService {

    @GatewayOperationType(value = "com.wdxxs2z.gateway.bundle.callMessage", name = "testDemo", desc = "This is test interface demo.")
    String helloMessage(String data);

}
