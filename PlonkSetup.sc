PlonkSetup {
    var <path;
    var <systemName;
    var <projectName;
    var <verboseLevels;
    var <dict;
    var <config;
    var <server; // sound server
    var sounds; // a dict of sounds, minimal wrappers around NodeProxy that also include buffers and actions
    var soundHelpers; // same as above but for helper signal chains
    var <proxyspace; // a proxyspace to hold all NodeProxies for this setup

    *new {|path, projectName|
        ^super.new.init(path, projectName);
    }
    init {|argPath, argProjectName, argServer|        
        path = argPath;

        // require the project name to be a symbol
        projectName = argProjectName.asSymbol;

        systemName = "uname -n".unixCmdGetStdOut;
        verboseLevels = Set[];

        // create a proxyspace for this setup
        proxyspace = ProxySpace.new(server, projectName);

        // dictionaries
        dict = ();
        sounds = ();
        soundHelpers = ();
    }
    *protoConfig {
        ^(
            relAssetsDir: "_assets",
            server: Server.default,
            beforeBoot: [],
            afterBoot: [],
            sampleRate: 48000,
            blockSize: 64,
            numBuffers: 1024,
            // audioDevice_Linux: "", // not needed because we're running pipewire
            audioDevice_OSX: "BlackHole 64ch",
            numOutputBusChannels: 2,
            numInputBusChannels: 2,
            maxNodes: 1024 * 16,
            memSize: 8192 * 64,
            numWireBufs: 1024,
            bindAddress: "0.0.0.0"
        );
    }


    sounds {|key|
        key.isNil.if{
            ^sounds;
        };
        ^this.pr_getSoundSupport(key, \sound);
    }

    soundHelpers {|key|
        ^this.pr_getSoundSupport(key, \helper);
    }

    pr_getSoundSupport {|key, kind = \sound|
        var dict = (kind == \sound).if({
            sounds
        }, {
            soundHelpers
        });

        dict[key].notNil.if({
            ^dict.at(key);
        }, {
            var obj = PlonkSoundSupport.new(key, proxyspace);
            obj.home = this;
            dict[key] = obj;
            ^obj;
        });
    }
    stopAllSounds {
        sounds.do{|sound| sound.stop};
    }
    playAllSounds {
        sounds.do{|sound| sound.play};
    }

    log {|who, msg, urgence = false|
        (verboseLevels.includes(who.asSymbol) || { urgence }).if{
            "*** %:".format(who).postln;
            msg.isString.if{msg = [msg]};
            msg.do{|line|"   %".format(line).postln};
        };
    }
    verbose {|who, level = true|
        who.isKindOf(ArrayedCollection).if{
            who.do{|w| this.verbose(w, level)};
            ^this;
        };
        if(level, {
            verboseLevels.add(who.asSymbol);
        }, {
            verboseLevels.remove(who.asSymbol);
        });
    }
    activateVerbose {|who|
        this.verbose(who, true);
    }
    deactivateVerbose {|who|
        this.verbose(who, false);
    }
    loadConfig {|force = false|
        var configPath = path ++ "/config.scd";

        (config.isNil || force).if{
            var inDict = this.pr_loadConfigFile(configPath, config);
            inDict.notNil.if({
                this.log(\config, "Configuration successfully loaded.");
                config = inDict;
                config.proto = this.class.protoConfig;
                this.pr_prepareServerStartup;
            }, {
                this.log(\config, "No config loaded, kept previous state.", true);
            });
        };
    }
    pr_loadConfigFile {|configPath|
        var inDict;
        File.exists(configPath).if({
            this.log(\config, "Loading config from %".format(configPath));
            inDict = configPath.load;
        }, {
            this.log(\config, "No config file found at %".format(configPath), true);
        });
        ^inDict;
    }
    pr_prepareServerStartup {
        server = config.server;
        server.latency = config.latency;
        server.options.numOutputBusChannels = config.numOutputBusChannels;
        server.options.numInputBusChannels = config.numInputBusChannels;
        server.options.maxNodes = config.maxNodes;
        server.options.memSize = config.memSize;
        server.options.numWireBufs = config.numWireBufs;
        server.options.bindAddress = config.bindAddress;
        server.options.sampleRate = config.sampleRate;
        server.options.blockSize = config.blockSize;
        server.options.numBuffers = config.numBuffers;

    	Platform.case(
            \osx,   {
                // hide SuperCollider IDE
                //unixCmd("osascript -e 'tell application \"Finder\"' -e 'set visible of process \"SuperCollider\" to false' -e 'end tell'”);
                ServerOptions.outDevices.includesEqual(config.audioDevice_OSX).if({
                    server.options.device = config.audioDevice_OSX;
                    this.log(\config, "Using audio interface %".format(server.options.device));
                }, {
                    server.options.device = nil;
                    this.log(\config, "Using default audio interface; change it via systems dialog or define preferred interface in the config file.");
                    // "(Alt + click on loudspeaker symbol in the status bar)".postln;
                });
            },
            // not needed anymore since we're running pipewire
            // \linux,   {
            //     this.log(\startup, "Using audio interface %".format(config.audioDevice_Linux));
            //     "SC_JACK_DEFAULT_OUTPUTS".setenv(config.audioDevice_Linux);
            //     "SC_JACK_DEFAULT_INPUTS".setenv(config.audioDevice_Linux);
            //     // TODO: fix async runtime error
            //     4.wait;
            // },
        );

        // tell which scripts to run after boot
        server.doWhenBooted({
            this.pr_doWhenBooted;
        });

    }

    pr_doWhenBooted {
        this.log(\startup, "Server booted.");
        this.pr_runList(config.afterBoot, asyncProtect: true);
    }

    startup {
        this.loadConfig;
        this.pr_runList(this.config.beforeBoot);
        this.startServer; // implicitly runs afterBoot scripts
    }
    sync {
        this.server.sync;
    }
    startServer {
        server.boot;        
    }

    at {|selector|
        ^dict.at(selector);
    }
    put {|selector, value|
        dict.put(selector, value);
    }
    // // forward anything else to dict
    // doesNotUnderstand { arg selector ... args;
    //     ^dict.perform(selector, *args);
    // }
    pr_runList {|list, asyncProtect = false|
        // evaluate a list of files or functions. 
        // if asyncProtect is true, the evaluation is wrapped in a Task
        // complains, if a file is not found

        var listEval = {|list|
            list.do{|filename|
                filename.isKindOf(Function).if({
                    this.log(\startup, "Executing %".format(filename));
                    filename.value;
                    asyncProtect.if({this.sync});
                }, {
                    var absPath = path +/+ filename;
                    File.exists(absPath).if({
                        this.log(\startup, "Executing %".format(filename));
                        (path +/+ filename).load;
                        asyncProtect.if({this.sync});
                    }, {
                            this.log(\startup, "!!! File % not found.".format(filename), true);
                    });
                });
            };
        };

        asyncProtect.if({
            Task {
                listEval.(list);
            }.play;
        }, {
            listEval.(list);
        });
    }

    assetsDir {
        ^(path +/+ config.relAssetsDir);
    }
}