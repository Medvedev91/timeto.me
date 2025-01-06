import SwiftUI
import shared

extension View {

    func attachNavigation() -> some View {
        modifier(NavigationModifier())
    }
}

@MainActor
class Navigation: ObservableObject, DialogsManager {
    
    @Published fileprivate var pathList: [NavigationPath] = []
    @Published fileprivate var sheetViews = [NavigationSheet<AnyView>]()
    @Published fileprivate var alertViews = [NavigationAlert]()

    func push(_ path: NavigationPath) {
        pathList.append(path)
    }
    
    func sheet<Content: View>(
        @ViewBuilder content: @escaping () -> Content
    ) {
        sheetViews.append(
            NavigationSheet(
                content: {
                    AnyView(content())
                },
                onRemove: { id in
                    self.sheetViews.removeAll { $0.id == id }
                }
            )
        )
    }
    
    nonisolated func alert(message: String) {
        Task { @MainActor in
            alertViews.append(NavigationAlert(message: message, onRemove: { id in
                self.alertViews.removeAll { $0.id == id }
            }))
        }
    }
}

///

private struct NavigationModifier: ViewModifier {

    @StateObject private var navigation = Navigation()

    func body(content: Content) -> some View {
        
        ZStack {
            
            NavigationStack(path: $navigation.pathList) {
                content.navigationDestination(for: NavigationPath.self) { value in
                    AnyView(value.rend())
                }
            }
            
            ForEach(navigation.sheetViews) { sheetView in
                sheetView
            }
            
            ForEach(navigation.alertViews) { alertView in
                alertView
            }
        }
        .environmentObject(navigation)
    }
}
