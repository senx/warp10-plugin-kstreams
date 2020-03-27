# Warp 10 Kafka Streams Plugin

This plugin allows to run [Kafka Streams](https://kafka.apache.org/documentation/streams/) topologies defined entirely in [WarpScript](https://www.warp10.io/content/03_Documentation/04_WarpScript/01_Concepts). The goal is to leverage the power and versatility of the [Warp 10](https://warp10.io/) ecosystem within the Kafka Streams paradigm.

The primary goal of the plugin is to define Kafka Streams topologies within a Warp 10 instance but it includes a launcher which can run topologies within a dedicated Java Virtual Machine.

# Topology definition

A topology is a WarpScript program making use of some specific functions. Those functions allow to define the sources, sinks and processors of the topology in a way very similar to the Kafka Streams [Processor API](https://kafka.apache.org/10/documentation/streams/developer-guide/processor-api.html).

A single WarpScript program can contain the definition of several topologies and launch them in the same JVM.

The sequence of functions to use for defining and running a topology is:

```
KSTOPOLOGY          // Create a topology
{ ... } KSSOURCE    // Define a source and add it to the topology
{ ... } KSPROCESSOR // Define a processor and add it to the topology
{ ... } KSSINK      // Define a sink and add it to the topology

// The above functions can be used multiple times to add multiple instances of each node type

{
  ... // Kafka Streams configuration
}
KSSTART 
```
This is all there is to it. Please refer to the documentation of each function to learn about their parameters.

# Deploying topologies within a Warp 10 instance

The plugin needs to be configured for your Warp 10 instance:

```
warp10.plugin.kstreams = io.warp10.plugins.kstreams.KStreamsWarp10Plugin
kstreams.dir = /path/to/your/kstreams/topologies/directory
```

When the Kafka Streams Plugin is loaded into Warp 10, the directory specified under `kstreams.dir` is scanned periodically (as specified in `kstreams.period` or every `60000` milliseconds) and any new or modified `.mc2` file will be executed. Those files are expected to contain topology definitions.

When a file is updated, the topologies it previously launched are first stopped and the file content is then executed, launching new topologies if `KSSTART` is called.

# Launching topologies in a separate JVM

Launching topologies in a dedicated JVM is done via the use of the `io.warp10.plugins.kstreams.KSLaunch` class. Assuming the definition of the topologies is in file `topologies.mc2`, the following command would launch them:

```
java -cp warp10-plugins-kstreams-x.y.z-uberuberjar.jar -Dwarp10.conf=warp10.conf io.warp10.plugins.kstreams.KSLaunch topologies.mc2
```

Note that you have to use the `-uberuberjar` version of the plugin which embeds the WarpScript library. The `-uberjar` version only embeds the Kafka Streams dependencies, it is meant to be used within a Warp 10 instance.

The `warp10.conf` file contains the configuration of the WarpScript environment, such as the time units, the extensions to load, the WarpFleet URLs and any other configuration you may need.

# Additional functions

The following functions are provided by the `KStreamsWarpScriptExtension` which comes with the Kafka Streams Plugin.

## Topology definition functions

| Function | Description |
|----------|-------------|
| `KSTOPOLOGY` | Create an empty topology. |
| `KSSOURCE` | Add a source to a topology. |
| `KSPROCESSOR` | Add a processor to a topology. |
| `KSSINK` | Add a sink to a topology. |
| `KSSTART` | Start a topology. |


## Processor functions

| Function | Description |
|----------|-------------|
| `KSAPPID` | Retrieve the application ID. |
| `KSCOMMIT` | Request a commit of offsets. |
| `KSCONFIG` | Retrieve the topology configuration. |
| `KSHEADERS` | Retrieve the headers of the current record. |
| `KSOFFSET` | Retrieve the offset of the current record. |
| `KSPARTITION` | Retrieve the partition of the current record. |
| `KSTIMESTAMP` | Retrieve the timestamp of the current record. |
| `KSTOPIC` | Retrieve the topic of the current record. |
| `KSTIMESTAMP` | Retrieve the timestamp of the current record. Timestamp is in milliseconds since the Unix Epoch. |
| `KSSCHEDULE` | Schedule or cancel a punctuation. |
| `KSFORWARD` | Forward a record to a downstream processor. |

