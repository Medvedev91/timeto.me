import SwiftUI

extension View {
    
    func animateVmValue<T: Equatable>(
        value: T,
        state: Binding<T>,
        animation: Animation = .spring(response: 0.250)
    ) -> some View {
        modifier(AnimateVmValueModifier(value: value, state: state, animation: animation))
    }
}

///

private struct AnimateVmValueModifier<
    T: Equatable
>: ViewModifier {
    
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
