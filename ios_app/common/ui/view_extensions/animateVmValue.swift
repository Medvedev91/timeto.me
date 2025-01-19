import SwiftUI

extension View {
    
    func animateVmValue<T: Equatable>(
        value: T,
        state: Binding<T>,
        animation: Animation = .spring(response: 0.250),
        enabled: Bool = true,
        onChange: @escaping (() -> Void) = {}
    ) -> some View {
        modifier(
            AnimateVmValueModifier(
                value: value,
                state: state,
                animation: animation,
                enabled: enabled,
                onChange: onChange
            )
        )
    }
}

///

private struct AnimateVmValueModifier<
    T: Equatable
>: ViewModifier {
    
    let value: T
    @Binding var state: T
    let animation: Animation
    let enabled: Bool
    let onChange: () -> Void
    
    func body(content: Content) -> some View {
        content
            .onChange(of: value) { _, new in
                if enabled {
                    withAnimation(animation) {
                        state = new
                    }
                } else {
                    state = new
                }
            }
            .onAppear {
                state = value
            }
    }
}
