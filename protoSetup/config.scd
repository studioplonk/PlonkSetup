(
// relAssetsDir: "_assets", // set asset directory relative to project directory

// Audio Server
serverOptions: ServerOptions()
// .device_("Fireface UFX (23610321)") // use a non-default device (OSX only)
// .sampleRate_(48000)
// .blockSize_(64)
// .numBuffers_(1024)
// .numOutputBusChannels_(2)
// .numInputBusChannels_(2)
// .maxNodes_(1024 * 16)
// .memSize_(8192 * 64)
// .numWireBufs_(1024)
// .bindAddress_("0.0.0.0") // also listen to other machines on the network
,
serverLatency: 0.1, // 100 ms

//////////////////////////////////// files to execute
// order is important here
beforeBoot: [
	{"*** Hello Worlds".postln}, // executing functions here works as well
],
afterBoot: [
	// "dataSources/busAlloc.scd", // bus allocation for external data sources
	// load data sources, e.g.
	// "dataSources/sunMoon.scd",
	// ^^ this requires q.config.filenameMoon and q.config.filenameSun,
	//    see Sun and Moon Helpfiles for details.
	{"*** Hello Worlds after boot".postln}, // executing functions here works as well

	// load sounds
	// "sounds/example.scd",

	// high level control and sensor integration
],

)
