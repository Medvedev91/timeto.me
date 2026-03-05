import Foundation

extension Date {
    
    func widgetTimerRange(
        isCountUpOrDown: Bool,
    ) -> ClosedRange<Date> {
        let now = Date.now
        if isCountUpOrDown {
            return self...now.inSeconds(999_999)
        }
        return now...(self > now ? self : now)
    }
}
