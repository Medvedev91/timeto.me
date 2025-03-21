import SwiftUI

struct NavigationLink<Content: View>: View {
    
    private let path: NavigationPath
    private let content: () -> Content

    @Environment(Navigation.self) private var navigation

    init(
        _ path: NavigationPath,
        content: @escaping () -> Content
    ) {
        self.path = path
        self.content = content
    }
    
    var body: some View {
        //
        // Doesn't work well. Caches the initial path.
        // SwiftUI.NavigationLink(value: path, label: content)
        //
        // Fix. Source: https://stackoverflow.com/a/72030978
        Button(
            action: {
                navigation.push(path)
            },
            label: {
                SwiftUI.NavigationLink(
                    destination: EmptyView(),
                    label: content
                )
            }
        )
        .foregroundColor(Color(uiColor: .label))
    }
}
