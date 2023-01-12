package app.time_to.timeto.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.R

private val textPaddingTop = 16.dp
private val textLineHeight = 22.sp
private val textFontSize = 16.sp
private val textFontWeight = FontWeight.Normal

@Composable
fun ReadmeView(
    isPresented: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (!isPresented.value)
        return

    MyDialog(
        isPresented = isPresented,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.93f),
        paddingValues = PaddingValues()
    ) {

        val scroll = rememberScrollState()

        Box(
            Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(bottom = 60.dp, start = 24.dp, end = 24.dp),
            ) {

                RTitle("Personal Productivity System", paddingTop = 25.dp)
                RText("I created this app to manage my productivity system. This guide describes the system and how to get started.")
                RText("I wrote this guide as a daily reading checklist to keep the best practices in mind.")
                RText("The system is focused on:", fontWeight = FontWeight.Bold)
                RText("•  Boosting productivity;", paddingTop = 4.dp)
                RText("•  Following long-term goals.", paddingTop = 4.dp)

                RTitle("The Main Idea")
                RText("The biggest thing that increased my productivity was the idea:")
                RQuote("All we need to be productive is mental energy, not extra time.")

                val s1 = buildAnnotatedString {
                    append("It is the most important point. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("We have enough time, but not enough energy.")
                    }
                    append(" Even if you have extra time, you can't do anything without energy.")
                }
                RTextAnnotated(s1)

                RText("Unfortunately, it is almost impossible to increase mental energy, but there are ways to save it. I will show you where we lose the most energy and how to prevent it.")
                RText("The system is a set of practices on how to save mental energy, focus on tasks and follow long-term goals.")

                RTitle("How to Start Using the System")
                RText("From here to the end will be four practices:")
                RText("•  Limit social media;", paddingTop = 4.dp)
                RText("•  Turn off notifications;", paddingTop = 4.dp)
                RText("•  Timer for each task;", paddingTop = 4.dp)
                RText("•  Using the task list.", paddingTop = 4.dp)
                RText("For now, I recommend reading the whole guide. Then decide if the system is right for you. If yes, please trust me and follow the guide strictly. I worked through every single sentence in this text and use all the practices myself. If something changes, I update the guide. Last updated March 12, 2022.")

                val s2 = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Don't skip practices. ")
                    }
                    append("All practices depend on each other. The first two eliminate energy leaks and distractions. The third one helps you focus on tasks. The fourth frees your mind from storing everything.")
                }
                RTextAnnotated(s2)
                RText("I propose to start using the system right after reading this guide.")
                RText("Good luck!")

                RTitle("1. Limit Social Media")
                RText("Social media drains mental energy. Here I will show the way how I limit it.")
                RText("Don't try to get rid of this addiction completely. It will get worse. It's like gravity. The longer you hang on the bar, the harder it is to resist. Eventually, you will fall.")
                val s3 = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("To limit social media, schedule time for it. ")
                    }
                    append("Read news, watch youtube, tv, etc., always at a certain time. I do it once a day at 8 p.m.")
                }
                RTextAnnotated(s3)
                RText("Be careful, at first, you will feel uncomfortable and try to replace social media. After a few days, you will get used to it. As a bonus, you will notice that you enjoy social media more.")
                RText("I don't use any software to limit social media. It brings additional problems. For example, if I need to watch a lesson on youtube, I have to fight the software.")
                val s4 = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("What should I do if I find myself on social media? ")
                    }
                    append("It still happens to me. When I notice it, I think, \"Stop! I'll wait until 8 p.m. to continue.\"")
                }
                RTextAnnotated(s4)
                RText("It's especially bad to use social media in the morning, even for a minute. This is when you have the most energy, so spend it on useful things.")
                RText("Don't use social media to rest, it drains your energy. You will feel even worse.")

                RTitle("2. Turn Off Notifications")
                RText("Notifications became part of our lives. But it also has a negative side.")
                val s5 = buildAnnotatedString {
                    append("Notifications provoke thoughts irrelevant to the task.")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" So if you see or hear a notification, you are already interrupted.")
                    }
                }
                RTextAnnotated(s5)
                RText("This is how I manage notifications:")

                val s6 = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("PC:")
                    }
                    append(" all communication tools are always closed. Including email, messaging apps, etc. Except when they are required for the current task. I check email once a day at 12 noon.")
                }
                RTextAnnotated(s6)

                val s7 = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Smartphone:")
                    }
                    append(" always in \"Do Not Disturb\" mode. Exception when I'm waiting for a call or message. I added ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Wyou")
                    }
                    append(" app to \"ALLOWED NOTIFICATIONS\" to receive notifications from the timer. The rest of the notifications, including IM, I check between tasks. Instructions for ")

                    pushStringAnnotation(tag = "ios", annotation = "https://support.apple.com/en-us/HT204321")
                    withStyle(style = SpanStyle(color = c.blue)) {
                        append("iOS")
                    }
                    pop()

                    append(" and ")

                    pushStringAnnotation(tag = "android", annotation = "https://support.google.com/android/answer/9069335")
                    withStyle(style = SpanStyle(color = c.blue)) {
                        append("Android")
                    }

                    append(".")
                }
                ClickableText(
                    text = s7,
                    modifier = Modifier.padding(top = textPaddingTop),
                    style = prepTextStyle(),
                    onClick = { offset ->
                        s7.getStringAnnotations(tag = "ios", start = offset, end = offset).firstOrNull()?.let {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.item)))
                        }

                        s7.getStringAnnotations(tag = "android", start = offset, end = offset).firstOrNull()?.let {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.item)))
                        }
                    }
                )

                RText("At first, turn off notifications for a few hours a day. Then increases until you turn it off completely. You may feel anxious, that's okay. In a few days, you will feel better than before.")

                RText("Turning off notifications is a hard but necessary step to eliminate distractions. Just imagine: nothing distracts you, you can fully focus on your tasks.")

                RTitle("3. Timer for Each Task")
                RText("I don't know how it works. But as soon as I set a timer for each task, I get less distracted and do more. Here I will show you how I use the timer.")
                RText("The main rule:")
                RQuote("You have to set a timer for each activity. During the timer, you should only do one activity without distractions.", fontWeight = FontWeight.Bold)
                RText("The main feature of this app is that there is no \"stop\" option. Once you have completed one activity, you have to set a timer for the next one, even if it's a \"sleeping\" activity.")

                val s8 = buildAnnotatedString {
                    append("This time tracking approach provides real data on how long everything takes. You can see it on the ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Chart")
                    }
                    append(" screen. It's always surprising.")
                }
                RTextAnnotated(s8)

                RText("By default, the app contains the activities that I use: meditation, work, music, personal development, exercises, walk, getting ready, sleep / rest, other. That's all I need, but you can change it.")
                RText("Tips:")
                RTextAnnotated(buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("•  NO DISTRACTIONS!")
                    }
                    append(" Start from 20 minutes without distractions. It is possible! 🙂")
                }, paddingTop = 10.dp)
                RTextAnnotated(buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("•  Don't cheat.")
                    }
                    append(" If you stop working, set a timer for your current activity or for ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Other")
                    }
                    append(".")
                }, paddingTop = 10.dp)
                RText("This practice helps me focus on what I'm doing. You can start it right now.")

                RTitle("4. Using The Task List")
                RText("Task list frees your mind from storing everything. It is the most important part of the system.")
                RTextAnnotated(buildAnnotatedString {
                    append("I designed this app to cover the best practices I use. Everything to manage the task list is under the ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Tasks")
                    }
                    append(" tab. There are 5 folders:")
                })

                RText("1. Calendar", fontWeight = FontWeight.Bold, paddingTop = 28.dp)
                RTextAnnotated(buildAnnotatedString {
                    append("Typical calendar. Set a date and the task will automatically move to the ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Today")
                    }
                    append(" on the set date.")
                })

                RText("2. Repeating tasks", fontWeight = FontWeight.Bold, paddingTop = 28.dp)
                RTextAnnotated(buildAnnotatedString {
                    append("Tasks will be automatically moved to the ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Today")
                    }
                    append(" at specified intervals (every day, day of week, etc).")
                })
                RTextAnnotated(buildAnnotatedString {
                    append("By default, the app contains some of repeating tasks that I use. In fact, there are more than 20 tasks on my ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("repeating")
                    }
                    append(" list, like pay for the Internet, water the cactus, etc. Try to write out all your repeating tasks, I'm sure there will be as many. Just imagine how much you keep in your head. Free your mind from that.")
                })

                RText("3. Inbox", fontWeight = FontWeight.Bold, paddingTop = 28.dp)
                RText("This is where I write all ideas, thoughts, and tasks including personal ones.")
                RText("The main rule:")
                RQuote("Every time you think \"I need to remember,\" \"I will do it later,\" and so on, you have to write it.")
                RText("Adding everything to the task list is a habit that takes a long time to develop. It takes months. But it's useful right from the start.")

                RTextAnnotated(buildAnnotatedString {
                    append("This is right if ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Inbox")
                    }
                    append(" has a lot of tasks, if not it signals that you don't write everything. I have up to 50 tasks in the inbox. Just imagine if I kept it all in my head.")
                })

                RText("4. Week", fontWeight = FontWeight.Bold, paddingTop = 28.dp)
                RText("Tasks that must be done this week. Only urgent ones.")
                RText("I make this list every Monday. By default, the app contains a repeating task for that.")

                RText("5. Today", fontWeight = FontWeight.Bold, paddingTop = 28.dp)
                RText("Tasks that must be done today. Only urgent ones. If you add more, you will quickly overload yourself.")
                RText("This is the most used list, I make it every morning and look at it every hour.")
                RTextAnnotated(buildAnnotatedString {
                    append("I try to do everything from ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Today")
                    }
                    append(" in the first 6 hours after I wake up. If I have done everything from ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Today")
                    }
                    append(", I do tasks from ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Week")
                    }
                    append(" or ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Inbox")
                    }
                    append(".")
                })

                Divider(
                    color = c.dividerBackground2,
                    modifier = Modifier
                        .padding(top = 25.dp),
                    thickness = 0.8.dp
                )

                RText("Before you start using the task list, I want to warn you about the biggest danger - hidden task lists.", paddingTop = 25.dp)

                RTextAnnotated(buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Hidden task lists")
                    }
                    append(" is where you actually get tasks from. It can be a messenger, email, your brain, and so on. The more tasks you take from hidden lists, the less relevant the right list is. Eventually you quit using the right list. To avoid this, put all tasks into one list and take tasks only from it.")
                })
                RText("Task list is powerful tool. The longer you use it, the more useful it is. Once I got used to it, my life changed. Please give this practice most of your attention, it is worth it.")

                RTitle("First Steps")
                RText("Right now, fill all the events in your calendar,  all the repeating tasks, write down everything you remember in the Inbox. Try the feeling that you don't need to store anything in your mind.")
                RText("Before you begin, I want to say:")
                RQuote("Changing lives is hard. At first, you will often fail. That's ABSOLUTELY OKAY. Just keep going.")

                if (false) {
                    Divider(
                        color = c.dividerBackground2,
                        modifier = Modifier
                            .padding(top = 40.dp),
                        thickness = 0.8.dp
                    )

                    Row(
                        modifier = Modifier.padding(top = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            buildAnnotatedString {
                                append("Show ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Readme")
                                }
                                append(" on the timer")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp, bottom = 3.dp),
                            fontSize = 14.5.sp
                        )

                        /*
                        val isShowReadme = KVModel.isShowReadmeOnMainLive().observeAsState().value
                        Switch(
                            checked = isShowReadme == true,
                            onCheckedChange = {
                                scope.launchEx {
                                    KVModel__.upsert(KVModel__.KEY.IS_SHOW_README_ON_MAIN, if (it) "1" else "0")
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = c.blue,
                                checkedThumbColor = c.blue,
                            )
                        )
                         */
                    }
                }
            }

            Icon(
                painterResource(id = R.drawable.ic_round_close_24),
                "Close",
                tint = c.textSecondary,
                modifier = Modifier
                    .padding(end = 20.dp, bottom = 20.dp)
                    .alpha(0.7f)
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(c.background2)
                    .clickable {
                        isPresented.value = false
                    }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun RTitle(
    text: String,
    paddingTop: Dp = 40.dp,
) {
    Text(
        text,
        modifier = Modifier.padding(top = paddingTop),
        style = prepTextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 28.sp
        ),
    )
}

@Composable
private fun RText(
    text: String,
    paddingTop: Dp = textPaddingTop,
    fontWeight: FontWeight = textFontWeight,
) {
    Text(
        text,
        modifier = Modifier.padding(top = paddingTop),
        style = prepTextStyle(fontWeight = fontWeight)
    )
}

@Composable
private fun RTextAnnotated(
    text: AnnotatedString,
    paddingTop: Dp = textPaddingTop,
    fontWeight: FontWeight = textFontWeight,
) {
    Text(
        text,
        modifier = Modifier.padding(top = paddingTop),
        style = prepTextStyle(fontWeight = fontWeight)
    )
}


@Composable
private fun RQuote(
    text: String,
    paddingTop: Dp = textPaddingTop,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    Row(
        modifier = Modifier
            // To use fillMaxHeight() inside
            .height(IntrinsicSize.Min)
            .padding(top = paddingTop),
    ) {

        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(c.text)
        )

        Text(
            text,
            modifier = Modifier.padding(start = 10.dp, bottom = 1.dp),
            style = prepTextStyle(fontWeight = fontWeight),
        )
    }
}

@Composable
private fun prepTextStyle(
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = textFontSize,
    lineHeight: TextUnit = textLineHeight,
) = LocalTextStyle.current.merge(
    TextStyle(
        color = c.text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
    )
)
