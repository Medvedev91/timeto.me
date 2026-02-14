import SwiftUI

extension View {
    
    func customList() -> some View {
        self
            .listStyle(.plain)
            .environment(\.defaultMinListRowHeight, 0)
    }
    
    func customListItem(
        withSeparator: Bool = false,
        buttonStyle: some PrimitiveButtonStyle = .plain
    ) -> some View {
        self
            .listRowInsets(EdgeInsets())
            #if os(iOS)
            .listRowSeparator(withSeparator ? .visible : .hidden)
            #endif
            .buttonStyle(buttonStyle)
    }
}
