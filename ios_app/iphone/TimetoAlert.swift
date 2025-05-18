import SwiftUI
import Combine
import shared

// todo remove
private var modifiersStacks: [UUID] = []

extension View {

    func attachTimetoAlert() -> some View {
        modifier(TimetoAlert__Modifier())
    }
}

private struct TimetoAlert__Modifier: ViewModifier {

    @StateObject private var timetoAlert = TimetoAlert()
    @State private var isPresented = false

    private let alertPublisher: AnyPublisher<UIAlertData, Never> = Utils_kmpKt.uiAlertFlow.toPublisher()
    private let confirmationPublisher: AnyPublisher<UIConfirmationData, Never> = Utils_kmpKt.uiConfirmationFlow.toPublisher()

    @State private var uuid = UUID()

    func body(content: Content) -> some View {

        content
        .alert(
            timetoAlert.data?.title ?? "",
            isPresented: $isPresented
        ) {
            if let data = timetoAlert.data {
                let buttons = data.buttons
                ForEach(0..<buttons.count, id: \.self) { idx in
                    buttons[idx]
                }
            }
        }
        .environmentObject(timetoAlert)
        .onChange(of: timetoAlert.data?.id) { _ in
            if timetoAlert.data != nil {
                /// In case of nested calls
                isPresented = false
                DispatchQueue.main.async {
                    isPresented = true
                }
                //////
            } else {
                isPresented = false
            }
        }
        .onReceive(alertPublisher) { data in
            if modifiersStacks.last != uuid {
                return
            }
            timetoAlert.alert(data.message)
        }
        .onReceive(confirmationPublisher) { data in
            if modifiersStacks.last != uuid {
                return
            }
            timetoAlert.confirm(
                data.text,
                confirmationText: data.buttonText,
                onConfirm: data.onConfirm,
                isDestructive: data.isRed
            )
        }
        .onAppear {
            modifiersStacks.append(uuid)
        }
        .onDisappear {
            modifiersStacks.removeAll {
                $0 == uuid
            }
        }
    }
}

class TimetoAlert: ObservableObject {

    @Published fileprivate var data: TimetoAlert__Data?

    func alert(
        _ title: String,
        btnText: String = "OK",
        onClick: (() -> Void)? = nil
    ) {
        data = TimetoAlert__Data(
            id: UUID(),
            title: title,
            buttons: [
                Button(btnText, role: .cancel) {
                    if let onClick = onClick {
                        onClick()
                    }
                }
            ]
        )
    }

    func confirm(
        _ title: String,
        confirmationText: String,
        onConfirm: @escaping () -> Void,
        isDestructive: Bool = false,
        cancelText: String = "Cancel",
        onCancel: (() -> Void)? = nil
    ) {
        data = TimetoAlert__Data(
            id: UUID(),
            title: title,
            buttons: [
                Button(confirmationText, role: isDestructive ? .destructive : .none) {
                    onConfirm()
                },
                Button(cancelText, role: .cancel) {
                    if let onCancel = onCancel {
                        onCancel()
                    }
                }
            ]
        )
    }
}

struct TimetoAlert__Data: Identifiable {
    let id: UUID
    let title: String
    let buttons: [Button<Text>]
}

extension View {

    func sheetEnv<Content>(
        isPresented: Binding<Bool>,
        onDismiss: (() -> Void)? = nil,
        hideKeyboardOnPresent: Bool = true,
        @ViewBuilder content: @escaping () -> Content
    ) -> some View where Content: View {
        sheet(
            isPresented: isPresented,
            onDismiss: onDismiss,
            content: {
                content()
                    .attachTimetoAlert()
                    .attachNavigation()
            }
        )
        .onChange(of: isPresented.wrappedValue) { newValue in
            if newValue && hideKeyboardOnPresent {
                hideKeyboard()
            }
        }
    }
}
