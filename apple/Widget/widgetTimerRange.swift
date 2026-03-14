import Foundation

extension Date {
    
    func widgetTimerRange(
        isTimerOrStopwatch: Bool,
    ) -> ClosedRange<Date> {
        let now = Date.now
        if isTimerOrStopwatch {
            return now...(self > now ? self : now)
        }
        return self...now.inSeconds(999_999)
    }
}
