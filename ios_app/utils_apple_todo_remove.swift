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

// todo move to KMM

func is12HoursFormat() -> Bool {
    DateFormatter.dateFormat(fromTemplate: "j", options: 0, locale: Locale.current)?.range(of: "a") != nil
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

///
/// Exceptions

extension Error {

    func myMessage() -> String {
        if let error = self as? MyError {
            return error.message
        }
        return localizedDescription
    }
}

class MyError: Error {

    let message: String

    init(_ message: String) {
        self.message = message
    }
}

///

extension Array {

    /// https://stackoverflow.com/a/25330930
    func getOrNull(index: Int) -> Element? {
        if index < 0 {
            fatalError()
        }
        return index < count ? self[index] : nil
    }
}

extension UIColor {

    convenience init(argb: UInt) {
        self.init(
            red: Double((argb >> 16) & 0xff) / 255,
            green: Double((argb >> 8) & 0xff) / 255,
            blue: Double((argb >> 0) & 0xff) / 255,
            alpha: Double((argb >> 24) & 0xff) / 255
        )
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

    init(rgbaString: String) {
        self.init(rgba: rgbaString.split(separator: ",").map {
            Int($0)!
        })
    }
}

extension ColorRgba {
    func toColor() -> Color {
        Color(rgba: [r.toInt(), g.toInt(), b.toInt(), a.toInt()])
    }
}

extension View {

    /**
     * https://www.avanderlee.com/swiftui/conditional-view-modifier/
     */
    @ViewBuilder func conditional<Content: View>(
        _ condition: Bool,
        transform: (Self) -> Content
    ) -> some View {
        if condition {
            transform(self)
        } else {
            self
        }
    }

    /**
     * https://stackoverflow.com/a/72435691
     */
    @ViewBuilder func cornerRadius(
        _ radius: CGFloat,
        onTop: Bool,
        onBottom: Bool
    ) -> some View {
        self
                .conditional(onTop) { view in
                    view
                            .padding(.bottom, radius)
                            .cornerRadius(radius)
                            .padding(.bottom, -radius)
                }
                .conditional(onBottom) { view in
                    view
                            .padding(.top, radius)
                            .cornerRadius(radius)
                            .padding(.top, -radius)
                }
    }
}
