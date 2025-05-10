import SwiftUI

struct NavigationLinkSheet<
    LabelContent: View,
    SheetContent: View
>: View {
    
    let label: () -> LabelContent
    let sheet: () -> SheetContent
    
    ///

    @Environment(Navigation.self) private var navigation

    var body: some View {
        // https://stackoverflow.com/a/72030978
        Button(
            action: {
                hideKeyboard()
                navigation.sheet {
                    sheet()
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
