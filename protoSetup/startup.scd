// this is the main entrance to a PlonkSetup.
// To start a new project, copy the whole directory and fill it with your content

// this file is meant to be executed in one go,
// so either load it from your startup file or select all and execute.

q = PlonkSetup(
    path: thisProcess.nowExecutingPath.dirname,
    projectName: \test
);

// feel free to add/remove verbosity
// q.activateVerbose(\config);


// you have to explicitely call startup to run your setup
// this will load the config file and all associated functionality as well as populating the config file
q.startup;


/*
here you have space to add test code, e.g.

you can add your own messages by calling

q.log(\name, "messagString"); // only displayed if q.activateVerbose(\name)
q.log(\name, "messagString", true); // force display


or peek into the loaded config


q.config; // looks empty
q.config.proto; // but has a proto dict with default values
// you can get information out of it just as you'd with the normal event:
q.config.serverLatency; // 0.2
q.server.latency; // should be the same as q.config.serverLatency

// variables are populated, e.g.
q.systemName

// theres a dict for prototyping
q.dict; // put your stuff here

// the directory in which assets (e.g. sounds) are expected
q.assetsDir;
// set it
q.moon

*/