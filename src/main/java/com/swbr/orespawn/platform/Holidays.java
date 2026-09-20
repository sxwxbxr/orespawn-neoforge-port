package com.swbr.orespawn.platform;

import com.swbr.orespawn.OreSpawn;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * The holiday date check of {@code OreSpawnMain.make_some_more_things}
 * (OreSpawnMain.java:4178-4232).
 *
 * <p>The original read a {@link GregorianCalendar} exactly once, while the mod loaded; a server
 * that started on October 30th and kept running never saw Halloween (verhalten/core-01a.md,
 * section 6). DECISIONS R18 keeps that: evaluated at mod construction (both sides, like preInit)
 * and again at {@code ServerAboutToStartEvent}, never per tick. Easter is fixed on April 20th,
 * as in the code, not as in the research.
 *
 * <p>Month is 0-based ({@code get(2)} = {@link Calendar#MONTH}), so 9 is October, 1 February and
 * 3 April.
 */
public final class Holidays {

    private Holidays() {}

    /** Reads the local clock and sets the three flags on {@link OreSpawn}. */
    public static void evaluate() {
        final GregorianCalendar gcalendar = new GregorianCalendar();
        final int nowmonth = gcalendar.get(Calendar.MONTH);
        final int nowday = gcalendar.get(Calendar.DAY_OF_MONTH);
        // PORT: the original only ever set the flags to 1 (initialised 0 in the class
        // initialiser, :6223-6224). Assigning both ways lets the server-start re-evaluation of
        // R18 clear a flag that the mod-construction pass set on a different day.
        OreSpawn.halloween = (nowmonth == 9 && nowday == 31) ? 1 : 0;
        OreSpawn.valentines_day = (nowmonth == 1 && nowday == 14) ? 1 : 0;
        OreSpawn.easter_day = (nowmonth == 3 && nowday == 20) ? 1 : 0;
        if (OreSpawn.halloween != 0 || OreSpawn.valentines_day != 0 || OreSpawn.easter_day != 0) {
            OreSpawn.LOG.info("OreSpawn holiday flags: halloween={}, valentines_day={}, easter_day={}",
                    OreSpawn.halloween, OreSpawn.valentines_day, OreSpawn.easter_day);
        }
    }
}
