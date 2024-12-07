PlonkInstallation : PlonkSetup {
    var <runners;
    var <dayTimer; // global control to e.g. switch between day and night mode

    *new {|path, projectName, server|
        // ^super.new(path, projectName, server).initInstallation;
        ^super.new(path, projectName, server).initInstallation;
    }

    initInstallation {
        // daytimer
        dayTimer = DayTimer(projectName);
        dayTimer.start;
    }

    initRunners {
        // remove any running runners
        runners.notNil.if{
            runners.do{|runner| runner.stop};
        };


        runners = (
            hf: PlonkRunner.new(this, this.config.hfDt, \hf),
            mf: PlonkRunner.new(this, this.config.mfDt, \mf),
            lf: PlonkRunner.new(this, this.config.lfDt, \lf),
        )
    }

    *protoConfig {
        var dict = super.protoConfig;

        dict.putPairs([
            // world updates
            hfDt: 1, // high frequency updates
            mfDt: 80.nextPrime, // medium frequency updates
            lfDt: 240.nextPrime, // low frequency updates
        ]);
        ^dict;
    }
    startRunners {
        this.initRunners;
        runners.do{|runner| runner.start};
    }

    stopRunners {
        runners.do{|runner| runner.stop};
    }


    // start runners after execution of afterBoot scripts
    pr_doWhenBooted {
        Routine {
            super.pr_doWhenBooted;
            this.sync;
            this.log(\startup, "Starting runners.");
            this.startRunners;
        }.play;
    }

    startup {
        this.loadConfig;

        // add runnners so that they are present for beforeBoot scripts
        this.initRunners;

        //start server, runs beforeBoot and afterBoot srcripts, including starting runners
        super.startup; 
    }
}


