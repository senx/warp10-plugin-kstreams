package io.warp10.ext.kstreams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.streams.KafkaStreams;

public class TopologyRegistry {
  private static final Map<String,KafkaStreams> topologies = new HashMap<String, KafkaStreams>();
  
  public static void register(String name, KafkaStreams topology) {
    if (topologies.containsKey(name)) {
      throw new RuntimeException("Topology '" + name + "' already exists.");
    }
    
    topologies.put(name, topology);
  }
  
  public static void unregister(String name) {
    topologies.remove(name);
  }
  
  public static List<String> topologies() {
    return new ArrayList<String>(topologies.keySet());
  }
}
