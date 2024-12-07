Moon {
    var <>data;

    *new {|filename|
        ^super.new.init(filename);
    }

    init {|filename|
        data = SemiColonFileReader.read(filename, skipEmptyLines: true)[1..].collect{|row|
            [
                row[0].asFloat,
                row[1].asFloat,
                row[2].replace(" ", ""),
                (row[3].asFloat / 360) * 2pi, // phase
                row[4].asFloat / 100
            ]
        };
    }

    getDataForNow {
        var prev = this.getDataForRelToNow(0);
        var next = this.getDataForRelToNow(1);

        // decimal daytime of the current day 
        var  phase = Date.gmtime.rawSeconds % 86400 / 86400;

        // interpolate between the two closest data points
        ^(prev + ((next-prev)* phase));
    }

    getDataForRelToNow { |days = 0|
        var today = Date.gmtime;
        var date = today.copy.day_(today.day + days).resolve;

        ^this.getDataFor(date);
    }

    getDataFor {|date|
        var isostring = date.format("%Y-%m-%d"); 
        // returns tuple [phase[degree], illumination[%]]
        ^data.select{|row| (row[2] == isostring)}.first[3..]
    }


}