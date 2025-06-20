import SwiftUI
import shared

struct ChecklistsPickerSheet: View {
    
    let initChecklistsDb: [ChecklistDb]
    let onDone: ([ChecklistDb]) -> Void
    
    var body: some View {
        VmView({
            ChecklistsPickerVm(
                initChecklistsDb: initChecklistsDb
            )
        }) { vm, state in
            ChecklistsPickerSheetInner(
                vm: vm,
                state: state,
                selectedIds: Set(state.selectedIds.map { $0.int32Value }),
                animatedChecklistsDb: state.checklistsDbSorted,
                onDone: onDone
            )
        }
    }
}

private struct ChecklistsPickerSheetInner: View {
    
    let vm: ChecklistsPickerVm
    let state: ChecklistsPickerVm.State
    
    @State var selectedIds = Set<Int32>()
    @State var animatedChecklistsDb: [ChecklistDb]
    
    let onDone: ([ChecklistDb]) -> Void
    
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
                            .contextMenu {
                                Button("Edit") {
                                    navigation.sheet {
                                        ChecklistFormItemsSheet(
                                            checklistDb: checklistDb,
                                            onDelete: {
                                                self.selectedIds.remove(checklistDb.id)
                                            }
                                        )
                                    }
                                }
                            }
                    }
                }
                .listSectionSeparator(.hidden, edges: [.top, .bottom])
            }
            .animateVmValue(vmValue: state.checklistsDbSorted, swiftState: $animatedChecklistsDb)
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
                        onDone(vm.getSelectedChecklistsDb())
                    }
                    .fontWeight(.semibold)
                }
                
                ToolbarItemGroup(placement: .bottomBar) {
                    
                    BottomBarAddButton(
                        text: state.newChecklistText,
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
                                            ChecklistFormItemsSheet(
                                                checklistDb: newChecklistDb,
                                                onDelete: {}
                                            )
                                        }
                                    },
                                    onDelete: {}
                                )
                            }
                        }
                    )
                    
                    Spacer()
                }
            }
            .onChange(of: selectedIds) { _, new in
                vm.setSelectedIds(ids: Set(new.map { $0.toKotlinInt() }))
            }
        }
    }
}
