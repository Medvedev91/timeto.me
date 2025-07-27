import WidgetKit
import SwiftUI

// todo Enable in WidgetBundle

struct WidgetStatic: SwiftUI.Widget {
    
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "widget", provider: Provider()) { entry in
            WidgetEntryView(entry: entry)
                .containerBackground(.black, for: .widget)
        }
    }
}

///

private struct Provider: TimelineProvider {
    
    func placeholder(in context: Context) -> SimpleEntry {
        SimpleEntry(date: Date(), emoji: "😀")
    }
    
    func getSnapshot(in context: Context, completion: @escaping (SimpleEntry) -> ()) {
        let entry = SimpleEntry(date: Date(), emoji: "😀")
        completion(entry)
    }
    
    func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
        Task {
            
            // todo need to init kmp
            
            var entries: [SimpleEntry] = []
            
            reportApi("temp getTimeline()")
            
            try await Task.sleep(nanoseconds: 1_000_000_000)
            
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
    }
}

private struct SimpleEntry: TimelineEntry {
    let date: Date
    let emoji: String
}

private struct WidgetEntryView : View {
    
    var entry: Provider.Entry

    var body: some View {
        VStack {
            Text("Time -")
                .foregroundColor(.white)
            
            Text("00")
                .hidden()
                .overlay(alignment: .leading) {
                    Text(entry.date, style: .timer)
                }
            
            Text(entry.date, style: .timer)
//                .textAlign(.center)
                .frame(minWidth: 0, idealWidth: 0, maxWidth: 10, alignment: .center)
                .lineLimit(1)
                .foregroundColor(.white)

            Text("Emoji - \(Int(Date().timeIntervalSince1970) % 60)")
            Text(entry.emoji)
        }
    }
}
