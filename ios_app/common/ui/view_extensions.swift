import SwiftUI

// List

extension View {
    
    func plainList() -> some View {
        self
            .listStyle(.plain)
            .environment(\.defaultMinListRowHeight, 0)
    }
    
    func listItemNoPaddings() -> some View {
        listRowInsets(EdgeInsets())
    }
    
    func listItemNotClickable() -> some View {
        buttonStyle(.plain)
    }
}

// fillMax..()

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
