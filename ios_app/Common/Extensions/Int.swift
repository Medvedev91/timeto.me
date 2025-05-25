extension Int {

    func limitMin(_ value: Int) -> Int {
        self < value ? value : self
    }

    func limitMax(_ value: Int) -> Int {
        self > value ? value : self
    }

    func limitMinMax(
        _ min: Int,
        _ max: Int
    ) -> Int {
        limitMin(min).limitMax(max)
    }
}
