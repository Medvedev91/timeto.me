import SwiftUI
import WatchConnectivity
import shared

//
// KMP

let Cache = shared.Cache.shared

func time() -> Int {
    Atm_kmp_appleKt.time().toInt()
}

func reportApi(_ message: String) {
    Utils_kmpKt.reportApi(message: message, force: false)
}

//

func buildTimerFont(size: CGFloat) -> Font {
    Font.custom("NotoSansMono-ExtraBold", size: size)
}

func ForEachIndexed<T, Content: View>(
    _ items: Array<T>,
    @ViewBuilder content: @escaping (Int, T) -> Content
) -> some View {
    ForEach(Array(items.enumerated()), id: \.offset) { index, item in
        content(index, item)
    }
}

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

func myAsync(_ function: @escaping () -> Void) {
    DispatchQueue.main.async {
        function()
    }
}

func myAsyncAfter(
    _ seconds: CGFloat,
    work: @escaping () -> Void
) {
    DispatchQueue.main.asyncAfter(deadline: .now() + seconds) {
        work()
    }
}

//

// todo move to KMM

func is12HoursFormat() -> Bool {
    DateFormatter.dateFormat(fromTemplate: "j", options: 0, locale: Locale.current)?.range(of: "a") != nil
}

//

struct Padding: View {

    var horizontal: Double = 0
    var vertical: Double = 0

    var body: some View {
        ZStack {
        }
                .frame(width: horizontal)
                .frame(height: vertical)
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

    //////

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

//////

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

///
/// https://medium.com/@michael.forrest.music/how-to-make-a-scrollview-or-list-in-swiftui-that-starts-from-the-bottom-b0c4a69beb0d

struct FlippedUpsideDown: ViewModifier {
    func body(content: Content) -> some View {
        content
                .rotationEffect(.radians(.pi))
                .scaleEffect(x: -1, y: 1, anchor: .center)
    }
}

extension View {
    func flippedUpsideDown() -> some View {
        self.modifier(FlippedUpsideDown())
    }
}

/// https://stackoverflow.com/a/62588295/5169420
/// Made for scroll calculation for RepeatingFormSheet
struct ViewOffsetKey: PreferenceKey {

    typealias Value = CGFloat
    static var defaultValue = CGFloat.zero

    static func reduce(value: inout Value, nextValue: () -> Value) {
        value += nextValue()
    }
}
