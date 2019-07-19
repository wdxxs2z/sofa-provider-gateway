# SOFA provider gateway demo

只是简单实现一个 SOFA RPC 服务网关，比较粗糙，作为一个思路扩展，学习使用。

后段路由定义存储使用redis，即需要在resources的application.yaml里修改

## 1. 配置注册中心,默认走本地注册中心

```
com:
  alipay:
    env: shared
    instanceid: xxxx
  antcloud:
    antvip:
      endpoint: xxx
    mw:
      access: xxx
      secret: xxx
sofa:
  registry:
    address: local:///Users/xxxx/localFileRegistry/localRegistry.reg
run:
  mode: DEV
```

## 2. SOFA rpc interface 接口定义、并实现方法，采用rpc方式暴露

```
cloud.provider.facade.CallerService

Map<String, Object> datasource(String name);
```

## 3. SOFA adapt filter 定义RPC过滤器

直接使用yaml的方式定义

1). 可以不用指定入参参数类型

```
- id: com.biz.service.CallerService
  uri: http://127.0.0.1:8085
  predicates:
    - Path=/api/bizCaller
  filters:
  # 第一个参数是开启过滤器，第二个是rpc对应的接口，第三个对应的是接口方法
  - SofaAdapt=true,cloud.provider.facade.CallerService,datasource
```

2). 也可以自己指定入参，格式是{"parameterName_1":"objectType_1","parameterName_2":"objectType_2"}

```
- id: com.biz.service.CallerService
  uri: http://127.0.0.1:8085
  predicates:
    - Path=/api/bizCaller
  filters:
  # 第一个参数是开启过滤器，第二个是rpc对应的接口，第三个对应的是接口方法,第三个是入参参数类型
  - SofaAdapt=true,cloud.provider.facade.CallerService,datasource,{"name":"java.lang.String"}
```

id：即路由标识：自定义和业务区分即可

SofaAdapt: 泛化调用过滤器：内置一个缓存，放置GenericService引用对象，提高访问效率。

## 4. 通过http方式创建动态路由

请求方式：POST

接口方法：/routes

接口内容：

```
/**
{
"id": "com.wdxxs2z.gateway.bundle.callMessage",
"hostUrl": "127.0.0.1:9987",
"predicates": [
    {"name": "Path","args": {"pattern": "/api/bizMessage"}}
],
"filters": [
    {"name": "SofaAdapt","args": {
        "_genkey_0": "true","_genkey_1": "cloud.provider.facade.CallerService","_genkey_3": "message"
    }
    }
]
}
```

## 5. 网关发起业务请求

方式一. 过滤器没有入参参数定义的，需要自己写清楚入参参数类型

请求路径: http://127.0.0.1:8080/api/bizCaller

请求方式: POST

请求内容:

```
{
  "params": [
    {
      "java.lang.String": "heolofasdfdfadsf"
    }
  ]
}
```

方式二. 过滤器有入参参数定义的，请求时，只需要知道参数名是什么就可以了，会到路由的参数列表里找

```
{
  "params": [
    {
      "name": "heol"
    }
  ]
}
```

## 6. bundle design

思路: 作为客户端，自定义一个方法注解，扫描后自动注册到服务网关，类似于坐标

通过反射可以拿到相关bean的接口，方法，参数等信息

```
    @GatewayOperationType(value = "com.wdxxs2z.gateway.bundle.callMessage", name = "testDemo", desc = "This is test interface demo.")
    String helloMessage(String data);
```