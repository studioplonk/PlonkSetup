PlonkRunner {
    // MFunc interface: value(state, dt, runner)

    var <state;
    var <name;
    var <mFunc;
    var <task; // TODO: possibly use SkipJack here?
    var clock;

    *new {|state, dt = 1, name = "runner"|
        ^super.new.init(state, dt, name);
    }

    init {|argState, dt, argName|
        mFunc = MFunc.new;
        state = argState;
        name = argName;
        clock = TempoClock(dt.reciprocal);
        task = Task({
            loop {
                // tempo is determined by the clock's tempo, we do something between all beats
                0.5.wait;
                state.log(\runner, "Running %".format(name));
                mFunc.value(state, this.dt, this);
                0.5.wait;
            };
        });
    }


    // start and stop task
    start {
        task.play(clock);
    }

    stop {
        task.stop;
    }


    dt { 
        ^clock.tempo.reciprocal()
     }
    dt_ {|dt|
        clock.tempo = dt.reciprocal();
    }


    // Mfunc interface
	add { |name, func, active = true, addAction = \replace, otherName|
        mFunc.add(name, func, active, addAction, otherName);
    }

	replace { |name, func, active = true|
        mFunc.replace(name, func, active);
    }

	addLast { |name, func, active = true| // no where
        mFunc.addLast(name, func, active);
	}

	addFirst { |name, func, active = true| // no where
        addFirst(name, func, active);
	}

	addBefore { |name, func, active = true, otherName|
        mFunc.addBefore(name, func, active, otherName);
	}

	addAfter { |name, func, active = true, otherName|
        mFunc.addAfter(name, func, active, otherName);
	}

	remove {|name|
        mFunc.remove(name);
	}

	enable { |names| 
        mFunc.enable(names);        
    }


}