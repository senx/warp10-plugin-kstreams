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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStack.Macro;
import io.warp10.script.WarpScriptStackFunction;

public class KSSCHEDULE extends NamedWarpScriptFunction implements WarpScriptStackFunction {

  public KSSCHEDULE(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    if (null == stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT) || !(stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT) instanceof ProcessorContext)) {
      throw new WarpScriptException(getName() + " cannot be called in this context.");
    }
    
    ProcessorContext context = (ProcessorContext) stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT);
    
    Object top = stack.pop();
    
    if (!(top instanceof Map)) {
      throw new WarpScriptException(getName() + " expects a parameter map.");
    }
    
    Map<Object,Object> params = (Map<Object,Object>) top;
    
    if (!params.containsKey(KStreamsUtils.KEY_NAME)) {
      throw new WarpScriptException(getName() + " punctuations must have a '" + KStreamsUtils.KEY_NAME + "' set.");
    }
        
    String name = String.valueOf(params.get(KStreamsUtils.KEY_NAME));
    
    if (!params.containsKey(KStreamsUtils.KEY_MACRO) || !(null == params.get(KStreamsUtils.KEY_MACRO) || params.get(KStreamsUtils.KEY_MACRO) instanceof Macro)) {
      throw new WarpScriptException(getName() + " expects a macro or NULL under key '" + KStreamsUtils.KEY_MACRO + "'.");
    }
    
    Macro macro = (Macro) params.get(KStreamsUtils.KEY_MACRO);
    
    //
    // Check if there is already a punctuator registered under this name. If so
    // then if macro is null cancel it, otherwise emit an error
    //
    
    Map<String,Cancellable> punctuators = (Map<String,Cancellable>) stack.getAttribute(KStreamsWarpScriptExtension.ATTR_PUNCTUATORS);
    
    if (null == punctuators) {
      punctuators = new HashMap<String,Cancellable>();
      stack.setAttribute(KStreamsWarpScriptExtension.ATTR_PUNCTUATORS, punctuators);
    }
    
    if (punctuators.containsKey(name) && null != macro) {
      throw new WarpScriptException(getName() + " punctuator '" + name + "' already exists.");
    }
    
    if (null == macro) {
      Cancellable punct = punctuators.remove(name);
      // Cancel the currently scheduled punctuator
      if (null != punct) {
        punct.cancel();
      }
    } else {
      PunctuationType type = PunctuationType.WALL_CLOCK_TIME;
      
      if (Boolean.TRUE.equals(params.get(KStreamsUtils.KEY_STREAMTIME))) {
        type = PunctuationType.STREAM_TIME;
      }
      
      if (!params.containsKey(KStreamsUtils.KEY_INTERVAL) || !(params.get(KStreamsUtils.KEY_INTERVAL) instanceof Long)) {
        throw new WarpScriptException(getName() + " expects a LONG interval under key '" + KStreamsUtils.KEY_INTERVAL + "'.");
      }
      
      Duration interval = Duration.of(((Long) params.get(KStreamsUtils.KEY_INTERVAL)).longValue(), ChronoUnit.MILLIS);

      Punctuator callback = new WarpScriptPunctuator(stack, macro, context);    

      Cancellable punct = context.schedule(interval, type, callback);
      punctuators.put(name, punct);
    }
        
    return stack;
  }
}
