import SwiftUI

extension View {
    
    func onTouchGesture(
        action: @escaping () -> Void
    ) -> some View {
        modifier(OnTouchModifier(action: action))
    }
}

///

private struct OnTouchModifier: ViewModifier {
    
    let action: () -> Void
    
    ///

    @State private var isTapped = false
    
    func body(content: Content) -> some View {
        content.simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in
                    if !isTapped {
                        isTapped = true
                        action()
                    }
                }
                .onEnded { _ in
                    isTapped = false
                }
        )
    }
}
