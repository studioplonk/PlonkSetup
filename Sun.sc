Sun {
    var <>data;
    var <>rawData;
    var <header;
    var <precision;
    var numFramesPerDay;
    var numFramesPerLeapYear;

    *new {|filename|
        ^super.new.init(filename)
    }
    init {|filename|
        data = SemiColonFileReader.read(filename, skipEmptyLines: true);
        header = data[0][2..];
        // // remove leading # from first header element
        // header[0] = header[0][1..];



        // remove whitespace from header elements and convert symbols
        header = header.collect{|el| el.replace(" ", "").asSymbol};
        
        data = data[1..].collect{|row|
            [
                row[0].asFloat, // ordinate
                row[1].asFloat,//  Julian date
                row[2].asFloat, // distance
                (row[3].asFloat / 360) * 2pi, // alt
                (row[4].asFloat / 360) * 2pi, // az
                // sin((row[3].asFloat / 360) * 2pi), // sin(az)
                // sin((row[4].asFloat / 360) * 2pi), // sin(az)
            ]
        };
        data = data[0..data.size-2];

        precision = data[1][1] - data[0][1];


        // rawData = SemiColonFileReader.read(filename, skipEmptyLines: true)[1..].collect{|row|
        //     [
        //         row[0].asFloat, // ordinate
        //         row[1].asFloat, // JD, timezone corrected
        //         row[2].asFloat, // distance
        //         row[3].asFloat, // alt
        //         row[4].asFloat, // az
        //     ]
        // };
        // rawData = rawData[0..rawData.size-2];

        numFramesPerDay = 24;
        numFramesPerLeapYear = numFramesPerDay * 366;
    }

    getDataForNow {
        ^this.getDataFor(Date.gmtime)
    }

    getDataFor {|date|
        var julianDate = TimeUtils.toJulianDateFromDate(date);
        var julianDateRounded = julianDate.roundUp(precision);

        var idx = data.detectIndex{|row| row[1].roundUp(precision) >= julianDateRounded};

        var prev = data[idx];
        var next = data[idx + 1];
        var prevTime = prev[1];
        var nextTime = next[1];
        var phase = (julianDate - prevTime) / (nextTime - prevTime);

        prev = prev[2..];
        next = next[2..];
        // julianDate.postln;

        ^prev + ((next-prev)* phase);        
    }

}