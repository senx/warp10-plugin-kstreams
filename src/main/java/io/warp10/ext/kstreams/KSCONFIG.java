//
//   Copyright 2020  SenX S.A.S.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package io.warp10.ext.kstreams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.kafka.common.config.types.Password;
import org.apache.kafka.streams.processor.ProcessorContext;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

public class KSCONFIG extends NamedWarpScriptFunction implements WarpScriptStackFunction {

  public KSCONFIG(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    if (null == stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT) || !(stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT) instanceof ProcessorContext)) {
      throw new WarpScriptException(getName() + " cannot be called in this context.");
    }
    
    ProcessorContext context = (ProcessorContext) stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT);

    Map<Object,Object> config = new HashMap<Object,Object>();

    // Convert config to STRING, ignoring passwords
    for (Entry<String,Object> entry: context.appConfigs().entrySet()) {
      Object value = convertToString(entry.getValue());
      if (null == value) {
        continue;
      }
      config.put(entry.getKey(), value);
    }
        
    stack.push(config);
        
    return stack;
  }
  
  private static String convertToString(Object value) {
    if (null == value) {
      return null;
    } else if (value instanceof Password) {
      return null;
    } else if (value instanceof List) {
      StringBuilder sb = new StringBuilder();
      for (Object v: (List) value) {
        String str = convertToString(v);
        if (null == str) {
          continue;
        }
        if (0 != sb.length()) {
          sb.append(",");
        }
        sb.append(str);
      }
      return sb.toString();
    } else if (value instanceof Class) {
      return ((Class) value).getName();
    } else {
      return value.toString();
    }
  }
}
