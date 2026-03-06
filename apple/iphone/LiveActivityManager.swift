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
        let timerType = liveActivity.timerType
        let time = switch timerType {
        case let timerType as IntervalDb.TimerTypeCountUp: timerType.startTime
        case let timerType as IntervalDb.TimerTypeCountDown: timerType.finishTime
        default: fatalError()
        }
        let state = WidgetLiveAttributes.ContentState(
            title: liveActivity.dynamicIslandTitle,
            isCountUpOrDown: liveActivity.timerType is IntervalDb.TimerTypeCountUp,
            date: Date(timeIntervalSince1970: Double(time)),
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
