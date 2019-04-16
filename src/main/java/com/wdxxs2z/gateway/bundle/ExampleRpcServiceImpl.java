package com.wdxxs2z.gateway.bundle;

import org.springframework.stereotype.Service;

@Service
public class ExampleRpcServiceImpl implements ExampleRpcService{

    @Override
    public String helloMessage(String data) {
        return "hello: " + data;
    }
}
