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
