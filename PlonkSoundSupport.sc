PlonkSoundSupport {
    var <key; // a Symbol
    var <bufs; // a dictionary of Buffers
    var <actions; // a dictionary of Functions
    var <meta; // a dictionary intended for random support data
    var <>home; // possibly a PlonkSetup
    var <proxyspace; // holds the NodeProxy at the specified key

    *new {|key, proxyspace|
        ^super.new.init(key, proxyspace);
    }

    init {|argKey, argProxyspace|
        proxyspace = argProxyspace;
        key = argKey;
        bufs = ();
        actions = ();
        meta = ();
    }

    loadBuffersFrom {|dict, force = false, cue = false|
        dict.collect{|path, key|
            this.loadBufferFrom(key, path, force, cue);
       };
    }

    loadBufferFrom {|key, obj, force = false, cue = false|
        bufs[key].notNil.if({
            if (force, {
                home.log(\PlonkSoundSupport, "Reloading buffer %".format(key));
                bufs[key].free;
                bufs[key] = nil;
                obj.isKindOf(String).if({
                    this.pr_loadBufferFromPath(key, obj, cue);
                }, {
                    this.pr_loadBufferFromCollection(key, obj, cue);
                });
            }, {
                home.log(\PlonkSoundSupport, "Buffer already exists %".format(key));
            });
        }, {
            obj.isKindOf(String).if({
                this.pr_loadBufferFromPath(key, obj, cue);
            }, {
                this.pr_loadBufferFromCollection(key, obj, cue);
            });
        });
    }

    // does not test for existence
    pr_loadBufferFromPath {|key, pathname, cue|
        var action = {|buf|
            home.log(\PlonkSoundSupport, "Buffer % loaded".format(key));
        };
        var buf;

		cue.if({
			var info = SoundFile.use(pathname, {|file|
				(
					\numFrames: file.numFrames,
					\numChannels: file.numChannels,
					\sampleRate: file.sampleRate,
					\duration: file.duration
				)
			});
			buf = Buffer.cueSoundFile(
				proxyspace.server,
				path: pathname, numChannels: info[\numChannels]
			);
			action.value; // cueSoundFile does not have an action, so we do the next best thing.
		}, {
			buf = Buffer.read(proxyspace.server, pathname, action: action);
		});
        bufs[key] = buf;
    }

    // does not test for existence
    pr_loadBufferFromCollection {|key, array, cue|
        var action = {|buf|
            home.log(\PlonkSoundSupport, "Buffer % loaded".format(key));
        };
        var buf;

		cue.if({
            home.log(\PlonkSoundSupport, "Collection cannot be cued %".format(key), true);
		}, {
			buf = Buffer.loadCollection(proxyspace.server, array, action: action);
		});
        bufs[key] = buf;
    }

    prepareProxy {|numChans = 2, rate = \audio|
        this.proxy.reshaping = \expanding;
        this.proxy.mold(numChans, rate);
    }

    proxy {
        ^proxyspace.at(key)
    }

    source {
        ^this.proxy.source
    }

    at {|key|
        ^this.proxy.at(key)
    }

    put {|key, value|
        this.proxy.put(key, value);
    }

    source_ {|source|
        this.proxy.source_(source);
    }

    play {|out, numChannels, group, multi=false, vol, fadeTime, addAction|
		out = out ?? {meta[\out]};
		numChannels = numChannels ?? {meta[\numChannels]};
		group = group ?? {meta[\group]};
		multi = multi ?? {meta[\multi]};
		vol = vol ?? {meta[\vol]};
		fadeTime = fadeTime ?? {meta[\fadeTime]};
		addAction = addAction ?? {meta[\addAction]};

        this.proxy.play(out, numChannels, group, multi, vol, fadeTime, addAction);
    }

    playN {|outs , amps , ins , vol , fadeTime , group , addAction |
        outs = outs ?? {meta[\outs]};
        amps = amps ?? {meta[\amps]};
        ins = ins ?? {meta[\ins]};
        vol = vol ?? {meta[\vol]};
        fadeTime = fadeTime ?? {meta[\fadeTime]};
        group = group ?? {meta[\group]};
        addAction = addAction ?? {meta[\addAction]};


        this.proxy.playN(outs, amps, ins, vol, fadeTime, group, addAction);
    }

    stop {|fadeTime, reset = false|
        fadeTime = fadeTime ?? {meta[\fadeTime]};
        
        this.proxy.stop(fadeTime, reset);
    }

    gui {
        ^this.proxy.gui.name_(key.asString)
    }

}