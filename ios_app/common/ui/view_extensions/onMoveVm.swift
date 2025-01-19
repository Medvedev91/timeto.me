import SwiftUI

extension DynamicViewContent {
    
    func onMoveVm(
        action: @escaping (Int32, Int32) -> Void
    ) -> some DynamicViewContent {
        onMove { from, to in
            from.forEach { fromIdx in
                // Fix crash if single item
                // and ignore same position
                if fromIdx == to {
                    return
                }
                action(fromIdx.toInt32(), (fromIdx > to ? to : (to - 1)).toInt32())
            }
        }
    }
}
