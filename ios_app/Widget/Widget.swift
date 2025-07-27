import ActivityKit
import WidgetKit
import SwiftUI
import shared

struct Provider: TimelineProvider {
    
    func placeholder(in context: Context) -> SimpleEntry {
        SimpleEntry(date: Date(), emoji: "😀")
    }
    
    func getSnapshot(in context: Context, completion: @escaping (SimpleEntry) -> ()) {
        let entry = SimpleEntry(date: Date(), emoji: "😀")
        completion(entry)
    }
    
    func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
        var entries: [SimpleEntry] = []
        
        // Generate a timeline consisting of five entries an hour apart, starting from the current date.
        let currentDate = Date()
        for hourOffset in 0 ..< 5 {
            let entryDate = Calendar.current.date(byAdding: .hour, value: hourOffset, to: currentDate)!
            let entry = SimpleEntry(date: entryDate, emoji: "😀")
            entries.append(entry)
        }
        
        let timeline = Timeline(entries: entries, policy: .atEnd)
        completion(timeline)
    }
    
    //    func relevances() async -> WidgetRelevances<Void> {
    //        // Generate a list containing the contexts this widget is relevant in.
    //    }
}

struct SimpleEntry: TimelineEntry {
    let date: Date
    let emoji: String
}

struct WidgetEntryView : View {
    var entry: Provider.Entry

    var body: some View {
        VStack {
            Text("Time -")
            
            Text("00")
                .hidden()
                .overlay(alignment: .leading) {
                    Text(entry.date, style: .timer)
                }
            
            Text(entry.date, style: .timer)
//                .textAlign(.center)
                .frame(minWidth: 0, idealWidth: 0, maxWidth: 10, alignment: .center)
                .lineLimit(1)
                .background(.red)

            Text("Emoji - \(Int(Date().timeIntervalSince1970) % 60)")
            Text(entry.emoji)
        }
    }
}


struct Widget2: SwiftUI.Widget {
    
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "widget", provider: Provider()) { entry in
            WidgetEntryView(entry: entry)
                .containerBackground(.black, for: .widget)
        }
    }
}

struct Widget: SwiftUI.Widget {
    
    
    var body: some WidgetConfiguration {
        
        ActivityConfiguration(
            for: WidgetAttributes.self,
            content: { context in
                HStack {
                    
                    Button("Report") {
                        reportApi("test")
                    }
                    
                    Spacer()
                    
                    VStack {
                        Text(context.state.title)
                            .textAlign(.trailing)
                            .foregroundColor(.white)
                            .fontWeight(.medium)
                            .padding(.trailing, 1)
                        Text(timerInterval: context.state.endDate.widgetTimerRange(), countsDown: true)
                            .textAlign(.trailing)
                            .foregroundColor(.white)
                            .font(.system(size: 48, weight: .light))
                    }
                }
                .padding(.horizontal)
                .padding(.top, 12)
                .padding(.bottom, 6)
                .background(.black)
            },
            dynamicIsland: { context in
                DynamicIsland(
                    expanded: {
                        DynamicIslandExpandedRegion(.center) {
                            VStack {
//                                Tmp(state: context.state)
                                Button("report") {
                                    reportApi("tap")
                                }
                                
                            }
                        }
                    },
                    compactLeading: {
                        Text(context.state.title)
                            .lineLimit(1)
                            .minimumScaleFactor(0.5)
                            .frame(width: 50)
                    },
                    compactTrailing: {
                        IslandTimerView(state: context.state)
                    },
                    minimal: {
                        IslandTimerView(state: context.state)
                    }
                )
            }
        )
    }
}

private struct IslandTimerView: View {
    
    let state: WidgetAttributes.ContentState
    
    var body: some View {
        TimelineView(.periodic(from: Date.now, by: 1.0)) { timeline in
            // https://stackoverflow.com/a/78356027
            Text("00:00")
                .hidden()
                .overlay(alignment: .leading) {
                    Text(timerInterval: state.endDate.widgetTimerRange(), countsDown: true)
                        .monospacedDigit()
                        .lineLimit(1)
                        .minimumScaleFactor(0.1)
                }
        }
    }
}
