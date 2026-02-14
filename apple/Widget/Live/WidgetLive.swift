import WidgetKit
import SwiftUI

struct WidgetLive: Widget {
    
    var body: some WidgetConfiguration {
        
        ActivityConfiguration(
            for: WidgetLiveAttributes.self,
            content: { context in
                FullSizeTimerView(state: context.state)
                    .padding(.top, 12)
                    .padding(.bottom, 6)
                    .padding(.horizontal)
                    .activityBackgroundTint(.black.opacity(0.4))
            },
            dynamicIsland: { context in
                DynamicIsland(
                    expanded: {
                        DynamicIslandExpandedRegion(.center) {
                            FullSizeTimerView(state: context.state)
                        }
                    },
                    compactLeading: {
                        Text(context.state.title)
                            .textAlign(.leading)
                            .lineLimit(1)
                            .minimumScaleFactor(0.6)
                            .frame(width: min(50, CGFloat(context.state.title.count) * 10))
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
    
    let state: WidgetLiveAttributes.ContentState
    
    var body: some View {
        TimelineView(.periodic(from: Date.now, by: 1.0)) { timeline in
            // https://stackoverflow.com/a/78356027
            Text("00:00")
                .hidden()
                .overlay(alignment: .center) {
                    Text(timerInterval: state.endDate.widgetTimerRange(), countsDown: true)
                        .textAlign(.center)
                        .monospacedDigit()
                        .lineLimit(1)
                        .minimumScaleFactor(0.1)
                }
        }
    }
}

private struct FullSizeTimerView: View {
    
    let state: WidgetLiveAttributes.ContentState
    
    var body: some View {
        VStack {
            
            Text(state.title)
                .textAlign(.trailing)
                .foregroundColor(.white)
                .fontWeight(.medium)
                .padding(.trailing, 1)
                .lineLimit(1)
            
            Text(timerInterval: state.endDate.widgetTimerRange(), countsDown: true)
                .textAlign(.trailing)
                .foregroundColor(.white)
                .font(.system(size: 48, weight: .light))
        }
    }
}
