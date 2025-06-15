import SwiftUI

extension View {
    
    func animateVmValue<T: Equatable>(
        vmValue: T,
        swiftState: Binding<T>,
        animation: Animation = .spring(response: 0.250),
        enabled: Bool = true,
        onChange: @escaping (() -> Void) = {}
    ) -> some View {
        modifier(
            AnimateVmValueModifier(
                vmValue: vmValue,
                swiftState: swiftState,
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
    
    let vmValue: T
    @Binding var swiftState: T
    let animation: Animation
    let enabled: Bool
    let onChange: () -> Void
    
    func body(content: Content) -> some View {
        content
            .onChange(of: vmValue) { _, new in
                if enabled {
                    withAnimation(animation) {
                        swiftState = new
                    }
                } else {
                    swiftState = new
                }
                onChange()
            }
            .onAppear {
                swiftState = vmValue
            }
    }
}
