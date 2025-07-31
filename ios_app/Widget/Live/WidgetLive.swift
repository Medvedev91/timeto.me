import WidgetKit
import SwiftUI

struct WidgetLive: Widget {
    
    var body: some WidgetConfiguration {
        
        ActivityConfiguration(
            for: WidgetLiveAttributes.self,
            content: { context in
                VStack {
                    
                    Text(context.state.title)
                        .textAlign(.trailing)
                        .foregroundColor(.white)
                        .fontWeight(.medium)
                        .padding(.trailing, 1)
                        .padding(.top, 12)
                        .lineLimit(1)
                    
                    Text(timerInterval: context.state.endDate.widgetTimerRange(), countsDown: true)
                        .textAlign(.trailing)
                        .foregroundColor(.white)
                        .font(.system(size: 48, weight: .light))
                        .padding(.bottom, 6)
                }
                .padding(.horizontal)
                .activityBackgroundTint(.black.opacity(0.4))
            },
            dynamicIsland: { context in
                DynamicIsland(
                    expanded: {
                        DynamicIslandExpandedRegion(.center) {
                            VStack {
                                // todo
//                                Tmp(state: context.state)
                                Button("report") {
                                    reportApi("tap")
                                }
                                
                            }
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
                        .monospacedDigit()
                        .lineLimit(1)
                        .minimumScaleFactor(0.1)
                }
        }
    }
}
