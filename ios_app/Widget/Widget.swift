import ActivityKit
import WidgetKit
import SwiftUI
import shared

struct Widget: SwiftUI.Widget {
    
    var body: some WidgetConfiguration {
        
        ActivityConfiguration(
            for: WidgetLiveAttributes.self,
            content: { context in
                HStack {
                    
                    // todo
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
    
    let state: WidgetLiveAttributes.ContentState
    
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
