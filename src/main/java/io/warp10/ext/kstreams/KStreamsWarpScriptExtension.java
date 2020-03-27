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

import io.warp10.warp.sdk.WarpScriptExtension;

public class KStreamsWarpScriptExtension extends WarpScriptExtension {

  public static final String ATTR_PROCESSOR_CONTEXT = "kstreams.processor.context";
  public static final String ATTR_PUNCTUATORS = "kstreams.punctuators";
  
  private static final Map<String,Object> functions;
  
  static {
    functions = new HashMap<String,Object>();
    
    functions.put("KSTOPOLOGY", new KSTOPOLOGY("KSTOPOLOGY"));
    functions.put("KSSOURCE", new KSSOURCE("KSSOURCE"));
    functions.put("KSSINK", new KSSINK("KSSINK"));
    functions.put("KSPROCESSOR", new KSPROCESSOR("KSPROCESSOR"));
    // KSSTART MUST NOT be declared here
    
    functions.put("KSAPPID", new KSAPPID("KSAPPID"));
    functions.put("KSCOMMIT", new KSCOMMIT("KSCOMMIT"));
    functions.put("KSTOPIC", new KSTOPIC("KSTOPIC"));
    functions.put("KSPARTITION", new KSPARTITION("KSPARTITION"));
    functions.put("KSOFFSET", new KSOFFSET("KSOFFSET"));
    functions.put("KSTIMESTAMP", new KSTIMESTAMP("KSTIMESTAMP"));
    functions.put("KSHEADERS", new KSHEADERS("KSHEADERS"));
    functions.put("KSFORWARD", new KSFORWARD("KSFORWARD"));
    functions.put("KSCONFIG", new KSCONFIG("KSCONFIG"));
    functions.put("KSSCHEDULE", new KSSCHEDULE("KSSCHEDULE"));
  }
  
  @Override
  public Map<String, Object> getFunctions() {
    return functions;
  }
}
