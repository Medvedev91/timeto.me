import shared

extension KotlinInt {

    func toInt() -> Int {
        Int(truncating: self)
    }
}

extension [KotlinInt] {
    
    func toIntList() -> [Int] {
        self.map { $0.toInt() }
    }
}
