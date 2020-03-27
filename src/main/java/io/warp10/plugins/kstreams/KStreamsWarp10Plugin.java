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

import java.util.Properties;

import io.warp10.ext.kstreams.KStreamsWarpScriptExtension;
import io.warp10.script.WarpScriptLib;
import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class KStreamsWarp10Plugin extends AbstractWarp10Plugin {
  
  /**
   * Directory where the topology definitions should reside
   */
  public static final String CONF_KSTREAMS_DIR = "kstreams.dir";
  
  /**
   * How often (in ms) should `kstreams.dir` be scanned.
   */
  public static final String CONF_KSTREAMS_PERIOD = "kstreams.period";
  
  @Override
  public void init(Properties props) {
    //
    // Check that 'kstreams.dir' is defined
    //
    
    if (!props.containsKey(CONF_KSTREAMS_DIR)) {
      throw new RuntimeException("Missing configuration for '" + CONF_KSTREAMS_DIR + "'.");
    }
    
    //
    // Load the KStreams extension
    //
    
    WarpScriptLib.register(new KStreamsWarpScriptExtension());
    
    //
    // Launch a topology manager
    //
    
    new KStreamsTopologyManager(props);
  }
}
