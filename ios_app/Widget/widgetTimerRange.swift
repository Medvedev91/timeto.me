import Foundation

extension Date {
    
    func widgetTimerRange() -> ClosedRange<Date> {
        let now = Date.now
        return now...(self > now ? self : now)
    }
}
