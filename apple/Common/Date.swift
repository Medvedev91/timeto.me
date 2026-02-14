import shared

extension Date {

    // For device time zone
    func startOfDay() -> Date {
        Calendar.current.startOfDay(for: self)
    }

    func inSeconds(_ seconds: Int) -> Date {
        var components = DateComponents()
        components.second = seconds
        return Calendar.current.date(
            byAdding: components,
            to: self
        )!
    }

    func toUnixTime() -> UnixTime {
        UnixTime(time: Int32(timeIntervalSince1970), utcOffset: LocalUtcOffsetKt.localUtcOffset)
    }
}
