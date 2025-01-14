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
    
    func plainListItem() -> some View {
        self
            #if os(iOS)
            .listRowSeparator(.hidden)
            #endif
            .listRowInsets(EdgeInsets())
    }
    
    func listItemNotClickable() -> some View {
        // Disable cell clickable
        buttonStyle(PlainButtonStyle())
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


// Animate Vm Value

extension View {
    
    func animateVmValue<T: Equatable>(
        value: T,
        state: Binding<T>,
        animation: Animation = .spring(response: 0.250)
    ) -> some View {
        modifier(AnimateVmValueModifier(value: value, state: state, animation: animation))
    }
}

private struct AnimateVmValueModifier<T: Equatable>: ViewModifier {
    
    let value: T
    @Binding var state: T
    let animation: Animation
    
    func body(content: Content) -> some View {
        content
            .onChange(of: value) { _, new in
                withAnimation(animation) {
                    state = new
                }
            }
            .onAppear {
                state = value
            }
    }
}

//
