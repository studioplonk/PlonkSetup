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

    loadBuffersFrom {|dict, force = false|
        var server = proxyspace.server;
        var action;

        dict.collect{|path, key|
            this.loadBufferFrom(key, path, force);
       };
    }

    loadBufferFrom {|key, obj, force = false|
        bufs[key].notNil.if({
            if (force, {
                home.log(\PlonkSoundSupport, "Reloading buffer %".format(key));
                bufs[key].free;
                bufs[key] = nil;
                obj.isKindOf(String).if({
                    this.pr_loadBufferFromPath(key, obj);
                }, {
                    this.pr_loadBufferFromCollection(key, obj);
                });
            }, {
                home.log(\PlonkSoundSupport, "Buffer already exists".format(key));
            });
        }, {
            obj.isKindOf(String).if({
                this.pr_loadBufferFromPath(key, obj);
            }, {
                this.pr_loadBufferFromCollection(key, obj);
            });
        });
    }

    // does not test for existence
    pr_loadBufferFromPath {|key, pathname|
        var action = {|buf|
            home.log(\PlonkSoundSupport, "Buffer % loaded".format(key));
        };
        var buf = Buffer.read(proxyspace.server, pathname, action: action);
        bufs[key] = buf;
    }

    // does not test for existence
    pr_loadBufferFromCollection {|key, array|
        var action = {|buf|
            home.log(\PlonkSoundSupport, "Buffer % loaded".format(key));
        };
        var buf = Buffer.loadCollection(proxyspace.server, array, action: action);
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
        this.proxy.play(out, numChannels, group, multi, vol, fadeTime, addAction);
    }



    playN {|outs , amps , ins , vol , fadeTime , group , addAction |
        this.proxy.playN(outs, amps, ins, vol, fadeTime, group, addAction);
    }

    stop {|fadeTime, reset = false|
        this.proxy.stop(fadeTime, reset);
    }

}