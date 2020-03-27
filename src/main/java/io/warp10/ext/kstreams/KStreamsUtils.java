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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.Topology.AutoOffsetReset;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.StreamPartitioner;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.kafka.streams.processor.internals.StaticTopicNameExtractor;

import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack.Macro;

public class KStreamsUtils {
  
  public static final String KEY_NAME = "name";
  private static final String KEY_TOPICS = "topics";
  private static final String KEY_KEYDESER = "keydeser";
  private static final String KEY_VALUEDESER = "valuedeser";
  private static final String KEY_KEYSER = "keyser";
  private static final String KEY_VALUESER = "valueser";
  private static final String KEY_TSEXTRACTOR = "tsextractor";
  private static final String KEY_AUTOOFFSETRESET = "reset";
  private static final String KEY_TOPIC = "topic";
  private static final String KEY_PARTITIONER = "partitioner";
  private static final String KEY_PARENTS = "parents";
  public static final String KEY_MACRO = "macro";
  public static final String KEY_PROCESS = "process";
  public static final String KEY_INTERVAL = "interval";
  public static final String KEY_STREAMTIME = "streamtime";
  private static final String KEY_INIT = "init";
  private static final String KEY_CLOSE = "close";
  
  public static void addSource(Topology topology, Map<Object,Object> params) throws WarpScriptException {

    if (!params.containsKey(KEY_NAME)) {
      throw new WarpScriptException("Key '" + KEY_NAME + "' is mandatory.");
    }
    
    String name = String.valueOf(params.get(KEY_NAME));
    
    TimestampExtractor timestampExtractor = null;

    if (params.containsKey(KEY_TSEXTRACTOR)) {
      if (!(params.get(KEY_TSEXTRACTOR) instanceof Macro)) {
        throw new WarpScriptException("Key '" + KEY_TSEXTRACTOR + "' must be associated with a macro.");
      }
      timestampExtractor = new WarpScriptTimestampExtractor((Macro) params.get(KEY_TSEXTRACTOR));
    }

    Deserializer<Object> keyDeserializer = null;
    
    if (params.containsKey(KEY_KEYDESER)) {
      if (!(params.get(KEY_KEYDESER) instanceof Macro)) {
        throw new WarpScriptException("Key '" + KEY_KEYDESER + "' must be associated with a macro.");
      }
      keyDeserializer = new WarpScriptDeserializer((Macro) params.get(KEY_KEYDESER));
    }

    Deserializer<Object> valueDeserializer = null;
    
    if (params.containsKey(KEY_VALUEDESER)) {
      if (!(params.get(KEY_VALUEDESER) instanceof Macro)) {
        throw new WarpScriptException("Key '" + KEY_VALUEDESER + "' must be associated with a macro.");
      }
      valueDeserializer = new WarpScriptDeserializer((Macro) params.get(KEY_VALUEDESER));
    }

    if (!params.containsKey(KEY_TOPICS)) {
      throw new WarpScriptException("Missing '" + KEY_TOPICS + "' key.");
    }
    
    Pattern topicPattern = Pattern.compile(params.get(KEY_TOPICS).toString());
    
    AutoOffsetReset offsetReset = null;
    
    if (params.containsKey(KEY_AUTOOFFSETRESET)) {
      offsetReset = AutoOffsetReset.valueOf(params.get(KEY_AUTOOFFSETRESET).toString().toUpperCase());
    }
    
    topology.addSource(offsetReset, name, timestampExtractor, keyDeserializer, valueDeserializer, topicPattern);
  }
  
  public static void addSink(Topology topology, Map<Object,Object> params) throws WarpScriptException {
    if (!params.containsKey(KEY_NAME)) {
      throw new WarpScriptException("Key '" + KEY_NAME + "' is mandatory.");
    }
    
    String name = String.valueOf(params.get(KEY_NAME));
    
    TopicNameExtractor topicExtractor = null;

    if (params.containsKey(KEY_TOPIC)) {
      if (params.get(KEY_TOPIC) instanceof Macro) {
        topicExtractor = new WarpScriptTopicNameExtractor((Macro) params.get(KEY_TOPIC));        
      } else if (params.get(KEY_TOPIC) instanceof String) {
        topicExtractor = new StaticTopicNameExtractor((String) params.get(KEY_TOPIC));
      } else {
        throw new WarpScriptException("Key '" + KEY_TOPIC + "' must be associated with a macro or a STRING.");
      }
    }

    StreamPartitioner partitioner = null;

    if (params.containsKey(KEY_PARTITIONER)) {
      if (!(params.get(KEY_PARTITIONER) instanceof Macro)) {
        throw new WarpScriptException("Key '" + KEY_PARTITIONER + "' must be associated with a macro.");
      }
      partitioner = new WarpScriptStreamPartitioner((Macro) params.get(KEY_PARTITIONER));
    }

    Serializer<Object> keySerializer = null;
    
    if (params.containsKey(KEY_KEYSER)) {
      if (!(params.get(KEY_KEYSER) instanceof Macro)) {
        throw new WarpScriptException("Key '" + KEY_KEYSER + "' must be associated with a macro.");
      }
      keySerializer = new WarpScriptSerializer((Macro) params.get(KEY_KEYSER));
    }

    Serializer<Object> valueSerializer = null;
    
    if (params.containsKey(KEY_VALUESER)) {
      if (!(params.get(KEY_VALUESER) instanceof Macro)) {
        throw new WarpScriptException("Key '" + KEY_VALUESER + "' must be associated with a macro.");
      }
      valueSerializer = new WarpScriptSerializer((Macro) params.get(KEY_VALUESER));
    }

    if (!params.containsKey(KEY_PARENTS) || !(params.get(KEY_PARENTS) instanceof List)) {
      throw new WarpScriptException("Missing '" + KEY_PARENTS + "' key.");
    }

    String[] parentNames = new String[((List<Object>) params.get(KEY_PARENTS)).size()];

    int idx = 0;
    for (Object parent: (List<Object>) params.get(KEY_PARENTS)) {
      parentNames[idx++] = parent.toString();
    }
    
    topology.addSink(name, topicExtractor, keySerializer, valueSerializer, partitioner, parentNames);   
  }
  
  public static void addProcessor(Topology topology, Map<Object,Object> params) throws WarpScriptException {
    
    if (!params.containsKey(KEY_NAME)) {
      throw new WarpScriptException("Key '" + KEY_NAME + "' is mandatory.");
    }
    
    String name = String.valueOf(params.get(KEY_NAME));
    
    ProcessorSupplier supplier;
    
    if (!params.containsKey(KEY_PROCESS) || !(params.get(KEY_PROCESS) instanceof Macro)) {
      throw new WarpScriptException("Missing '" + KEY_PROCESS + "' key.");
    }

    if (params.containsKey(KEY_INIT) && !(params.get(KEY_INIT) instanceof Macro)) {
      throw new WarpScriptException("Key '" + KEY_INIT + "' must be a macro.");
    }

    if (params.containsKey(KEY_CLOSE) && !(params.get(KEY_CLOSE) instanceof Macro)) {
      throw new WarpScriptException("Key '" + KEY_CLOSE + "' must be a macro.");
    }

    supplier = new WarpScriptProcessorSupplier((Macro) params.get(KEY_INIT), (Macro) params.get(KEY_PROCESS), (Macro) params.get(KEY_CLOSE));
    
    if (!params.containsKey(KEY_PARENTS) || !(params.get(KEY_PARENTS) instanceof List)) {
      throw new WarpScriptException("Missing '" + KEY_PARENTS + "' key.");
    }

    String[] parentNames = new String[((List<Object>) params.get(KEY_PARENTS)).size()];

    int idx = 0;
    for (Object parent: (List<Object>) params.get(KEY_PARENTS)) {
      parentNames[idx++] = parent.toString();
    }
    
    topology.addProcessor(name, supplier, parentNames);
    
  }
}
