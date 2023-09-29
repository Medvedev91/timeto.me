import SwiftUI

// not used
struct TabReadmeView: View {

    @Binding var isPresented: Bool
    @State private var isReadmeOnTimer = false

    static private let SIDE_PADDING: CGFloat = 30

    var body: some View {

        let rViews: [AnyView] = [
            RTitle("Personal Productivity System"),
            RParagraph(Text("I created this app to manage my productivity system. This guide describes the system and how to get started.")),
            RParagraph(Text("I wrote this guide as a daily reading checklist to keep the best practices in mind.")),
            RParagraph(Text("The system is focused on:")),
            RListItem(Text("Boosting productivity;").rBold()),
            RListItem(Text("Following long-term goals.").rBold()),

            RTitle("The Main Idea"),
            RParagraph(Text("The biggest thing that increased my productivity was the idea:"), paddingTop: 25),
            RQuote("All we need to be productive is mental energy, not extra time."),
            RParagraph(
                    Text("It is the most important point. ") +
                    Text("We have enough time, but not enough energy. ").rBold() +
                    Text("Even if you have extra time, you can't do anything without energy.")
            ),
            RParagraph(Text("Unfortunately, it is almost impossible to increase mental energy, but there are ways to save it. I will show you where we lose the most energy and how to prevent it.")),
            RParagraph(Text("The system is a set of practices on how to save mental energy, focus on tasks and follow long-term goals.")),

            RTitle("How to Start Using the System"),
            RParagraph(Text("From here to the end will be four practices:")),

            RListItem(Text("Limit social media;").rBold()),
            RListItem(Text("Turn off notifications;").rBold()),
            RListItem(Text("Timer for each task;").rBold()),
            RListItem(Text("Using the task list.").rBold()),

            RParagraph(Text("For now, I recommend reading the whole guide. Then decide if the system is right for you. If yes, please trust me and follow the guide strictly. I worked through every single sentence in this text and use all the practices myself. If something changes, I update the guide. Last updated March 12, 2022.")),

            RParagraph(
                    Text("Don't skip practices. ").rBold() +
                    Text("All practices depend on each other. The first two eliminate energy leaks and distractions. The third one helps you focus on tasks. The fourth frees your mind from storing everything."),
                    paddingTop: 20
            ),

            RParagraph(Text("I propose to start using the system right after reading this guide.")),
            RParagraph(Text("Good luck!")),

            RTitle("1. Limit Social Media"),
            RParagraph(Text("Social media drains mental energy. Here I will show the way how I limit it.")),
            RParagraph(Text("Don't try to get rid of this addiction completely. It will get worse. It's like gravity. The longer you hang on the bar, the harder it is to resist. Eventually, you will fall.")),
            RParagraph(
                    Text("To limit social media, schedule time for it. ").rBold() +
                    Text("Read news, watch youtube, tv, etc., always at a certain time. I do it once a day at 8 p.m.")
            ),
            RParagraph(Text("Be careful, at first, you will feel uncomfortable and try to replace social media. After a few days, you will get used to it. As a bonus, you will notice that you enjoy social media more.")),
            RParagraph(Text("I don't use any software to limit social media. It brings additional problems. For example, if I need to watch a lesson on youtube, I have to fight the software.")),
            RParagraph(
                    Text("What should I do if I find myself on social media? ").rBold() +
                    Text("It still happens to me. When I notice it, I think, \"Stop! I'll wait until 8 p.m. to continue.\"")
            ),
            RParagraph(Text("It's especially bad to use social media in the morning, even for a minute. This is when you have the most energy, so spend it on useful things.")),
            RParagraph(Text("Don't use social media to rest, it drains your energy. You will feel even worse.")),

            RTitle("2. Turn Off Notifications"),
            RParagraph(Text("Notifications became part of our lives. But it also has a negative side.")),
            RParagraph(
                    Text("Notifications provoke thoughts irrelevant to the task. ")
                    + Text("So if you see or hear a notification, you are already interrupted.").rBold()
            ),

            RParagraph(Text("This is how I manage notifications:")),

            RParagraph(
                    Text("PC: ").rBold() +
                    Text("all communication tools are always closed. Including email, messaging apps, etc. Except when they are required for the current task. I check email once a day at 12 noon.")
            ),
            RParagraph(
                    Text("Smartphone: ").rBold() +
                    Text("always in \"Do Not Disturb\" mode. Exception when I'm waiting for a call or message. I added ") +
                    Text("Wyou ").rBold() +
                    Text("app to \"ALLOWED NOTIFICATIONS\" to receive notifications from the timer. The rest of the notifications, including IM, I check between tasks.")
            ),
            RParagraph(Text("At first, turn off notifications for a few hours a day. Then increases until you turn it off completely. You may feel anxious, that's okay. In a few days, you will feel better than before.")),
            RParagraph(Text("Turning off notifications is a hard but necessary step to eliminate distractions. Just imagine: nothing distracts you, you can fully focus on your tasks.")),

            RTitle("3. Timer for Each Task"),
            RParagraph(Text("I don't know how it works. But as soon as I set a timer for each task, I get less distracted and do more. Here I will show you how I use the timer.")),
            RParagraph(Text("The main rule:")),
            RQuote("You have to set a timer for each activity. During the timer, you should only do one activity without distractions."),
            RParagraph(Text("The main feature of this app is that there is no \"stop\" option. Once you have completed one activity, you have to set a timer for the next one, even if it's a \"sleeping\" activity.")),
            RParagraph(
                    Text("This time tracking approach provides real data on how long everything takes. You can see it on the ")
                    + Text("Chart").rBold()
                    + Text(" screen. It's always surprising.")
            ),
            RParagraph(Text("By default, the app contains the activities that I use: meditation, work, music, personal development, exercises, walk, getting ready, sleep / rest, other. That's all I need, but you can change it.")),
            RParagraph(Text("Tips:")),
            RListItem(Text("NO DISTRACTIONS! ").rBold() + Text("Start from 20 minutes without distractions. It is possible! 🙂")),
            RListItem(Text("Don't cheat. ").rBold() + Text("If you stop working, set a timer for your current activity or for Other.")),
            RParagraph(Text("This practice helps me focus on what I'm doing. You can start it right now. Good luck!")),

            RTitle("4. Using the Task List"),
            RParagraph(Text("Task list frees your mind from storing everything. It is the most important part of the system.")),
            RParagraph(
                    Text("I designed this app to cover the best practices I use. Everything to manage the task list is under the ")
                    + Text("Tasks").rBold()
                    + Text(" tab. There are 5 folders:")
            ),

            RParagraph(Text("1. Calendar").rBold(), paddingTop: 40),
            RParagraph(
                    Text("Typical calendar. Set a date and the task will automatically move to the ")
                    + Text("Today").rBold()
                    + Text(" on the set date.")
            ),

            RParagraph(Text("2. Repeating tasks").rBold(), paddingTop: 40),
            RParagraph(
                    Text("Tasks will be automatically moved to the ")
                    + Text("Today").rBold()
                    + Text(" at specified intervals (every day, day of week, etc).")
            ),
            RParagraph(
                    Text("By default, the app contains some of repeating tasks that I use. In fact, there are more than 20 tasks on my ")
                    + Text("repeating").rBold()
                    + Text(" list, like pay for the Internet, water the cactus, etc. Try to write out all your repeating tasks, I'm sure there will be as many. Just imagine how much you keep in your head. Free your mind from that.")
            ),

            RParagraph(Text("3. Inbox").rBold(), paddingTop: 40),
            RParagraph(Text("This is where I write all ideas, thoughts, and tasks including personal ones.")),
            RParagraph(Text("The main rule:")),
            RQuote("Every time you think \"I need to remember,\" \"I will do it later,\" and so on, you have to write it."),
            RParagraph(Text("Adding everything to the task list is a habit that takes a long time to develop. It takes months. But it's useful right from the start.")),
            RParagraph(
                    Text("This is right if ")
                    + Text("Inbox").rBold()
                    + Text(" has a lot of tasks, if not it signals that you don't write everything. I have up to 50 tasks in the inbox. Just imagine if I kept it all in my head.")
            ),

            RParagraph(Text("4. Week").rBold(), paddingTop: 40),
            RParagraph(Text("Tasks that must be done this week. Only urgent ones.")),
            RParagraph(Text("I make this list every Monday. By default, the app contains a repeating task for that.")),

            RParagraph(Text("5. Today").rBold(), paddingTop: 40),
            RParagraph(Text("Tasks that must be done today. Only urgent ones. If you add more, you will quickly overload yourself.")),
            RParagraph(Text("This is the most used list, I make it every morning and look at it every hour.")),
            RParagraph(
                    Text("I try to do everything from ")
                    + Text("Today").rBold()
                    + Text(" in the first 6 hours after I wake up. If I have done everything from ")
                    + Text("Today").rBold()
                    + Text(", I do tasks from ")
                    + Text("Week").rBold()
                    + Text(" or ")
                    + Text("Inbox.").rBold()
            ),
            RDivider(paddingTop: 40),

            RParagraph(Text("Before you start using the task list, I want to warn you about the biggest danger - hidden task lists.")),
            RParagraph(
                    Text("Hidden task lists ")
                    + Text(" is where you actually get tasks from. It can be a messenger, email, your brain, and so on. The more tasks you take from hidden lists, the less relevant the right list is. Eventually you quit using the right list. To avoid this, put all tasks into one list and take tasks only from it.")
            ),
            RParagraph(Text("Task list is powerful tool. The longer you use it, the more useful it is. Once I got used to it, my life changed. Please give this practice most of your attention, it is worth it.")),

            RTitle("5. First Steps"),
            RParagraph(Text("Right now, fill all the events in your calendar,  all the repeating tasks, then write down everything you remember in the Inbox. Try the feeling that you don't need to store anything in your mind.")),
            RParagraph(Text("Before you begin, I want to say:")),
            RQuote("Changing lives is hard. At first, you will often fail. That's ABSOLUTELY OKAY. Just keep going."),
            RDivider(paddingTop: 40),
        ]


        ZStack(alignment: .bottomTrailing) {

            ScrollView {

                VStack(spacing: 0) {
                    ForEach(0..<rViews.count, id: \.self) { rView in
                        rViews[rView]
                    }
                    Toggle(
                            "Show Readme on Timer",
                            isOn: $isReadmeOnTimer
                    )
                            .padding(.top, 35)
                            .padding(.leading, TabReadmeView.SIDE_PADDING)
                            .padding(.trailing, TabReadmeView.SIDE_PADDING)
                }
                        .padding(.bottom, 55)
            }

            Button(
                    action: {
                        isPresented = false
                    },
                    label: {
                        Image(systemName: "xmark")
                                .padding(.top, 9)
                                .padding(.bottom, 9)
                                .foregroundColor(.secondary)

                    }
            )
                    .frame(width: 35, height: 35)
                    .padding(.trailing, 30)
        }
    }

    private func RTitle(
            _ text: String
    ) -> AnyView {
        AnyView(
                Text(text)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .font(.system(size: 32, weight: .medium))
                        .padding(.leading, TabReadmeView.SIDE_PADDING)
                        .padding(.trailing, TabReadmeView.SIDE_PADDING)
                        .padding(.top, 45)
                        .padding(.bottom, 2)
                        .myMultilineText()
        )
    }

    private func RParagraph(
            _ textView: Text,
            paddingTop: CGFloat = 20,
            paddingBottom: CGFloat = 0,
            paddingLeading: CGFloat = TabReadmeView.SIDE_PADDING
    ) -> AnyView {
        AnyView(
                textView
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .foregroundColor(.secondary)
                        .font(.system(size: 18))
                        .lineSpacing(3)
                        .padding(.leading, paddingLeading)
                        .padding(.trailing, 30)
                        .padding(.top, paddingTop)
                        .padding(.bottom, paddingBottom)
                        .myMultilineText()
        )
    }

    private func RQuote(
            _ text: String
    ) -> AnyView {
        AnyView(
                HStack {
                    RoundedRectangle(cornerRadius: 5, style: .continuous)
                            .frame(maxWidth: 5, maxHeight: .infinity)
                            .padding(.leading, TabReadmeView.SIDE_PADDING)
                            .foregroundColor(.primary)
                    RParagraph(
                            Text(text).rBold(),
                            paddingTop: 2,
                            paddingBottom: 2,
                            paddingLeading: 5
                    )
                }
                        .padding(.top, 20)
        )
    }

    private func RListItem(
            _ text: Text,
            paddingTop: CGFloat = 10
    ) -> AnyView {
        AnyView(
                HStack(spacing: 0) {
                    Text("•")
                            .rBold()
                            .frame(maxWidth: 12, maxHeight: .infinity, alignment: .topLeading)
                            .padding(.top, 1.5)
                    RParagraph(
                            text,
                            paddingTop: 2,
                            paddingBottom: 2,
                            paddingLeading: 5
                    )
                }
                        .padding(.top, paddingTop)
                        .padding(.leading, TabReadmeView.SIDE_PADDING)
        )
    }

    private func RDivider(
            paddingTop: CGFloat = 0
    ) -> AnyView {
        AnyView(
                AnyView(
                        ZStack {
                        }
                                .frame(maxWidth: .infinity)
                                .frame(height: 0.5)
                                .background(.secondary)
                                .opacity(0.5)
                )
                        .padding(.top, paddingTop)
                        .padding(.leading, TabReadmeView.SIDE_PADDING)
                        .padding(.trailing, TabReadmeView.SIDE_PADDING)
        )
    }
}

private extension Text {
    func rBold() -> Text {
        bold()
                .foregroundColor(.primary)
    }
}
