import SwiftUI

extension View {
    
    func fillMaxWidth() -> some View {
        frame(minWidth: 0, maxWidth: .infinity)
    }
    
    func fillMaxHeight() -> some View {
        frame(minHeight: 0, maxHeight: .infinity)
    }
    
    func fillMaxSize() -> some View {
        fillMaxWidth().fillMaxHeight()
    }
}
