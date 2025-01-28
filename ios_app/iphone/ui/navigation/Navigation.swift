import SwiftUI
import shared

extension View {

    func attachNavigation() -> some View {
        modifier(NavigationModifier())
    }
}

@MainActor
@Observable
class Navigation: DialogsManager {
    
    fileprivate var pathList: [NavigationPath] = []
    fileprivate var sheetViews = [NavigationSheet<AnyView>]()
    fileprivate var fullScreenViews = [NavigationFullScreen<AnyView>]()
    fileprivate var alertViews = [NavigationAlert]()
    fileprivate var confirmationViews = [NavigationConfirmation]()
    
    ///
    
    func push(_ path: NavigationPath) {
        pathList.append(path)
    }
    
    func cleanPath() {
        pathList.removeAll()
    }
    
    ///
    
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
            let alert = NavigationAlert(
                message: message,
                onRemove: { id in
                    self.alertViews.removeAll { $0.id == id }
                }
            )
            alertViews.append(alert)
        }
    }
    
    nonisolated func confirmation(
        message: String,
        buttonText: String,
        onConfirm: @escaping () -> Void
    ) {
        Task { @MainActor in
            let confirmation = NavigationConfirmation(
                message: message,
                buttonText: buttonText,
                onConfirm: onConfirm,
                onRemove: { id in
                    self.confirmationViews.removeAll { $0.id == id }
                }
            )
            confirmationViews.append(confirmation)
        }
    }
}

///

private struct NavigationModifier: ViewModifier {
    
    @State private var navigation = Navigation()
    
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
            
            ForEach(navigation.fullScreenViews) { fullScreenView in
                fullScreenView
            }
            
            ForEach(navigation.alertViews) { alertView in
                alertView
            }
            
            ForEach(navigation.confirmationViews) { confirmationView in
                confirmationView
            }
        }
        .environment(navigation)
    }
}
