package com.wdxxs2z.gateway.adapt;

import java.util.List;
import java.util.Map;

public interface ProtocolAdapt {

    Object doGenericInvoke(String interfaceClass, String methodName, List<Map<String, Object>> params);

}
