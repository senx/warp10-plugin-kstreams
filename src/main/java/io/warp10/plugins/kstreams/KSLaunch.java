package io.warp10.plugins.kstreams;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.locks.LockSupport;

import io.warp10.WarpConfig;
import io.warp10.ext.kstreams.KSSTART;
import io.warp10.ext.kstreams.KStreamsWarpScriptExtension;
import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.WarpScriptLib;
import io.warp10.script.WarpScriptStack.Macro;
import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class KSLaunch {
  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    
    if (null != System.getenv(WarpConfig.WARP10_CONFIG_ENV)) {
      WarpConfig.setProperties(System.getenv(WarpConfig.WARP10_CONFIG_ENV));
    } else if (null != System.getProperty(WarpConfig.WARP10_CONFIG)) {
      WarpConfig.setProperties(System.getProperty(WarpConfig.WARP10_CONFIG));
    } else {
      throw new RuntimeException("Unspecified configuration, use system property '" + WarpConfig.WARP10_CONFIG + "' or environment variable '" + WarpConfig.WARP10_CONFIG_ENV + "' to specify config file.");
    }
    
    WarpScriptLib.registerExtensions();
    WarpScriptLib.register(new KStreamsWarpScriptExtension());
    
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(AbstractWarp10Plugin.getExposedStoreClient(), AbstractWarp10Plugin.getExposedDirectoryClient(), new Properties());
    stack.maxLimits();
    
    // Add KSSTART
    Macro macro = new Macro();
    macro.add(new KSSTART("KSSTART"));
    stack.define("KSSTART", macro);
    
    String mc2 = new String(Files.readAllBytes(new File(args[0]).toPath()), StandardCharsets.UTF_8);
    
    stack.execMulti(mc2);
    
    while(true) {
      LockSupport.parkNanos(Long.MAX_VALUE);
    }
  }
}
