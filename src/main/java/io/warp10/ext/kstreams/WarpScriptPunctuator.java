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
import org.apache.kafka.streams.processor.Punctuator;

import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStack.Macro;
import io.warp10.script.WarpScriptStopException;

public class WarpScriptPunctuator implements Punctuator {
  
  private final WarpScriptStack stack;
  private final Macro macro;
  
  public WarpScriptPunctuator(WarpScriptStack stack, Macro macro, ProcessorContext context) {
    this.macro = macro;
    this.stack = stack;
  }
  
  @Override
  public void punctuate(long timestamp) {
    this.stack.clear();
    
    try {
      this.stack.push(timestamp);
      this.stack.exec(this.macro);
    } catch (WarpScriptStopException wsse) {
      // Allow the call of STOP
    } catch (WarpScriptException wse) {
      throw new RuntimeException("Error while executing punctuator.", wse);
    }
  }

}
