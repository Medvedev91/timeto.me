import Foundation

extension Date {
    
    func rangeForTimer() -> ClosedRange<Date> {
        let now = Date.now
        return now...(self > now ? self : now)
    }
}
