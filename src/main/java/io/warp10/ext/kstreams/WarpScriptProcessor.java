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

import java.util.Properties;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack.Macro;
import io.warp10.script.WarpScriptStopException;
import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class WarpScriptProcessor implements Processor {
  
  private MemoryWarpScriptStack stack;
  private final Macro process;
  private final Macro init;
  private final Macro close;

  public WarpScriptProcessor(Macro init, Macro process, Macro close) {
    this.init = init;
    this.process = process;
    this.close = close;
  }
  
  @Override
  public void close() {
    
    if (null != this.close) {
      try {
        this.stack.exec(this.init);
      } catch (WarpScriptStopException wsse) {
        // Don't consider STOP an error
      } catch (WarpScriptException wse) {
        throw new RuntimeException("Error closing processor.", wse);
      }            
    }
    
    this.stack = null;
  }
  
  @Override
  public void init(ProcessorContext context) {
    this.stack = new MemoryWarpScriptStack(AbstractWarp10Plugin.getExposedStoreClient(), AbstractWarp10Plugin.getExposedDirectoryClient(), new Properties());
    this.stack.maxLimits();
    this.stack.setAttribute(KStreamsWarpScriptExtension.ATTR_PROCESSOR_CONTEXT, context);
    
    //
    // Call the init macro if it is defined
    //
    
    if (null != this.init) {
      try {
        this.stack.exec(this.init);
      } catch (WarpScriptStopException wsse) {
        // Don't consider STOP an error
      } catch (WarpScriptException wse) {
        throw new RuntimeException("Error initializing processor.", wse);
      }      
    }
  }
  
  @Override
  public void process(Object key, Object value) {
    stack.clear();

    try {
      stack.push(key);
      stack.push(value);
      stack.exec(process);
    } catch (WarpScriptStopException wsse) {
      // Don't consider STOP an error
    } catch (WarpScriptException wse) {
      throw new RuntimeException("Error processing record.", wse);
    }
  }
}
