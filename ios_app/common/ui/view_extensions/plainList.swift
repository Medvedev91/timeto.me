import SwiftUI

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
