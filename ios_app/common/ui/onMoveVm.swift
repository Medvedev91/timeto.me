import SwiftUI

extension DynamicViewContent {
    
    func onMoveVm(
        action: @escaping (Int32, Int32) -> Void
    ) -> some DynamicViewContent {
        onMove { from, to in
            from.forEach { fromIdx in
                // Fix crash if single time
                if fromIdx == 0 && to == 0 {
                    return
                }
                action(fromIdx.toInt32(), (fromIdx > to ? to : (to - 1)).toInt32())
            }
        }
    }
}
