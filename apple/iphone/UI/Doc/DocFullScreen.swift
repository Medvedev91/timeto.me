import SwiftUI
import shared

struct DocFullScreen: View {
    
    let forceRead: Bool
    
    var body: some View {
        VmView({
            DocVm()
        }) { vm, state in
            let state = vm.state.value as! DocVm.State
            DocFullScreenInner(
                vm: vm,
                state: state,
                forceRead: forceRead,
            )
        }
    }
}

private struct DocFullScreenInner: View {
    
    let vm: DocVm
    let state: DocVm.State
    
    let forceRead: Bool
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        List {
            
            if forceRead {
                
                PView {
                    Text("I force you to read this guide because without it, you will not understand how to use the app.")
                        .forceText()
                        .padding(.top, 4)
                }
                
                PView {
                    Text("Please DO NOT SKIP this! It will help you get started and begin improving your life.")
                        .forceText()
                }
                
                PView {
                    Text("Good luck!")
                        .forceText()
                }
                
                Divider()
                    .fillMaxWidth()
                    .frame(height: 1)
                    .background(.separator)
            }
            
            PView {
                Text("I built this app to manage my productivity. Here, I will ") +
                Text("SHARE")
                    .greenSemiBold() +
                Text(" my productivity system and how I use the app.")
            }
            
            PView {
                Text("My system ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about time tracking, ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about getting nice activity charts, ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about reducing wasted time.")
            }
            
            PView {
                Text("My system ") +
                Text("IS ALL ABOUT")
                    .greenSemiBold() +
                Text(" achieving my ") +
                Text("REAL-LIFE")
                    .greenSemiBold() +
                Text(" goals.")
            }
            
            PView {
                Text("For example, ") +
                Text("I DO NOT")
                    .redSemiBold() +
                Text(" care how much time I waste, but ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" if I read a book every day, ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" if I exercise every day, ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" if I don't forget anything, ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" if I constantly follow my long-term goals.")
            }
            
            PView {
                Text("Now I will show ") +
                Text("MY PERSONAL")
                    .greenSemiBold() +
                Text(" app setup with ") +
                Text("REAL-LIFE")
                    .greenSemiBold() +
                Text(" scenarios.")
            }
            
            PView {
                Text("IMPORTANT!")
                    .blueSemiBold() +
                Text(" Life is hard, life is tricky. No way to have a perfect app or system.") +
                Text(" Some solutions seem strange, but they work, they help me achieve my ") +
                Text("REAL-LIFE")
                    .greenSemiBold() +
                Text(" goals.")
            }
            
            HeaderView("Activities")
            
            PView {
                Text("The first thing you have to do is ") +
                Text("SET UP ACTIVITIES.")
                    .greenSemiBold()
            }
            
            PView {
                Text("This is how ") +
                Text("MY ACTIVITIES")
                    .greenSemiBold() +
                Text(" look in the morning, right after I wake up:")
            }
            
            ScreenshotView("doc_activities_morning", width: .infinity)
            
            PView {
                Text("During the day, I have to turn it into this:")
            }
            
            ScreenshotView("doc_activities_evening", width: .infinity)
            
            PView {
                Text("I ") +
                Text("ONLY")
                    .greenSemiBold() +
                Text(" create activities to follow my ") +
                Text("REAL-LIFE")
                    .greenSemiBold() +
                Text(" goals. I ") +
                Text("DO NOT")
                    .redSemiBold() +
                Text(" create activities just to track, like commute, eating, etc.")
            }
            
            PView {
                Text("Every activity has ") +
                Text("PRACTICAL")
                    .greenSemiBold() +
                Text(" value. Now I'll show how I set up and use each activity.")
            }
            
            HeaderView("Morning")
            
            PView {
                Text("Right after waking up, I tap the ") +
                Text("Morning")
                    .greenSemiBold() +
                Text(" activity. This is what I see:")
            }
            
            ScreenshotView("doc_morning_start")
            
            PView {
                Text("There are two important things: ")  +
                Text("TIMER")
                    .greenSemiBold() +
                Text(" and ") +
                Text("CHECKLIST.")
                    .greenSemiBold()
            }
            
            PView {
                Text("TIMER")
                    .greenSemiBold() +
                Text(" helps me limit my morning routine time. I set 2 hours, it's enough to do everything smoothly, but I don't have to spend more time.")
            }
            
            PView {
                Text("CHECKLIST")
                    .greenSemiBold() +
                Text(" helps me make sure I don't forget anything. I'm just doing step by step.")
            }
            
            PView {
                Text("Once I finish the checklist, ") +
                Text("Morning")
                    .greenSemiBold() +
                Text(" will be marked as complete:")
            }
            
            ScreenshotView("doc_morning_completed")
            
            PView {
                Text("To make ") +
                Text("Morning")
                    .greenSemiBold() +
                Text(" works this way, you have to set up two options:")
            }
            
            ScreenshotView("doc_morning_form")
            
            HeaderView("Workout")
            
            PView {
                Text("Workout")
                    .greenSemiBold() +
                Text(" works absolutely ") +
                Text("DIFFERENT.")
                    .greenSemiBold() +
                Text(" Just after tapping ") +
                Text("Workout,")
                    .greenSemiBold() +
                Text(" I see this:")
            }
            
            ScreenshotView("doc_workout_start")
            
            PView {
                Text("Two differences:") +
                Text("\n1. Workout")
                    .greenSemiBold() +
                Text(" marked ") +
                Text("AS COMPLETED")
                    .greenSemiBold() +
                Text(" even if a checklist ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" completed;") +
                Text("\n2.")
                    .greenSemiBold() +
                Text(" Instead of timer ") +
                Text("(COUNT DOWN)")
                    .redSemiBold() +
                Text(" we see a stopwatch ") +
                Text("(COUNT UP FROM 00:00).")
                    .greenSemiBold()
            }
            
            PView {
                Text("Why it works this way? As I said, I focus on ") +
                Text("PRACTICAL")
                    .greenSemiBold() +
                Text(" value. I exercise to stay healthy. ") +
                Text("I have to find a way to exercise ") +
                Text("EVERY DAY.")
                    .greenSemiBold()
            }
            
            PView {
                Text("We know, the most difficult thing is getting started. ") +
                Text("I just tap ") +
                Text("Workout")
                    .greenSemiBold() +
                Text(" (feels like I've done the first step), then commute to the place, do my workout, come back, take a shower, and have dinner.")
            }
            
            PView {
                Text("Usually, it takes up to 4 hours. ") +
                Text("I DO NOT")
                    .redSemiBold() +
                Text(" care about tracking every single step, but ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" I do workout every day.")
            }
            
            PView {
                Text("I DO NOT FORCE MYSELF")
                    .redSemiBold() +
                Text(" completing checklists, setting timer, etc.") +
                Text(" Only this way works best for me for ") +
                Text("Workout.")
                    .greenSemiBold()
            }
            
            PView {
                Text("Let's see the ") +
                Text("Workout's")
                    .greenSemiBold() +
                Text(" settings:")
            }
            
            ScreenshotView("doc_workout_form")
            
            HeaderView("Small Tasks")
            
            PView {
                Text("We all have plenty of non-urgent tasks that we constantly postpone.") +
                Text(" It could be personal matters, housework, etc.") +
                Text(" Every day, ") +
                Text("I FORCE MYSELF")
                    .greenSemiBold() +
                Text(" to spend 30 minutes for that.")
            }
            
            PView {
                Text("I just tap ") +
                Text("Small Tasks")
                    .greenSemiBold() +
                Text(" and do these tasks.") +
                Text(" After 30 minutes, the activity will be marked as complete.")
            }
            
            ScreenshotView("doc_small_tasks_progress")
            
            PView {
                Text("Settings:")
            }
            
            ScreenshotView("doc_small_tasks_form")
            
            HeaderView("timeto.me")
            
            PView {
                Text("As a ") +
                Text("timeto.me")
                    .greenSemiBold() +
                Text(" developer, I dedicate all the time I can to the project.") +
                Text(" Here's how I manage that.")
            }
            
            PView {
                Text("Tapping the ") +
                Text("timeto.me,")
                    .greenSemiBold() +
                Text(" I see this:")
            }
            
            ScreenshotView("doc_timetome_start")
            
            PView {
                Text("We see") +
                Text(" TIMER, CHECKLIST,")
                    .greenSemiBold() +
                Text(" and ") +
                Text("TASKS.")
                    .greenSemiBold()
            }
            
            PView {
                Text("I will talk about ") +
                Text("TASKS")
                    .greenSemiBold() +
                Text(" later. Let's see the ") +
                Text("CHECKLIST")
                    .greenSemiBold() +
                Text(" and ") +
                Text("TIMER")
                    .greenSemiBold() +
                Text(" for now.")
            }
            
            PView {
                Text("CHECKLIST.")
                    .greenSemiBold() +
                Text(" Every day, I start by answering user questions.") +
                Text(" Then mark the checklist ") +
                Text("AS COMPLETED.")
                    .greenSemiBold() +
                Text(" And only then start working.")
            }
            
            PView {
                Text("It may seem ") +
                Text("ILLOGICAL")
                    .redSemiBold() +
                Text(" that I mark ") +
                Text("timeto.me")
                    .greenSemiBold() +
                Text(" as completed and only then start working on it.") +
                Text(" But it works in ") +
                Text("REAL-LIFE.")
                    .greenSemiBold()
            }
            
            PView {
                Text("It works because, I don't know how much work I'll be able to get done today,") +
                Text(" and it's really frustrating that one of the activities will always remain uncompleted.")
            }
            
            PView {
                Text("I don't forget the essential tasks thanks the checklist, then I work whatever hours I can.")
            }
            
            PView {
                Text("This way, I can constantly follow my long-term plans as a ") +
                Text("timeto.me")
                    .greenSemiBold() +
                Text(" developer, without overwhelming by \"Task Management\" rituals.")
            }
            
            PView {
                Text("TIMER.")
                    .greenSemiBold() +
                Text(" I use a ") +
                Text("POMODORO-LIKE")
                    .greenSemiBold() +
                Text(" technique. I set the timer for 45 minutes, then take a break, and set the timer again.")
            }
            
            PView {
                Text("After the timer ends, it turns red and display the overdue time:")
            }
            
            ScreenshotView("doc_timetome_overdue")
            
            PView {
                Text("It ") +
                Text("DOES NOT")
                    .redSemiBold() +
                Text(" mean I'm taking a break immediately.") +
                Text(" Sometimes I want to continue working. ") +
                Text("KEEP IN MIND:")
                    .greenSemiBold() +
                Text(" the most important is ") +
                Text("PRACTICAL VALUE.")
                    .greenSemiBold()
            }
            
            PView {
                Text("You can tap the timer to start a ") +
                Text("BREAK")
                    .greenSemiBold() +
                Text(" timer:")
            }
            
            ScreenshotView("doc_timetome_break")
            
            PView {
                Text("But ") +
                Text("HONESTLY,")
                    .greenSemiBold() +
                Text(" I don't use this feature. After the break, I just tap ") +
                Text("timeto.me")
                    .greenSemiBold() +
                Text(" again to start a new 45 min timer.")
            }
            
            PView {
                Text("Settings:")
            }
            
            ScreenshotView("doc_timetome_form")
            
            HeaderView("Option1")
            
            PView {
                Text("[option1.io](https://option1.io)")
                    .underline()
                    .blueSemiBold() +
                Text(" is also my personal project.") +
                Text(" Here, I'm building a pragmatic window manager for macOS.")
            }
            
            PView {
                Text("Settings are the same as for ") +
                Text("timeto.me.")
                    .greenSemiBold()
            }
            
            HeaderView("Work")
            
            PView {
                Text("Work")
                    .greenSemiBold() +
                Text(" is a special case.") +
                Text(" Working as a developer, I have to track my working hours, there are a set of features for that.")
            }
            
            PView {
                Text("Let's see the settings:")
            }
            
            ScreenshotView("doc_work_form")
            
            PView {
                Text("Total Stopwatch")
                    .greenSemiBold() +
                Text(" makes the timer display ") +
                Text("TOTAL TIME")
                    .greenSemiBold() +
                Text(" spent on work ") +
                Text("FOR TODAY.")
                    .greenSemiBold()
            }
            
            PView {
                Text("In other words, it's a regular stopwatch ") +
                Text("(COUNT UP),")
                    .greenSemiBold() +
                Text(" but it ") +
                Text("DOES NOT")
                    .redSemiBold() +
                Text(" start from ") +
                Text("00:00,")
                    .redSemiBold() +
                Text(" it continues for the activity.")
            }
            
            PView {
                Text("This way, I can always see how much time I've spent on work today.")
            }
            
            PView {
                Text("Another feature is adding notes to the current task:")
            }
            
            ScreenshotView("doc_work_note")
            
            PView {
                Text("Tap the edit icon to make a note about the task you're working on.")
            }
            
            PView {
                Text("Then, in the history, you can see how much time you spent on each task:")
            }
            
            ScreenshotView("doc_work_history")
            
            PView {
                Text("If you're a ") +
                Text("NIGHT OWL")
                    .greenSemiBold() +
                Text(" and work after ") +
                Text("12:00 AM,")
                    .greenSemiBold() +
                Text(" you can set a ") +
                Text("DAY START TIME")
                    .greenSemiBold() +
                Text(" setting, to track the working time right.") +
                Text(" I suggest set it for 2 hours before you wake up to refresh activities while you sleep.")
            }
            
            HeaderView("Reading")
            
            PView {
                Text("I don't know how it works, but I see that constant reading ") +
                Text("MAKES PEOPLE BETTER.")
                    .greenSemiBold()
            }
            
            PView {
                Text("Some people set a goal to read for a hour a day. It ") +
                Text("DOES NOT")
                    .redSemiBold() +
                Text(" work for me.")
            }
            
            PView {
                Text("I like to read a fixed number of chapters per day. In the book I'm reading now, I read ") +
                Text("FIVE")
                    .greenSemiBold() +
                Text(" chapters a day.")
            }
            
            ScreenshotView("doc_reading_progress")
            
            PView {
                Text("Technically, it works like a counter. In practice, I tap ") +
                Text("Reading")
                    .greenSemiBold() +
                Text(" and start reading. Then I count how many chapters I've read.")
            }
            
            PView {
                Text("Settings:")
            }
            
            ScreenshotView("doc_reading_form")
            
            PView {
                Text("Timer ") +
                Text("DOES NOT")
                    .redSemiBold() +
                Text(" matter.")
            }
            
            HeaderView("Music")
            
            PView {
                Text("Music")
                    .greenSemiBold() +
                Text(" is my hobby. I try to play the piano twice a day.")
            }
            
            ScreenshotView("doc_music_progress")
            
            PView {
                Text("Settings are very similar to ") +
                Text("Reading:")
                    .greenSemiBold()
            }
            
            ScreenshotView("doc_music_form")
            
            PView {
                Text("Timer ") +
                Text("DOES NOT")
                    .redSemiBold() +
                Text(" matter.")
            }
            
            HeaderView("Free Time")
            
            PView {
                Text("I use ") +
                Text("Free Time")
                    .greenSemiBold() +
                Text(" for activities I don't need to track, like eating, walking, meeting, etc.")
            }
            
            PView {
                Text("I would like to highlight ") +
                Text("THREE")
                    .greenSemiBold() +
                Text(" key points:")
            }
            
            ScreenshotView("doc_free_time_start")
            
            PView {
                Text("1. ALWAYS COMPLETED.")
                    .greenSemiBold() +
                Text(" No sense in setting goals.")
            }
            
            PView {
                Text("2. STOPWATCH (COUNT UP FROM 00:00).")
                    .greenSemiBold() +
                Text(" Helps me control the time I spend on different tasks.") +
                Text(" Sometimes it's useful to notice that I spend too much time on something.")
            }
            
            PView {
                Text("3. NESTED CHECKLISTS.")
                    .greenSemiBold() +
                Text(" The ") +
                Text("Free Time")
                    .greenSemiBold() +
                Text(" checklist contains all sorts of things.") +
                Text(" For example, the ") +
                Text("Shopping")
                    .greenSemiBold() +
                Text(" item contains a nested checklist with a list of goods I have to buy.")
            }
            
            PView {
                Text("Settings:")
            }
            
            ScreenshotView("doc_free_time_form")
            
            HeaderView("Sleep")
            
            PView {
                Text("Sleep")
                    .greenSemiBold() +
                Text(" is another special case:")
            }
            
            ScreenshotView("doc_sleep_start")
            
            PView {
                Text("1. ALWAYS COMPLETED.")
                    .greenSemiBold() +
                Text(" Since during the day I try to mark all activities as completed, it’s really frustrating that one of them will always remain uncompleted.")
            }
            
            PView {
                Text("That is why, despite the checklist, it's better when ") +
                Text("Sleep")
                    .greenSemiBold() +
                Text(" is always completed.")
            }
            
            PView {
                Text("2. STOPWATCH (COUNT UP FROM 00:00).")
                    .greenSemiBold() +
                Text(" Some people prefer to set a timer for sleep, for example, for 7 hours.") +
                Text(" That doesn't work for me.")
            }
            
            PView {
                Text("I sleep as much as I feel I need to today.") +
                Text(" I prefer to use a stopwatch and check in the morning how long I slept.")
            }
            
            PView {
                Text("Settings:")
            }
            
            ScreenshotView("doc_sleep_form")
            
            HeaderView("Conclusion")
            
            PView {
                Text("That is all the activities I use.") +
                Text(" By default, the app comes with almost the same activities and settings.") +
                Text(" You can use this setup.")
                    .greenSemiBold()
            }
            
            PView {
                Text("I want to give you ") +
                Text("THREE TIPS")
                    .greenSemiBold() +
                Text(" dealing with activities that are extremely important to me: ") +
                Text("PROCRASTINATION, ")
                    .greenSemiBold() +
                Text("PRIORITIES,")
                    .greenSemiBold() +
                Text(" and ") +
                Text("FLEXIBILITY.")
                    .greenSemiBold()
            }
            
            HeaderView("Procrastination")
            
            PView {
                Text("All of us have been there - when it's crystal clear what we should do, but we just don't do it.")
            }
            
            PView {
                Text("I solve it simply: I open the app, see uncompleted activity, and tap on it without thinking.")
            }
            
            PView {
                Text("DO NOT THINK!")
                    .redSemiBold() +
                Text(" JUST TAP THE ACTIVITY IMMEDIATELY!")
                    .greenSemiBold()
            }
            
            PView {
                Text("ONCE AGAIN:")
                    .blueSemiBold() +
                Text(" OPEN THE APP AND TAP ON UNCOMPLETED ACTIVITY WITHOUT THINKING!")
                    .greenSemiBold()
            }
            
            PView {
                Text("The most difficult is to get started.") +
                Text(" Tapping the activity feels like you've made the first step.") +
                Text(" If you start thinking, you will continue procrastinating.")
            }
            
            PView {
                Text("It always works for me. For example, I open the app, see an uncompleted ") +
                Text("Piano,")
                    .greenSemiBold() +
                Text(" tap it immediately, make some tea, and start practicing.")
            }
            
            PView {
                Text("I hope you get the idea. Just tap ") +
                Text("WITHOUT THINKING.")
                    .greenSemiBold()
            }
            
            PView {
                Text("IMPORTANT:")
                    .blueSemiBold() +
                Text(" You should trust your activities. Only important things should be here. Otherwise, you will be overwhelmed and fail.")
            }
            
            HeaderView("Priorities")
            
            PView {
                Text("We usually start the day with the most urgent tasks and ") +
                Text("SACRIFICE")
                    .redSemiBold() +
                Text(" long-term goals, because long-term goals are not usually urgent.")
            }
            
            PView {
                Text("NO MATTER")
                    .greenSemiBold() +
                Text(" what happens, I try to start my day focusing only on what really ") +
                Text("MATTERS TO ME.")
                    .greenSemiBold()
            }
            
            PView {
                Text("My perfect day: after the morning routine, I read, then practice the piano, then work on my personal projects, and only then I get to work.")
            }
            
            PView {
                Text("Only this way I'm able to keep developing this app for years.")
            }
            
            PView {
                Text("It's very difficult, but you have to remember what is really ") +
                Text("IMPORTANT TO YOU.")
                    .greenSemiBold()
            }
            
            HeaderView("Flexibility")
            
            PView {
                Text("Unexpected things happen every day. We have to accept this fact.") +
                Text(" It's ") +
                Text("ABSOLUTELY OKAY")
                    .greenSemiBold() +
                Text(" if we can't do some activity today.")
            }
            
            PView {
                Text("For example, I have a meeting today, so I don't have time to ") +
                Text("Workout.")
                    .greenSemiBold() +
                Text(" It's absolutely okay. ") +
                Text("I JUST MARK WORKOUT AS COMPLETED")
                    .greenSemiBold() +
                Text(" and move to other activities.")
            }
            
            PView {
                Text("It may seem strange that I mark ") +
                Text("Workout")
                    .greenSemiBold() +
                Text(" as completed even if ") +
                Text("I HAVEN'T")
                    .redSemiBold() +
                Text(" done it, but I just don't want to get distracted by uncompleted activity.")
            }
            
            PView {
                Text("NOTE: ")
                    .blueSemiBold() +
                Text("I have the same activities for every day, even if ") +
                Text("I DON'T")
                    .redSemiBold() +
                Text(" need ") +
                Text("Work")
                    .greenSemiBold() +
                Text(" activity on weekends.") +
                Text(" There is an option to hide activities on selected days, but ") +
                Text("HONESTLY,")
                    .greenSemiBold() +
                Text(" I don't use it.") +
                Text(" On weekends, every morning, while planning my day, I just mark ") +
                Text("Work")
                    .greenSemiBold() +
                Text(" as complete and focus on the remaining activities.")
            }
            
            PView {
                Text("KEEP IN MIND:")
                    .blueSemiBold() +
                Text(" the most important is ") +
                Text("REAL-LIFE")
                    .greenSemiBold() +
                Text(" and ") +
                Text("PRACTICAL VALUE.")
                    .greenSemiBold()
            }
            
            HeaderView("DO NOT RUSH")
            
            PView {
                Text("I believe we do more when we don't rush.")
            }
            
            PView {
                Text("In the ") +
                Text("Procrastination")
                    .greenSemiBold() +
                Text(" section, I suggest to start an activity without thinking, it's right, but it doesn't mean you have to immediately jump into action.")
            }
            
            PView {
                Text("For example, I open the app, see an uncompleted ") +
                Text("Reading,")
                    .greenSemiBold() +
                Text(" and tap it immediately.") +
                Text(" Then I go to the park with a book and start reading there.")
            }
            
            PView {
                Text("Stay calm and start slowly.")
                    .greenSemiBold()
            }
            
            Divider()
                .fillMaxWidth()
                .frame(height: 1)
                .background(.separator)
            
            PView {
                Text("That's all for ") +
                Text("Activities.")
                    .greenSemiBold() +
                Text(" Let's move to other features.")
            }
            
            HeaderView("Timer")
            
            PView {
                Text("You may notice that every screenshot has a timer.")
            }
            
            PView {
                Text("Timer is running ") +
                Text("ALL THE TIME.")
                    .greenSemiBold() +
                Text(" There is ") +
                Text("NO STOP OPTION!")
                    .redSemiBold() +
                Text(" To stop the current activity, you have to start the next one.")
            }
            
            PView {
                Text("This way I always remember what I have to do. Also, it provides 24/7 data on how long everything takes:")
            }
            
            ScreenshotView("doc_timer_summary")
            
            HeaderView("Tasks")
            
            PView {
                Text("TASKS")
                    .greenSemiBold() +
                Text(" is a big part of the app.") +
                Text(" Let's create a task:")
            }
            
            ScreenshotView("doc_tasks_field")
            
            PView {
                Text("You can select an activity for this task.") +
                Text(" But ") +
                Text("HONESTLY,")
                    .greenSemiBold() +
                Text(" I always keep the default ") +
                Text("Free Time:")
                    .greenSemiBold()
            }
            
            ScreenshotView("doc_tasks_form")
            
            PView {
                Text("Nice!")
                    .greenSemiBold() +
                Text(" Now you will not forget to buy fruits:")
            }
            
            ScreenshotView("doc_tasks_example1")
            
            PView {
                Text("You can tap it to start a stopwatch:")
            }
            
            ScreenshotView("doc_tasks_started")
            
            PView {
                Text("But ") +
                Text("HONESTLY,")
                    .greenSemiBold() +
                Text(" I newer tap on tasks.") +
                Text(" I prefer to complete the task first, then just delete it by swiping left:")
            }
            
            ScreenshotView("doc_tasks_delete")
            
            HeaderView("Task Folders")
            
            ScreenshotView("doc_folders_example")
            
            PView {
                Text("TODAY:")
                    .greenSemiBold() +
                Text(" Tasks you need to do today.")
            }
            
            PView {
                Text("TOMORROW:")
                    .greenSemiBold() +
                Text(" Tasks that will be moved to ") +
                Text("TODAY")
                    .greenSemiBold() +
                Text(" folder tomorrow.")
            }
            
            PView {
                Text("Let's schedule a call with Ann for tomorrow.") +
                Text(" Just tap the folder and add the task:")
            }
            
            ScreenshotView("doc_folders_tomorrow")
            
            PView {
                Text("If you want to move it to another folder, like ") +
                Text("TODAY,")
                    .greenSemiBold() +
                Text(" swipe right and tap the folder you need:")
            }
            
            ScreenshotView("doc_folders_swipe")
            
            PView {
                Text("CUSTOM FOLDERS:")
                    .greenSemiBold() +
                Text(" I use a few folders:") +
                Text("\n- tasks and ideas for ") +
                Text("timeto.me;")
                    .greenSemiBold() +
                Text("\n- tasks and ideas for ") +
                Text("Option1;")
                    .greenSemiBold() +
                Text("\n- interesting quotes from ") +
                Text("books")
                    .greenSemiBold() +
                Text(" I've read.") +
                Text("\nNote that the last one is not actually \"tasks\", but it’s very convenient to store them this way.")
            }
            
            PView {
                Text("Sometimes I create temporary folders. For example, while I was writing this guide, I created a folder to store the ideas.")
            }
            
            HeaderView("Conclusion")
            
            PView {
                Text("I want to give you ") +
                Text("TWO TIPS")
                    .greenSemiBold() +
                Text(" dealing with ") +
                Text("TASKS")
                    .greenSemiBold() +
                Text(" that are extremely important to me:")
            }
            
            PView {
                Text("NEVER KEEP ANYTHING IN MIND!")
                    .redSemiBold() +
                Text(" As soon as a task or idea comes to mind, leave it to the list.") +
                Text(" We get really tired when we try to keep everything in mind. ") +
                Text("TRY TO EXPERIENCE")
                    .greenSemiBold() +
                Text(" the feeling when you don't need to remember anything. ") +
                Text("EVERYTHING")
                    .greenSemiBold() +
                Text(" in the task list.")
            }
            
            PView {
                Text("ADD NEW TASKS ONLY TO THE TOMORROW FOLDER.")
                    .greenSemiBold() +
                Text(" If I add this to ") +
                Text("TODAY,")
                    .greenSemiBold() +
                Text(" it breaks my plans and overwhelms me.")
            }
            
            HeaderView("Repeating Tasks")
            
            PView {
                Text("There are many repeating tasks or events that we have to remember.") +
                Text(" Like birthdays, recurring payments, special dates, etc.")
            }
            
            PView {
                Text("I have about 30, and have no idea how to keep them all in mind. ") +
                Text("EVERYTHING IN THE APP.")
                    .greenSemiBold()
            }
            
            PView {
                Text("You can create any kind of repeating task:") +
                Text("\n- Every Day;") +
                Text("\n- Every N Days;") +
                Text("\n- Days of the Week;") +
                Text("\n- Days of the Month;") +
                Text("\n- Days of the Year.")
            }
            
            PView {
                Text("Let's create a birthday reminder:")
            }
            
            ScreenshotView("doc_repeating_form_1")
            
            PView {
                Text("Now, on ") +
                Text("MARCH 30,")
                    .greenSemiBold() +
                Text(" this task will appear in ") +
                Text("TODAY")
                    .greenSemiBold() +
                Text(" folder, so you can't miss it.")
            }
            
            PView {
                Text("One more example: paying for internet service at the ") +
                Text("END OF THE MONTH.")
                    .greenSemiBold()
            }
            
            ScreenshotView("doc_repeating_form_2")
            
            PView {
                Text("Also, these tasks will appear on the ") +
                Text("CALENDAR.")
                    .greenSemiBold()
            }
            
            HeaderView("Calendar")
            
            ScreenshotView("doc_calendar_button")
            
            PView {
                Text("A regular calendar where you can schedule tasks. As I mentioned, repeating tasks are also here.")
            }
            
            ScreenshotView("doc_calendar_screen")
            
            Divider()
                .fillMaxWidth()
                .frame(height: 1)
                .background(.separator)
            
            HeaderView("Let's Go")
            
            PView {
                Text("I hope my app will lead you to what matters to you the most in your life.")
            }
            
            PView {
                Text("If you have any questions, please feel free to ask.")
            }
            
            AskQuestionView(
                subject: state.askQuestionSubject,
            ) {
                Text("Ask a Question")
                    .foregroundColor(.blue)
                    .fontWeight(.semibold)
            }
            .listRowSeparator(.hidden)

            Text("Go to the App")
                .foregroundColor(.white)
                .fontWeight(.semibold)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(roundedShape.fill(.blue))
                .listRowSeparator(.hidden)
                .onTapGesture {
                    vm.onRead()
                    dismiss()
                }
            
            PView {
                Text("Best regards,\n") +
                Text("[Ivan](https://github.com/Medvedev91)")
                    .underline()
                    .blueSemiBold()
            }
            .padding(.top, 40)
            .padding(.bottom, 20)
        }
        .listStyle(.plain)
        .navigationTitle("How to Use the App")
        .toolbar {
            if !forceRead {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") {
                        dismiss()
                    }
                }
            }
        }
    }
}

private struct PView<Content: View>: View {
    
    @ViewBuilder private let content: () -> Content
    
    init(_ content: @escaping () -> Content) {
        self.content = content
    }
    
    var body: some View {
        content()
            .listRowSeparator(.hidden)
    }
}

private struct HeaderView: View {
    
    private let text: String
    
    init(_ text: String) {
        self.text = text
    }
    
    var body: some View {
        Text(text)
            .font(.system(size: 30, weight: .bold))
            .listRowSeparator(.hidden)
            .padding(.top, 32)
    }
}

private struct ScreenshotView: View {
    
    private let name: String
    private let width: CGFloat
    
    init(
        _ name: String,
        width: CGFloat = 240.0,
    ) {
        self.name = name
        self.width = width
    }
    
    var body: some View {
        Image(name)
            .resizable()
            .aspectRatio(contentMode: .fit)
            .cornerRadius(16)
            .frame(width: width)
            .shadow(color: .primary, radius: onePx)
            .padding(.vertical, 4) // Paddings for shadow radius
            .listRowSeparator(.hidden)
            .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
    }
}

private extension Text {
    
    func redSemiBold() -> Text {
        foregroundColor(.red).fontWeight(.bold)
    }
    
    func greenSemiBold() -> Text {
        foregroundColor(.green).fontWeight(.bold)
    }
    
    func blueSemiBold() -> Text {
        foregroundColor(.blue).fontWeight(.bold)
    }
    
    ///
    
    func forceText() -> Text {
        foregroundColor(.blue).font(.system(size: 20, weight: .bold))
    }
}
