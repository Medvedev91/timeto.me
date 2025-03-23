import SwiftUI
import shared

struct ActivityFormSheet: View {
    
    let activityDb: ActivityDb?
    
    var body: some View {
        VmView({
            ActivityFormVm(
                initActivityDb: activityDb
            )
        }) { vm, state in
            ActivityFormSheetInner(
                vm: vm,
                state: state,
                name: state.name
            )
        }
    }
}

private struct ActivityFormSheetInner: View {
    
    let vm: ActivityFormVm
    let state: ActivityFormVm.State
    
    @State var name: String
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        List {
            
            Section {
                TextField(
                    state.namePlaceholder,
                    text: $name
                )
                .onChange(of: name) { _, newName in
                    vm.setName(newName: newName)
                }
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.emojiTitle)
                            Spacer()
                            if let emoji = state.emoji {
                                Text(emoji)
                                    .font(.system(size: 25))
                            } else {
                                Text(state.emojiNotSelected)
                                    .foregroundColor(.red)
                            }
                        }
                    },
                    sheet: {
                        EmojiPickerSheet(
                            onPick: { emoji in
                                vm.setEmoji(newEmoji: emoji)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.colorTitle)
                            Spacer()
                            Circle()
                                .foregroundColor(state.colorRgba.toColor())
                                .frame(width: 28, height: 28)
                        }
                    },
                    sheet: {
                        ColorPickerSheet(
                            title: state.colorPickerTitle,
                            examplesData: state.buildColorPickerExamplesData(),
                            onPick: { colorRgba in
                                vm.setColorRgba(newColorRgba: colorRgba)
                            }
                        )
                    }
                )
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text("Checklists")
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.checklistsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ChecklistsPickerSheet(
                            initChecklistsDb: state.checklistsDb,
                            onPick: { newChecklistsDb in
                                vm.setChecklistsDb(newChecklistsDb: newChecklistsDb)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text("Shortcuts")
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.shortcutsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ShortcutsPickerSheet(
                            initShortcutsDb: state.shortcutsDb,
                            onPick: { newShortcutsDb in
                                vm.setShortcutsDb(newShortcutsDb: newShortcutsDb)
                            }
                        )
                    }
                )
            }
        }
        .myFormContentMargins()
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
                        onSave: {
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
            }
        }
    }
}
