import SwiftUI
import shared

struct ActivitiesFormSheet: View {
    
    var body: some View {
        VmView({
            ActivitiesFormVm()
        }) { vm, state in
            ActivitiesFormSheetInner(
                vm: vm,
                state: state
            )
        }
    }
}

private struct ActivitiesFormSheetInner: View {
    
    let vm: ActivitiesFormVm
    let state: ActivitiesFormVm.State
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @State private var editMode: EditMode = .active
    
    var body: some View {
        List {
            Section {
                ForEach(state.activitiesUi, id: \.activityDb.id) { activityUi in
                    Button(activityUi.title) {
                        navigation.sheet {
                            ActivityFormSheet(
                                activityDb: activityUi.activityDb
                            )
                        }
                    }
                    .contextMenu {
                        Button(
                            action: {
                                navigation.sheet {
                                    ActivityFormSheet(
                                        activityDb: activityUi.activityDb
                                    )
                                }
                            },
                            label: {
                                Label("Edit", systemImage: "square.and.pencil")
                            }
                        )
                    }
                }
                .onMoveVm { fromIdx, toIdx in
                    vm.moveIos(fromIdx: fromIdx, toIdx: toIdx)
                }
            }
            .listSectionSeparator(.hidden, edges: [.top, .bottom])
        }
        .environment(\.editMode, $editMode)
        .listStyle(.plain)
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Close") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    dismiss()
                }
                .fontWeight(.semibold)
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                
                BottomBarAddButton(
                    text: "New Activity",
                    action: {
                        navigation.sheet {
                            ActivityFormSheet(
                                activityDb: nil
                            )
                        }
                    }
                )
                
                Spacer()
            }
        }
    }
}
