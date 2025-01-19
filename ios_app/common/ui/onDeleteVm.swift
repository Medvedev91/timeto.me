import SwiftUI

extension DynamicViewContent {
    
    func onDeleteVm(
        action: @escaping (Int) -> Void
    ) -> some DynamicViewContent {
        onDelete { indexSet in
            for idx in indexSet {
                action(idx)
            }
        }
    }
}
