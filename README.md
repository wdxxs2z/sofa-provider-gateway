# SOFA provider gateway demo

只是简单实现一个 SOFA RPC 服务网关，比较粗糙，作为一个思路扩展

## SOFA rpc interface 接口定义

```
cloud.provider.facade.CallerService

public Map<String, Object> datasource(String name) {
    Map<String, Object> resultMap = new HashMap<String, Object>();
    resultMap.put("success", true);
    resultMap.put("id", counter.incrementAndGet());
    resultMap.put("content", String.format(TEMPLATE, name));
    return resultMap;
}
```

## SOFA adapt filter 过滤器使用方式

ID：即路由标识，自定义和业务区分即可

SofaAdapt: 泛化调用过滤器

可以不用指定入参参数类型

```
- id: com.biz.service.CallerService
  uri: http://127.0.0.1:8085
  predicates:
    - Path=/api/bizCaller
  filters:
  # 第一个参数是开启过滤器，第二个是rpc对应的接口，第三个对应的是接口方法
  - SofaAdapt=true,cloud.provider.facade.CallerService,datasource
```

也可以自己指定入参，格式是{"parameterName_1":"objectType_1","parameterName_2":"objectType_2"}

```
- id: com.biz.service.CallerService
  uri: http://127.0.0.1:8085
  predicates:
    - Path=/api/bizCaller
  filters:
  # 第一个参数是开启过滤器，第二个是rpc对应的接口，第三个对应的是接口方法,第三个是入参参数类型
  - SofaAdapt=true,cloud.provider.facade.CallerService,datasource,{"name":"java.lang.String"}
```

## redis dynamic route 动态路由

RouteDefinitionRepository 的redis实现, 并添加restful接口

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
* */
@RequestMapping(value = "/route/add", method = RequestMethod.POST)
public ResponseResult add(@RequestBody GatewayRouteDefinition gatewayRouteDefinition) {
    return routeService.add(gatewayRouteDefinition);
}
```

## SOFA adapt request 网关发起请求

过滤器没有入参参数定义的，需要自己写清楚入参参数类型

post请求   xxxx/api/bizCaller

```
{
  "params": [
    {
      "java.lang.String": "heolofasdfdfadsf"
    }
  ]
}
```

过滤器有入参参数定义的，请求时，只需要知道参数名是什么就可以了，会到路由的参数列表里找

```
{
  "params": [
    {
      "name": "heol"
    }
  ]
}
```

## bundle design 思路是作为客户端，自定义一个方法注解，扫描后自动注册到服务网关，类似于坐标

通过反射可以拿到相关bean的接口，方法，参数等信息

```
    @GatewayOperationType(value = "com.wdxxs2z.gateway.bundle.callMessage", name = "testDemo", desc = "This is test interface demo.")
    String helloMessage(String data);
```