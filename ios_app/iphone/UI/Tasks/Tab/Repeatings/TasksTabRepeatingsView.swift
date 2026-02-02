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
                    TasksTabRepeatingsItemView(
                        repeatingUi: repeatingUi,
                        withTopDivider: repeatingsUi.first != repeatingUi,
                    )
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
            }
            .padding(.horizontal, H_PADDING)
        }
        .defaultScrollAnchor(.bottom)
    }
}
