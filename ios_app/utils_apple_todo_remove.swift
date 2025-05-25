import SwiftUI
import shared

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

extension Color {

    init(r: Int, g: Int, b: Int, a: Int) {
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
