import shared

extension Double {

    func limitMin(_ value: Double) -> Double {
        self < value ? value : self
    }

    func limitMax(_ value: Double) -> Double {
        self > value ? value : self
    }

    func limitMinMax(
        _ min: Double,
        _ max: Double
    ) -> Double {
        limitMin(min).limitMax(max)
    }

    ///

    func goldenRatioUp() -> Double {
        self * Double(GoldenRatioKt.goldenRatio)
    }

    func goldenRatioDown() -> Double {
        self / Double(GoldenRatioKt.goldenRatio)
    }
}
