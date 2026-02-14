import Foundation
import shared

extension Set<Int> {
    
    func toKotlin() -> Set<KotlinInt> {
        Set<KotlinInt>(map { KotlinInt(int: Int32($0)) })
    }
}

extension Set<KotlinInt> {
    
    func toSwift() -> Set<Int> {
        Set<Int>(map { $0.toInt() })
    }
}
