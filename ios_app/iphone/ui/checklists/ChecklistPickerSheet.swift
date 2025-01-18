import SwiftUI
import shared

struct ChecklistPickerSheet: View {
    
    let selectedChecklistsDb: [ChecklistDb]
    let onPick: ([ChecklistDb]) -> Void
    
    var body: some View {
        VmView({
            ChecklistPickerVm(
                selectedChecklistsDb: selectedChecklistsDb
            )
        }) { vm, state in
            ChecklistPickerSheetInner(
                vm: vm,
                state: state,
                selectedIds: Set(state.selectedIds.map { $0.int32Value }),
                animatedChecklistsDb: state.checklistsDbSorted,
                onPick: onPick
            )
        }
    }
}

private struct ChecklistPickerSheetInner: View {
    
    let vm: ChecklistPickerVm
    let state: ChecklistPickerVm.State
    
    @State var selectedIds = Set<Int32>()
    @State var animatedChecklistsDb: [ChecklistDb]
    
    let onPick: ([ChecklistDb]) -> Void
    
    ///
    
    @State private var editMode: EditMode = .active
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        ScrollViewReader { scrollProxy in
            
            List(selection: $selectedIds) {
                
                Section {
                    
                    ForEach(animatedChecklistsDb, id: \.id) { checklistDb in
                        Text(checklistDb.name)
                    }
                }
                .listSectionSeparator(.hidden, edges: [.top, .bottom])
            }
            .animateVmValue(value: state.checklistsDbSorted, state: $animatedChecklistsDb)
            .plainList()
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
                        onPick(vm.getSelectedChecklistsDb())
                    }
                    .fontWeight(.bold)
                }
                
                ToolbarItemGroup(placement: .bottomBar) {
                    
                    Button(
                        action: {
                            if let firstChecklistDb = state.checklistsDbSorted.first {
                                withAnimation {
                                    scrollProxy.scrollTo(firstChecklistDb.id)
                                }
                            }
                            navigation.sheet {
                                ChecklistFormSheet(
                                    checklistDb: nil,
                                    onSave: { newChecklistDb in
                                        selectedIds.insert(newChecklistDb.id)
                                        navigation.sheet {
                                            ChecklistItemsFormSheet(
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
}
