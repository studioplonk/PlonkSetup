/// provide moon and sun data
// always use UTC (gmtime) for calculations

// q.isNil.if{
// 	q = ();
// 	"../utils.scd".loadRelative
// };

// q.verboseLevels = q.verboseLevels ? [ \moon ];

// fail safe
// q.assetsDir = q.assetsDir ?? {thisProcess.nowExecutingPath.dirname +/+ "../../_assets/"};
// q.config.filenameMoon = q.config.filenameMoon ? "moonphases_2024-01-01_2026-09-26.csv";
// q.config.filenameSun = q.config.filenameSun ? "sunAltAzDist-utc_51.152471_-6.6437431_2024-01-01_2025-01-01.csv";


/////////// MOON ////////////////


q.dict.moon = q.dict.moon ? ();
q.dict.moon.src = q.dict.moon.src ?? {
    Moon(q.assetsDir +/+ q.config.filenameMoon)
};


q.dict.moon.ndef = Ndef(\moon, {
	var startVals = q.dict.moon.src.getDataForNow;
	VarLag.kr(\vals.kr(startVals), \lag.kr(0), start: startVals)
});


q.dict.moon.updateFunc = {
	var vals = q.dict.moon.src.getDataForNow;

	q.log(\moon, [
		"phase %, illumination %".format(*vals)
	]);
	s.sync;
	Ndef(\moon).set(\vals, vals);
	// NetAddr.localAddr.sendMsg("/moon", *vals);
	q.dict.moon[\phase] = vals[0];
	q.dict.moon[\illumination] = vals[1];
};

q.dict.moon.updater = SkipJack.new(
	updateFunc: q.dict.moon.updateFunc,
	dt: 60,
	stopTest: false,
	name: "moonUpdater"
);


/////////// SUN ////////////////

q.dict.sun = q.dict.sun ? ();
q.dict.sun.src = q.dict.sun.src ?? {
    Sun(q.assetsDir +/+ q.config.filenameSun)
};

q.dict.sun.ndef = Ndef(\sun, {
	var startVals = q.dict.sun.src.getDataForNow;
	VarLag.kr(\vals.kr(startVals), \lag.kr(0), start: startVals)
});

q.dict.sun.updateFunc = {
	var vals = q.dict.sun.src.getDataForNow;

	q.log(\sun, [
		"phase %, illumination %".format(*vals)
	]);
	s.sync;
	Ndef(\sun).set(\vals, vals);
	// NetAddr.localAddr.sendMsg("/sun", *vals);
	q.dict.sun[\distance] = vals[0];
	q.dict.sun[\alt] = vals[1];
	q.dict.sun[\az] = vals[2];
};

q.dict.sun.updater = SkipJack.new(
	updateFunc: q.dict.sun.updateFunc,
	dt: 60,
	stopTest: false,
	name: "sunUpdater"
);


/*
q.dict.moon
q.dict.sun
*/