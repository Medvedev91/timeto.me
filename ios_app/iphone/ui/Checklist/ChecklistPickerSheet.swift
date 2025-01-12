import SwiftUI
import shared

// todo ui
struct ChecklistPickerSheet: View {
    
    let selectedChecklists: [ChecklistDb]
    let onPick: ([ChecklistDb]) -> Void
    
    var body: some View {
        VmView({
            ChecklistsPickerSheetVm(
                selectedChecklists: selectedChecklists
            )
        }) { vm, state in
            ChecklistPickerSheetInner(
                vm: vm,
                state: state,
                selectedIds: Set(state.selectedIds.map { $0.int32Value }),
                onPick: onPick
            )
        }
    }
}

private struct ChecklistPickerSheetInner: View {
    
    let vm: ChecklistsPickerSheetVm
    let state: ChecklistsPickerSheetVm.State
    
    @State var selectedIds = Set<Int32>()
    let onPick: ([ChecklistDb]) -> Void

    ///

    @State private var editMode: EditMode = .active

    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        List(selection: $selectedIds) {
            Section {
                ForEach(state.checklistsDb, id: \.id) { checklistDb in
                    Text(checklistDb.name)
                }
            }
            .listSectionSeparator(.hidden, edges: [.top, .bottom])
        }
        .plainList()
        .environment(\.editMode, $editMode)
        .contentMargins(.vertical, 8)
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
                    onPick(vm.getSelectedChecklistsDb())
                }
                .fontWeight(.bold)
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                Button(
                    action: {
                        navigation.sheet {
                            ChecklistSettingsSheet(
                                checklistDb: nil,
                                onSave: { newChecklistDb in
                                    selectedIds.insert(newChecklistDb.id)
                                    navigation.sheet {
                                        ChecklistFormSheet(
                                            checklistDb: newChecklistDb,
                                            onDelete: {}
                                        )
                                    }
                                },
                                onDelete: {}
                            )
                        }
                    },
                    label: {
                        HStack(spacing: 8) {
                            Image(systemName: "plus.circle.fill")
                                .foregroundStyle(.blue)
                                .fontWeight(.bold)
                            Text(state.newChecklistText)
                                .foregroundColor(.blue)
                                .fontWeight(.bold)
                        }
                    }
                )
                .buttonStyle(.plain)
                Spacer()
            }
        }
        .onChange(of: selectedIds) { _, new in
            vm.setSelectedIds(ids: Set(new.map { $0.toKotlinInt() }))
        }
    }
}
