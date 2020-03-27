package io.warp10.ext.kstreams;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.streams.processor.StreamPartitioner;

import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack.Macro;
import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class WarpScriptStreamPartitioner implements StreamPartitioner {
  
  private final MemoryWarpScriptStack stack;
  private final Macro macro;
  
  public WarpScriptStreamPartitioner(Macro macro) {
    this.macro = macro;
    this.stack = new MemoryWarpScriptStack(AbstractWarp10Plugin.getExposedStoreClient(), AbstractWarp10Plugin.getExposedDirectoryClient(), new Properties());
    this.stack.maxLimits();
  }
  
  @Override
  public Integer partition(String topic, Object key, Object value, int numPartitions) {
    Map<Object,Object> map = new HashMap<Object,Object>();
    
    map.put("topic", topic);
    map.put("numpartitions", (long) numPartitions);
    map.put("key", key);
    map.put("value", value);
    
    stack.clear();
    
    try {
      stack.push(map);
      stack.exec(macro);
      Object top = stack.pop();
      
      if (top instanceof Long) {
        return ((Long) top).intValue();
      } else {
        throw new WarpScriptException("Invalid result type, expected LONG, got " + top.getClass());
      }
    } catch (WarpScriptException wse) {
      throw new RuntimeException("Error determining partition.", wse);
    }
  }
}
