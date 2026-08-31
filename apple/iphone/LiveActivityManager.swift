import ActivityKit
import Combine
import shared

private let liveActivityPublisher: AnyPublisher<LiveActivity, Never> =
    LiveActivity.companion.flow.toPublisher()

private var keepObject: Any? = nil

class LiveActivityManager {
    
    static func setup() {
        keepObject = liveActivityPublisher.sink { value in
            updateLiveActivity(liveActivity: value)
        }
    }
    
    static func isPermissionGranted() -> Bool {
        ActivityAuthorizationInfo().areActivitiesEnabled
    }
}

///

private func updateLiveActivity(liveActivity: LiveActivity) {
    if !LiveActivityManager.isPermissionGranted() {
        return
    }
    
    Task {
        for activity in ActivityKit.Activity<WidgetLiveAttributes>.activities {
            await activity.end(nil, dismissalPolicy: .immediate)
        }
        
        let attributes = WidgetLiveAttributes()
        let widgetTimerData = WidgetTimerData.build(
            timerType: liveActivity.timerType,
        )
        let state = WidgetLiveAttributes.ContentState(
            title: liveActivity.dynamicIslandTitle,
            isTimerOrStopwatch: widgetTimerData.isTimerOrStopwatch,
            date: widgetTimerData.date,
        )
        
        do {
            _ = try Activity.request(
                attributes: attributes,
                content: .init(state: state, staleDate: nil),
                pushType: nil
            )
        } catch {
            reportApi("updateLiveActivity() error:\(error)")
        }
    }
}
