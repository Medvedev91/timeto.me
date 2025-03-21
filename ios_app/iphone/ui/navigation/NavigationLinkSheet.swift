import SwiftUI

struct NavigationLinkSheet<
    LabelContent: View,
    SheetContent: View
>: View {
    
    private let label: () -> LabelContent
    private let sheet: () -> SheetContent

    @Environment(Navigation.self) private var navigation

    init(
        label: @escaping () -> LabelContent,
        sheet: @escaping () -> SheetContent
    ) {
        self.label = label
        self.sheet = sheet
    }
    
    var body: some View {
        // Source: https://stackoverflow.com/a/72030978
        Button(
            action: {
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
