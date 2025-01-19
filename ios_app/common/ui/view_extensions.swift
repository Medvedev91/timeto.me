import SwiftUI

extension View {
    
    func textAlign(_ alignment: TextAlignment) -> some View {
        let frameAlignment: Alignment = switch alignment {
        case .leading: .leading
        case .center:  .center
        case .trailing: .trailing
        }
        return self
            .frame(maxWidth: .infinity, alignment: frameAlignment)
            .multilineTextAlignment(.leading)
    }
}

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
