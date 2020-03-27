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
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.serialization.Deserializer;

import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack.Macro;
import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class WarpScriptDeserializer<T> implements Deserializer<T> {
  
  private final Macro macro;
  private final MemoryWarpScriptStack stack;
  
  public WarpScriptDeserializer(Macro macro) {
    this.macro = macro;
    this.stack = new MemoryWarpScriptStack(AbstractWarp10Plugin.getExposedStoreClient(), AbstractWarp10Plugin.getExposedDirectoryClient(), new Properties());
    this.stack.maxLimits();
  }
  
  @Override
  public T deserialize(String topic, byte[] data) {
    Map<Object,Object> map = new HashMap<Object,Object>();
    
    map.put("topic", topic);
    map.put("data", data);
    
    stack.clear();
    
    try {
      stack.push(map);
      stack.exec(macro);
      return (T) stack.pop();
    } catch (WarpScriptException wse) {
      throw new RuntimeException("Error while deserializing.", wse);
    }
  }
}
