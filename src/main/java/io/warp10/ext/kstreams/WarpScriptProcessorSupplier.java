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

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorSupplier;

import io.warp10.script.WarpScriptStack.Macro;

public class WarpScriptProcessorSupplier implements ProcessorSupplier {
  
  private final Macro process;
  private final Macro init;
  private final Macro close;
  
  public WarpScriptProcessorSupplier(Macro init, Macro process, Macro close) {
    this.process = process;
    this.init = init;
    this.close = close;
  }
  
  @Override
  public Processor get() {
    return new WarpScriptProcessor(this.init, this.process, this.close);
  }
}
