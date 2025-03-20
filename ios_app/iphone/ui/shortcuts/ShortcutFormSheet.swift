import SwiftUI
import shared

struct ShortcutFormSheet: View {
    
    let shortcutDb: ShortcutDb?
    let onSave: (ShortcutDb) -> Void

    var body: some View {
        VmView({
            ShortcutFormVm(
                shortcutDb: shortcutDb
            )
        }) { vm, state in
            ShortcutFormSheetInner(
                vm: vm,
                state: state,
                name: state.name,
                uri: state.uri,
                onSave: onSave
            )
        }
    }
}

private struct ShortcutFormSheetInner: View {
    
    let vm: ShortcutFormVm
    let state: ShortcutFormVm.State
    
    @State var name: String
    @State var uri: String
    
    let onSave: (ShortcutDb) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List {
            
            Section(state.nameHeader) {
                TextField(
                    state.namePlaceholder,
                    text: $name
                )
                .onChange(of: name) { _, new in
                    vm.setName(name: new)
                }
            }
            
            Section(state.uriHeader) {
                TextField(
                    state.uriPlaceholder,
                    text: $uri
                )
                .onChange(of: uri) { _, new in
                    vm.setUri(uri: new)
                }
            }
            
            
            Section("EXAMPLES") {
                
                ForEach(examples, id: \.self) { example in
                    
                    let isSelected: Bool = state.uri == example.uri
                    
                    Button(
                        action: {
                            name = example.name
                            uri = example.uri
                            hideKeyboard()
                        },
                        label: {
                            
                            HStack {
                                
                                Text(example.name)
                                    .foregroundColor(.primary)
                                
                                Spacer()
                                
                                Text(example.hint)
                                    .foregroundColor(.secondary)
                                
                                if isSelected {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundColor(.green)
                                        .padding(.leading, 8)
                                        .transition(.opacity)
                                }
                            }
                            .animation(.spring(response: 0.1), value: isSelected)
                        }
                    )
                }
            }
            
            if let shortcutDb = state.shortcutDb {
                Section {
                    Button(state.deleteText) {
                        vm.delete(
                            shortcutDb: shortcutDb,
                            dialogsManager: navigation,
                            onDelete: {
                                dismiss()
                            }
                        )
                    }
                    .foregroundColor(.red)
                }
            }
        }
        .interactiveDismissDisabled()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.title)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(state.saveText) {
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: { newShortcutDb in
                            onSave(newShortcutDb)
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
                .disabled(!state.isSaveEnabled)
            }
        }
    }
}

///

private struct ShortcutExample: Hashable {
    let name: String
    let hint: String
    let uri: String
}

private let examples: [ShortcutExample] = [
    ShortcutExample(name: "10-Minute Meditation", hint: "Youtube", uri: "https://www.youtube.com/watch?v=O-6f5wQXSu8"),
    ShortcutExample(name: "Podcasts", hint: "Open App", uri: "https://podcasts.apple.com"),
    ShortcutExample(name: "Play a Song 😈", hint: "Music App", uri: "https://music.apple.com/ru/album/highway-to-hell/574043989?i=574044008&l=en"),
]
