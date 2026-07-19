import SwiftUI
import shared

struct HomeBarIconButton<Content: View>: View {
    
    let onClick: () -> Void
    @ViewBuilder let content: () -> Content
    
    var body: some View {
        Button(
            action: {
                onClick()
            },
            label: {
                content()
            },
        )
        .frame(
            width: HomeScreen__itemHeight,
            height: HomeScreen__itemHeight,
        )
    }
}
