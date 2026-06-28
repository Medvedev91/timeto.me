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
