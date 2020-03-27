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

import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;

import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack.Macro;
import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class WarpScriptTopicNameExtractor implements TopicNameExtractor {
  
  private final MemoryWarpScriptStack stack;
  private final Macro macro;
  
  public WarpScriptTopicNameExtractor(Macro macro) {
    this.macro = macro;
    this.stack = new MemoryWarpScriptStack(AbstractWarp10Plugin.getExposedStoreClient(), AbstractWarp10Plugin.getExposedDirectoryClient(), new Properties());
    this.stack.maxLimits();
  }

  @Override
  public String extract(Object key, Object value, RecordContext recordContext) {
    
    Map<Object,Object> map = new HashMap<Object,Object>();
    
    map.put("timestamp", recordContext.timestamp());
    map.put("topic", recordContext.topic());
    map.put("offset", recordContext.offset());
    map.put("partition", (long) recordContext.partition());
    map.put("key", key);
    map.put("value", value);
    Map<String,byte[]> headers = new HashMap<String,byte[]>();
    for (Header header: recordContext.headers()) {
      headers.put(header.key(), header.value());
    }
    map.put("headers", headers);
    
    stack.clear();
    
    try {
      stack.push(map);
      stack.exec(macro);
      Object top = stack.pop();
      
      if (top instanceof String) {
        return top.toString();
      } else {
        throw new WarpScriptException("Invalid result type, expected STRING, got " + top.getClass());
      }
    } catch (WarpScriptException wse) {
      throw new RuntimeException("Error extracting topic name.", wse);
    }
  }
}
