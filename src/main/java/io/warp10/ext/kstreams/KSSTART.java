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

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

public class KSSTART extends NamedWarpScriptFunction implements WarpScriptStackFunction {

  public KSSTART(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    Object top = stack.pop();
    
    if (!(top instanceof Map)) {
      throw new WarpScriptException(getName() + " expects a parameter map.");
    }
    
    Map<Object,Object> params = (Map<Object,Object>) top;
    
    Properties props = new Properties();
    
    props.putAll(params);
    
    top = stack.pop();
    if (!(top instanceof Topology)) {
      throw new WarpScriptException(getName() + " operates on a Kafka Streams topology.");
    }
    
    Topology topology = (Topology) top;
    
    KafkaStreams ks = new KafkaStreams(topology, props);
    
    try {
      TopologyRegistry.register(props.getProperty(StreamsConfig.APPLICATION_ID_CONFIG), ks);
      ks.start();
    } catch (Throwable t) {
      ks.close();
    }
        
    return stack;
  }
}
