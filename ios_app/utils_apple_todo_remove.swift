import SwiftUI
import WatchConnectivity
import shared

// Watch Connectivity
func setupWCSession(_ delegate: WCSessionDelegate) {
    if WCSession.isSupported() {
        let session = WCSession.default
        session.delegate = delegate
        session.activate()
    } else {
        zlog("setupWCSession is not supported")
    }
}

//

extension Date {

    /// For device time zone
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
        UnixTime(time: Int32(timeIntervalSince1970), utcOffset: Utils_kmpKt.localUtcOffset)
    }
}

extension UnixTime {

    func toDate() -> Date {
        Date(timeIntervalSince1970: Double(time))
    }
}

extension Array {

    // todo remove
    func chunked(
        _ size: Int
    ) -> [[Element]] {
        stride(from: 0, to: count, by: size).map {
            Array(self[$0..<Swift.min($0 + size, count)])
        }
    }
}

extension FixedWidthInteger {

    func toInt() -> Int {
        Int(self)
    }

    func toInt32() -> Int32 {
        Int32(self)
    }

    func toDouble() -> Double {
        Double(self)
    }

    func toKotlinInt() -> KotlinInt {
        KotlinInt(integerLiteral: Int(self))
    }

    func toString() -> String {
        String(self)
    }

    ///

    func asUnixDayToDate() -> Date {
        Date(timeIntervalSince1970: Double(UnixTime.companion.byLocalDay(localDay: Int32(self), utcOffset: Utils_kmpKt.localUtcOffset).time))
    }

    func asTimeToDate() -> Date {
        Date(timeIntervalSince1970: Double(self))
    }
}

extension Int {

    func limitMin(_ value: Int) -> Int {
        self < value ? value : self
    }

    func limitMax(_ value: Int) -> Int {
        self > value ? value : self
    }

    func limitMinMax(_ min: Int, _ max: Int) -> Int {
        limitMin(min).limitMax(max)
    }
}

extension KotlinInt {

    func toInt() -> Int {
        Int(self)
    }
}

extension Double {

    func limitMin(_ value: Double) -> Double {
        self < value ? value : self
    }

    func limitMax(_ value: Double) -> Double {
        self > value ? value : self
    }

    func limitMinMax(_ min: Double, _ max: Double) -> Double {
        limitMin(min).limitMax(max)
    }

    ///

    func goldenRatioUp() -> Double {
        self * Double(Utils_kmpKt.GOLDEN_RATIO)
    }

    func goldenRatioDown() -> Double {
        self / Double(Utils_kmpKt.GOLDEN_RATIO)
    }
}

extension Array {

    /// https://stackoverflow.com/a/25330930
    func getOrNull(index: Int) -> Element? {
        if index < 0 {
            fatalError()
        }
        return index < count ? self[index] : nil
    }
}

extension Color {

    init(rgba: [Int]) {
        self.init(
            .sRGB,
            red: Double(rgba[0]) / 255,
            green: Double(rgba[1]) / 255,
            blue: Double(rgba[2]) / 255,
            opacity: Double(rgba.getOrNull(index: 3) ?? 255) / 255
        )
    }
}

extension ColorRgba {
    func toColor() -> Color {
        Color(rgba: [r.toInt(), g.toInt(), b.toInt(), a.toInt()])
    }
}
