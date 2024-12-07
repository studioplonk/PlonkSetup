TimeUtils {
    *secsPerDay {
        ^86400; // constant
    }
    *isLeapYear {|year = 2024|
        var isLeapYear;
        if (year % 4 == 0, {
            // Divisible by 4
            if (year % 100 == 0, {
                // Divisible by 100
                if (year % 400 == 0, {
                    // Divisible by 400
                    isLeapYear = true;
                } , {
                    // Not divisible by 400
                    isLeapYear = false;
                })
            }, {
                // Not divisible by 100
                isLeapYear = true;
            })
        }, {
            // Not divisible by 4
            isLeapYear = false;
        });

        ^isLeapYear
    }
    *daysInYear {|year|
	    ^(this.isLeapYear(year)).if(366, 365)
    }
    *toJulianDate {|year = 2022, month =1, day = 1|
        var b;

        // [year, month, day].postln;
        if (month <= 2, {
            year = year - 1;
            month = month + 12;
        } );

        b = 2 - trunc(year/100) + trunc(year/400);

        ^(trunc(365.25 * (year + 4716)) + trunc(30.6001 * (month + 1)) + day + b - 1524.5);
    }
    *toJulianDateFromDate {|date|
        var month, year, day;
        day = date.day + ((date.hour + (date.minute / 60)) / 24);
        month = date.month;
        year = date.year;

        ^this.toJulianDate(year, month, day)
    }
}
