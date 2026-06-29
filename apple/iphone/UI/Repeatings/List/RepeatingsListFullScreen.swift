import SwiftUI
import shared

struct RepeatingsListFullScreen: View {
    
    var body: some View {
        VmView({
            RepeatingsListVm()
        }) { vm, state in
            let state = vm.state.value as! RepeatingsListVm.State
            RepeatingsListViewInner(
                state: state,
            )
        }
    }
}

private struct RepeatingsListViewInner: View {
    
    let state: RepeatingsListVm.State
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    @Environment(\.defaultMinListRowHeight) private var minListRowHeight

    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            
            VStack {
                
                let repeatingsUi = state.repeatingsUi.reversed()
                
                ForEach(repeatingsUi, id: \.repeatingDb.id) { repeatingUi in
                    RepeatingsListItemView(
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
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle("Repeating Tasks")
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Close") {
                    dismiss()
                }
            }
        }
    }
}
