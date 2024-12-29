import SwiftUI

extension View {
    
    func textAlign(_ alignment: Alignment) -> some View {
        frame(maxWidth: .infinity, alignment: alignment)
    }
}

// List

extension View {
    
    func plainList() -> some View {
        self
            .listStyle(.plain)
            .buttonStyle(PlainButtonStyle()) // Disable cell clickable
            .environment(\.defaultMinListRowHeight, 0)
    }
    
    func plainListItem() -> some View {
        self
            #if os(iOS)
            .listRowSeparator(.hidden)
            #endif
            .listRowInsets(EdgeInsets())
    }
}
