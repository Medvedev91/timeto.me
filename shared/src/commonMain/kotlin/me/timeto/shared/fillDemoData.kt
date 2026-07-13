package me.timeto.shared

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import kotlin.random.Random

suspend fun fillDemoData(
    morningActivityDb: ActivityDb,
    workActivityDb: ActivityDb,
    smallTasksActivityDb: ActivityDb,
    readingActivityDb: ActivityDb,
    workoutActivityDb: ActivityDb,
    freeTimeActivityDb: ActivityDb,
    sleepActivityDb: ActivityDb,
) {
    val daySeconds: Int = 24 * 3_600
    val startTime: Int =
        UnixTime().inDays(-11).localDayStartTime()
    // Hours
    val h1 = 3_600
    val h6 = 3_600 * 6
    val h6_30 = 3_600 * 6 + 1_800
    val h7_30 = 3_600 * 7 + 1_800
    val h8_30 = 3_600 * 8 + 1_800
    val h9 = 3_600 * 9
    val h11 = 3_600 * 11
    val h13 = 3_600 * 13
    val h13_30 = 3_600 * 13 + 1_800
    val h14 = 3_600 * 14
    val h16 = 3_600 * 16
    val h17 = 3_600 * 17
    val h17_30 = 3_600 * 17 + 1_800
    val h18 = 3_600 * 18
    val h19 = 3_600 * 19
    val h19_30 = 3_600 * 19 + 1_800
    val h20 = 3_600 * 20
    val h22 = 3_600 * 22
    val h23 = 3_600 * 23
    // Day 1 - Business Day
    val day1Time: Int = startTime
    IntervalDb.insertForDemo(day1Time - h1, sleepActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h7_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h8_30, freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h9, workActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h17, freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h17_30, smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h18, freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h19, workoutActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h20, freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day1Time + h22, readingActivityDb, null)
    // Day 2 - Business Day. Urgent Free Time. No Exercises.
    val day2Time: Int = startTime + (daySeconds * 1)
    IntervalDb.insertForDemo(day2Time - h1 + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h7_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h8_30 + rand(15), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h9 + rand(10), workActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h13 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h16 + rand(10), workActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h19 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h19_30 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h20 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day2Time + h22 + rand(10), readingActivityDb, null)
    // Day 3 - Business Day
    val day3Time: Int = startTime + (daySeconds * 2)
    IntervalDb.insertForDemo(day3Time - h1 + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h6_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h8_30 + rand(15), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h9 + rand(10), workActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h17 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h17_30 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h18 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h19 + rand(10), workoutActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h22 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day3Time + h22 + rand(10), readingActivityDb, null)
    // Day 4 Saturday
    val day4Time: Int = startTime + (daySeconds * 3)
    IntervalDb.insertForDemo(day4Time + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day4Time + h9, morningActivityDb, null)
    IntervalDb.insertForDemo(day4Time + h11 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day4Time + h13 + rand(10), readingActivityDb, null)
    IntervalDb.insertForDemo(day4Time + h13_30 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day4Time + h14 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day4Time + h18 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day4Time + h20 + rand(10), readingActivityDb, null)
    IntervalDb.insertForDemo(day4Time + h22 + rand(10), freeTimeActivityDb, null)
    // Day 5 Sunday
    val day5Time: Int = startTime + (daySeconds * 4)
    IntervalDb.insertForDemo(day5Time + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day5Time + h8_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day5Time + h11 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day5Time + h13 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day5Time + h14 + rand(10), readingActivityDb, null)
    IntervalDb.insertForDemo(day5Time + h16 + rand(10), workoutActivityDb, null)
    IntervalDb.insertForDemo(day5Time + h20 + rand(10), freeTimeActivityDb, null)
    // Day 6 - Business Day
    val day6Time: Int = startTime + (daySeconds * 5)
    IntervalDb.insertForDemo(day6Time - h1 + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h7_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h8_30 + rand(15), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h9 + rand(10), workActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h17 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h17_30 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h18 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h19 + rand(10), workoutActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h20 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day6Time + h22 + rand(10), readingActivityDb, null)
    // Day 7 - Business Day
    val day7Time: Int = startTime + (daySeconds * 6)
    IntervalDb.insertForDemo(day7Time - h1 + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h7_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h8_30 + rand(15), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h9 + rand(10), workActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h17 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h17_30 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h18 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h19 + rand(10), workoutActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h20 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day7Time + h23 + rand(10), readingActivityDb, null)
    // Copy Day 1
    val day8Time: Int = startTime + (daySeconds * 7)
    IntervalDb.insertForDemo(day8Time - h1, sleepActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h7_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h8_30, freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h9, workActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h17, freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h17_30, smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h18, freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h19, workoutActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h20, freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day8Time + h22, readingActivityDb, null)
    // Copy Day 2
    val day9Time: Int = startTime + (daySeconds * 8)
    IntervalDb.insertForDemo(day9Time - h1 + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h7_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h8_30 + rand(15), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h9 + rand(10), workActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h13 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h16 + rand(10), workActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h19 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h19_30 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h20 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day9Time + h22 + rand(10), readingActivityDb, null)
    // Copy Day 3 But Morning Difference
    val day10Time: Int = startTime + (daySeconds * 9)
    IntervalDb.insertForDemo(day10Time - h1 + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h7_30, morningActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h8_30 + rand(15), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h9 + rand(10), workActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h17 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h17_30 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h18 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h19 + rand(10), workoutActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h22 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day10Time + h22 + rand(10), readingActivityDb, null)
    // Copy Day 4
    val day11Time: Int = startTime + (daySeconds * 10)
    IntervalDb.insertForDemo(day11Time + rand(45), sleepActivityDb, null)
    IntervalDb.insertForDemo(day11Time + h9, morningActivityDb, null)
    IntervalDb.insertForDemo(day11Time + h11 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day11Time + h13 + rand(10), readingActivityDb, null)
    IntervalDb.insertForDemo(day11Time + h13_30 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day11Time + h14 + rand(10), freeTimeActivityDb, null)
    IntervalDb.insertForDemo(day11Time + h18 + rand(10), smallTasksActivityDb, null)
    IntervalDb.insertForDemo(day11Time + h20 + rand(10), readingActivityDb, null)
    IntervalDb.insertForDemo(day11Time + h22 + rand(10), freeTimeActivityDb, null)
}

private fun rand(minutes: Int): Int =
    Random.nextInt(from = -60 * minutes, until = 60 * minutes)
