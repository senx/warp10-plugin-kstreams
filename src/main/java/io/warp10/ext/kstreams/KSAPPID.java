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

import org.apache.kafka.streams.processor.ProcessorContext;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

public class KSAPPID extends NamedWarpScriptFunction implements WarpScriptStackFunction {

  public KSAPPID(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    if (null == stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT) || !(stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT) instanceof ProcessorContext)) {
      throw new WarpScriptException(getName() + " cannot be called in this context.");
    }
    
    ProcessorContext context = (ProcessorContext) stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT);
    
    stack.push(context.applicationId());
        
    return stack;
  }
}
