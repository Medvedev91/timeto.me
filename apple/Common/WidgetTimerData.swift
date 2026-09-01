import shared

struct WidgetTimerData {
    
    let isTimerOrStopwatch: Bool
    let date: Date
    
    static func build(
        timerType: IntervalDb.TimerType,
    ) -> WidgetTimerData {
        
        let time = switch timerType {
        case let timerType as IntervalDb.TimerTypeTimer: timerType.finishTime
        case let timerType as IntervalDb.TimerTypeOverdueTimer: timerType.startTime - timerType.overdueSeconds
        case let timerType as IntervalDb.TimerTypeStopwatch: timerType.startTime - timerType.startSeconds
        default: fatalError()
        }
        
        return WidgetTimerData(
            isTimerOrStopwatch: timerType is IntervalDb.TimerTypeTimer,
            date: Date(timeIntervalSince1970: Double(time)),
        )
    }
}
