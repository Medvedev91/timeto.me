import SwiftUI

struct NavigationLinkFullScreen<
    LabelContent: View,
    FullScreenContent: View
>: View {
    
    let label: () -> LabelContent
    let fullScreen: () -> FullScreenContent
    
    ///

    @Environment(Navigation.self) private var navigation

    var body: some View {
        // https://stackoverflow.com/a/72030978
        Button(
            action: {
                navigation.fullScreen {
                    fullScreen()
                }
            },
            label: {
                NavigationLink(
                    destination: EmptyView(),
                    label: label
                )
            }
        )
        .foregroundColor(Color(uiColor: .label))
    }
}
