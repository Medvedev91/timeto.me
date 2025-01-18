import SwiftUI

struct NavigationLink<Content: View>: View {
    
    private let path: NavigationPath
    private let content: () -> Content

    init(
        _ path: NavigationPath,
        content: @escaping () -> Content
    ) {
        self.path = path
        self.content = content
    }
    
    var body: some View {
        SwiftUI.NavigationLink(value: path, label: content)
    }
}
