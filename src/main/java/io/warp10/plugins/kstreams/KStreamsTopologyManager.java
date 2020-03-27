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

package io.warp10.plugins.kstreams;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.warp10.crypto.SipHashInline;
import io.warp10.ext.kstreams.KSSTART;
import io.warp10.ext.kstreams.TopologyRegistry;
import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.WarpScriptStack.Macro;
import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class KStreamsTopologyManager extends Thread {
  
  private static final String DEFAULT_SCAN_PERIOD = "60000";
  
  private static final Logger LOG = LoggerFactory.getLogger(KStreamsTopologyManager.class);
  
  private final File dir;
  private final long delay;
  
  public KStreamsTopologyManager(Properties props) {
    dir = new File(props.getProperty(KStreamsWarp10Plugin.CONF_KSTREAMS_DIR));
    
    if (!dir.exists() || !dir.isDirectory()) {
      throw new RuntimeException("Configuration '" + KStreamsWarp10Plugin.CONF_KSTREAMS_DIR + "' must define an existing directory.");
    }

    this.delay = 1000000L * Long.parseLong(props.getProperty(KStreamsWarp10Plugin.CONF_KSTREAMS_PERIOD, DEFAULT_SCAN_PERIOD));
    
    this.setDaemon(true);
    this.setName("[KStreamsTopologyManager " + dir.toString() + "]");
    this.start();
  }
  
  @Override
  public void run() {
    
    //
    // Map of file to topologies
    //
    
    Map<String,List<String>> topologiesByFile = new HashMap<String, List<String>>();
    
    //
    // Map of file to content hash
    //
    
    Map<String,Long> hashes = new HashMap<String,Long>();
    
    long k0 = System.nanoTime();
    long k1 = System.currentTimeMillis();
    
    while(true) {
      //
      // Scan 'dir'
      //
      
      for (File file: dir.listFiles()) {        
        try {
          String path = file.getCanonicalPath();

          //
          // Load the content and compute its hash
          //
          byte[] content = Files.readAllBytes(file.toPath());
          
          Long hash = SipHashInline.hash24(k0, k1, content, 0, content.length);
          
          // If the hash has not changed, do nothing
          if (hash.equals(hashes.get(path))) {
            continue;
          }
          
          // Kill existing topologies for this file
          if (topologiesByFile.containsKey(path)) {
            for (String topology: topologiesByFile.get(path)) {
              TopologyRegistry.unregister(topology);
            }
            topologiesByFile.remove(path);
          }
          
          // Execute the file content
          String mc2 = new String(content, StandardCharsets.UTF_8);
          MemoryWarpScriptStack stack = new MemoryWarpScriptStack(AbstractWarp10Plugin.getExposedStoreClient(), AbstractWarp10Plugin.getExposedDirectoryClient(), new Properties());
          stack.maxLimits();
          
          // Add KSSTART
          Macro macro = new Macro();
          macro.add(new KSSTART("KSSTART"));
          stack.define("KSSTART", macro);

          // Record the known topologies before the execution
          List<String> topologies = TopologyRegistry.topologies();
          
          try {
            stack.execMulti(mc2);
          } catch (Throwable t) {
            // Kill the topologies which were added before the exception
            List<String> newtopologies = TopologyRegistry.topologies();
            newtopologies.removeAll(topologies);
            
            for (String topology: newtopologies) {
              LOG.error("Error while executing file '" + file + "', killing topology '" + topology + "'.");
              TopologyRegistry.unregister(topology);
            }            
            continue;
          }

          // Store the newly created topologies
          List<String> newtopologies = TopologyRegistry.topologies();
          newtopologies.removeAll(topologies);
          topologiesByFile.put(path, newtopologies);
          
          // Store the new hash
          hashes.put(path, hash);          
        } catch (IOException ioe) {
          LOG.error("Error while reading file '" + file + "', no change to topologies.");
        }
      }
      
      LockSupport.parkNanos(delay);
    }
  }
}
