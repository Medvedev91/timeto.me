import shared

extension UnixTime {

    func toDate() -> Date {
        Date(timeIntervalSince1970: Double(time))
    }
}
