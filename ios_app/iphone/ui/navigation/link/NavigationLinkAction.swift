import SwiftUI

struct NavigationLinkAction<
    LabelContent: View
>: View {
    
    let label: () -> LabelContent
    let action: () -> Void
    
    ///
    
    var body: some View {
        // https://stackoverflow.com/a/72030978
        Button(
            action: {
                action()
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
