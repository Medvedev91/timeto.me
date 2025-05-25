import SwiftUI
import shared

struct TasksTabRepeatingsView: View {
    
    var body: some View {
        VmView({
            TasksTabRepeatingsVm()
        }) { _, state in
            TasksTabRepeatingsViewInner(
                state: state
            )
        }
    }
}

private struct TasksTabRepeatingsViewInner: View {
    
    let state: TasksTabRepeatingsVm.State
    
    ///
    
    @Environment(Navigation.self) private var navigation
    @Environment(\.defaultMinListRowHeight) private var minListRowHeight

    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            
            VStack {
                
                let repeatingsUi = state.repeatingsUi.reversed()
                
                ForEach(repeatingsUi, id: \.repeatingDb.id) { repeatingUi in
                    
                    let isFirst = repeatingsUi.first == repeatingUi
                    
                    ZStack(alignment: .top) {
                        
                        Button(
                            action: {
                                navigation.sheet {
                                    RepeatingFormSheet(
                                        initRepeatingDb: repeatingUi.repeatingDb
                                    )
                                }
                            },
                            label: {
                                
                                VStack {
                                    
                                    HStack {
                                        Text(repeatingUi.dayLeftString)
                                            .font(.system(size: 14, weight: .light))
                                            .foregroundColor(.secondary)
                                        
                                        Spacer()
                                        
                                        Text(repeatingUi.dayRightString)
                                            .font(.system(size: 14, weight: .light))
                                            .foregroundColor(.secondary)
                                    }
                                    
                                    HStack {
                                        
                                        Text(repeatingUi.listText)
                                            .textAlign(.leading)
                                        
                                        Spacer()
                                        
                                        TriggersIconsView(
                                            checklistsDb: repeatingUi.textFeatures.checklists,
                                            shortcutsDb: repeatingUi.textFeatures.shortcuts
                                        )
                                        
                                        if (repeatingUi.repeatingDb.isImportant) {
                                            Image(systemName: "flag.fill")
                                                .foregroundColor(.red)
                                                .padding(.leading, 8)
                                        }
                                    }
                                    .padding(.top, 4)
                                }
                                .padding(.top, 10)
                                .padding(.bottom, 10)
                                .foregroundColor(.primary)
                            }
                        )
                        .padding(.horizontal, H_PADDING)

                        if !isFirst {
                            Divider()
                                .padding(.horizontal, H_PADDING)
                        }
                    }
                }
                
                Button(
                    action: {
                        navigation.sheet {
                            RepeatingFormSheet(
                                initRepeatingDb: nil
                            )
                        }
                    },
                    label: {
                        Text("New Repeating Task")
                            .fillMaxSize()
                            .fontWeight(.semibold)
                            .foregroundColor(.white)
                            .background(squircleShape.fill(.blue))
                    }
                )
                .frame(height: minListRowHeight)
                .padding(.top, 20)
                .padding(.bottom, 20)
                .padding(.horizontal, H_PADDING - 1)
            }
        }
        .defaultScrollAnchor(.bottom)
    }
}
