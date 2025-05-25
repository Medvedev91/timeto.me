import SwiftUI
import shared

struct ShortcutsPickerSheet: View {
    
    let initShortcutsDb: [ShortcutDb]
    let onDone: ([ShortcutDb]) -> Void
    
    var body: some View {
        VmView({
            ShortcutsPickerVm(
                initShortcutsDb: initShortcutsDb
            )
        }) { vm, state in
            ShortcutsPickerSheetInner(
                vm: vm,
                state: state,
                selectedIds: Set(state.selectedIds.map { $0.int32Value }),
                animatedShortcutsDb: state.shortcutsDbSorted,
                onDone: onDone
            )
        }
    }
}

private struct ShortcutsPickerSheetInner: View {
    
    let vm: ShortcutsPickerVm
    let state: ShortcutsPickerVm.State
    
    @State var selectedIds = Set<Int32>()
    @State var animatedShortcutsDb: [ShortcutDb]
    
    let onDone: ([ShortcutDb]) -> Void
    
    ///
    
    @State private var editMode: EditMode = .active
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List(selection: $selectedIds) {
            
            Section {
                
                ForEach(animatedShortcutsDb, id: \.id) { shortcutDb in
                    Text(shortcutDb.name)
                        .contextMenu {
                            Button("Edit") {
                                navigation.sheet {
                                    ShortcutFormSheet(
                                        shortcutDb: shortcutDb,
                                        onSave: { _ in }
                                    )
                                }
                            }
                        }
                }
            }
            .listSectionSeparator(.hidden, edges: [.top, .bottom])
        }
        .animateVmValue(value: state.shortcutsDbSorted, state: $animatedShortcutsDb)
        .listStyle(.plain)
        .interactiveDismissDisabled()
        .environment(\.editMode, $editMode)
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.title)
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button(state.doneText) {
                    dismiss()
                    onDone(vm.getSelectedShortcutsDb())
                }
                .fontWeight(.semibold)
            }
        }
        .onChange(of: selectedIds) { _, new in
            vm.setSelectedIds(ids: Set(new.map { $0.toKotlinInt() }))
        }
    }
}
