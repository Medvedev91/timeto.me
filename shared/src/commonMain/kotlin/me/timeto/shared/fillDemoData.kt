package me.timeto.shared

import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.IntervalDb
import kotlin.random.Random

suspend fun fillDemoData(
    morningGoalDb: Goal2Db,
    commuteGoalDb: Goal2Db,
    workGoalDb: Goal2Db,
    eatingGoalDb: Goal2Db,
    exercisesGoalDb: Goal2Db,
    readingGoalDb: Goal2Db,
    freeTimeGoalDb: Goal2Db,
    sleepGoalDb: Goal2Db,
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
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day1Time - h1)
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day1Time + h7_30)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day1Time + h8_30)
    IntervalDb.insertWithValidation(60, workGoalDb, null, day1Time + h9)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day1Time + h17)
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day1Time + h17_30)
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day1Time + h18)
    IntervalDb.insertWithValidation(60, exercisesGoalDb, null, day1Time + h19)
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day1Time + h20)
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day1Time + h22)
    // Day 2 - Business Day. Urgent Free Time. No Exercises.
    val day2Time: Int = startTime + (daySeconds * 1)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day2Time - h1 + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day2Time + h7_30)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day2Time + h8_30 + rand(15))
    IntervalDb.insertWithValidation(60, workGoalDb, null, day2Time + h9 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day2Time + h13 + rand(10))
    IntervalDb.insertWithValidation(60, workGoalDb, null, day2Time + h16 + rand(10))
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day2Time + h19 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day2Time + h19_30 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day2Time + h20 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day2Time + h22 + rand(10))
    // Day 3 - Business Day
    val day3Time: Int = startTime + (daySeconds * 2)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day3Time - h1 + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day3Time + h6_30)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day3Time + h8_30 + rand(15))
    IntervalDb.insertWithValidation(60, workGoalDb, null, day3Time + h9 + rand(10))
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day3Time + h17 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day3Time + h17_30 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day3Time + h18 + rand(10))
    IntervalDb.insertWithValidation(60, exercisesGoalDb, null, day3Time + h19 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day3Time + h22 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day3Time + h22 + rand(10))
    // Day 4 Saturday
    val day4Time: Int = startTime + (daySeconds * 3)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day4Time + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day4Time + h9)
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day4Time + h11 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day4Time + h13 + rand(10))
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day4Time + h13_30 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day4Time + h14 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day4Time + h18 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day4Time + h20 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day4Time + h22 + rand(10))
    // Day 5 Sunday
    val day5Time: Int = startTime + (daySeconds * 4)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day5Time + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day5Time + h8_30)
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day5Time + h11 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day5Time + h13 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day5Time + h14 + rand(10))
    IntervalDb.insertWithValidation(60, exercisesGoalDb, null, day5Time + h16 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day5Time + h20 + rand(10))
    // Day 6 - Business Day
    val day6Time: Int = startTime + (daySeconds * 5)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day6Time - h1 + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day6Time + h7_30)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day6Time + h8_30 + rand(15))
    IntervalDb.insertWithValidation(60, workGoalDb, null, day6Time + h9 + rand(10))
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day6Time + h17 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day6Time + h17_30 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day6Time + h18 + rand(10))
    IntervalDb.insertWithValidation(60, exercisesGoalDb, null, day6Time + h19 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day6Time + h20 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day6Time + h22 + rand(10))
    // Day 7 - Business Day
    val day7Time: Int = startTime + (daySeconds * 6)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day7Time - h1 + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day7Time + h7_30)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day7Time + h8_30 + rand(15))
    IntervalDb.insertWithValidation(60, workGoalDb, null, day7Time + h9 + rand(10))
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day7Time + h17 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day7Time + h17_30 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day7Time + h18 + rand(10))
    IntervalDb.insertWithValidation(60, exercisesGoalDb, null, day7Time + h19 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day7Time + h20 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day7Time + h23 + rand(10))
    // Copy Day 1
    val day8Time: Int = startTime + (daySeconds * 7)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day8Time - h1)
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day8Time + h7_30)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day8Time + h8_30)
    IntervalDb.insertWithValidation(60, workGoalDb, null, day8Time + h9)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day8Time + h17)
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day8Time + h17_30)
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day8Time + h18)
    IntervalDb.insertWithValidation(60, exercisesGoalDb, null, day8Time + h19)
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day8Time + h20)
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day8Time + h22)
    // Copy Day 2
    val day9Time: Int = startTime + (daySeconds * 8)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day9Time - h1 + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day9Time + h7_30)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day9Time + h8_30 + rand(15))
    IntervalDb.insertWithValidation(60, workGoalDb, null, day9Time + h9 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day9Time + h13 + rand(10))
    IntervalDb.insertWithValidation(60, workGoalDb, null, day9Time + h16 + rand(10))
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day9Time + h19 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day9Time + h19_30 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day9Time + h20 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day9Time + h22 + rand(10))
    // Copy Day 3 But Morning Difference
    val day10Time: Int = startTime + (daySeconds * 9)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day10Time - h1 + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day10Time + h7_30)
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day10Time + h8_30 + rand(15))
    IntervalDb.insertWithValidation(60, workGoalDb, null, day10Time + h9 + rand(10))
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day10Time + h17 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day10Time + h17_30 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day10Time + h18 + rand(10))
    IntervalDb.insertWithValidation(60, exercisesGoalDb, null, day10Time + h19 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day10Time + h22 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day10Time + h22 + rand(10))
    // Copy Day 4
    val day11Time: Int = startTime + (daySeconds * 10)
    IntervalDb.insertWithValidation(60, sleepGoalDb, null, day11Time + rand(45))
    IntervalDb.insertWithValidation(60, morningGoalDb, null, day11Time + h9)
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day11Time + h11 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day11Time + h13 + rand(10))
    IntervalDb.insertWithValidation(60, commuteGoalDb, null, day11Time + h13_30 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day11Time + h14 + rand(10))
    IntervalDb.insertWithValidation(60, eatingGoalDb, null, day11Time + h18 + rand(10))
    IntervalDb.insertWithValidation(60, readingGoalDb, null, day11Time + h20 + rand(10))
    IntervalDb.insertWithValidation(60, freeTimeGoalDb, null, day11Time + h22 + rand(10))
}

private fun rand(minutes: Int): Int =
    Random.nextInt(from = -60 * minutes, until = 60 * minutes)
