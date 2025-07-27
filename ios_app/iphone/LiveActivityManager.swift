import ActivityKit
import Combine
import shared

private let liveActivityPublisher: AnyPublisher<LiveActivity, Never> =
    LiveActivity.companion.flow.toPublisher()

private var keepObject: Any? = nil

func initLiveActivity() {
    keepObject = liveActivityPublisher.sink { value in
        updateLiveActivity(liveActivity: value)
    }
}

///

// todo https://developer.apple.com/documentation/activitykit/activityauthorizationinfo/areactivitiesenabled
// todo rename
private func updateLiveActivity(liveActivity: LiveActivity) {
    let intervalDb = liveActivity.intervalDb
    Task {
        for activity in ActivityKit.Activity<WidgetLiveAttributes>.activities {
            await activity.end(nil, dismissalPolicy: .immediate)
        }
        
        let attributes = WidgetLiveAttributes()
        let state = WidgetLiveAttributes.ContentState(
            title: liveActivity.dynamicIslandTitle,
            endDate: Date(timeIntervalSince1970: Double(intervalDb.id + intervalDb.timer)),
        )
        
        // todo do catch
        _ = try Activity.request(
            attributes: attributes,
            content: .init(state: state, staleDate: nil),
            pushType: nil
        )
    }
}
