import shared

extension KotlinInt {

    func toInt() -> Int {
        Int(truncating: self)
    }
}
