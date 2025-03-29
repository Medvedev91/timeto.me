import SwiftUI
import shared

struct ActivityFormPomodoroSheet: View {
    
    let vm: ActivityFormVm
    let state: ActivityFormVm.State
    
    @State var seconds: Int32
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        VStack {
            
            Picker("", selection: $seconds) {
                ForEach(state.pomodoroListItemsData, id: \.timer) { item in
                    Text(item.text)
                        .tag(item.timer)
                }
            }
            .pickerStyle(.wheel)
        }
        .presentationDetents([.height(300)])
        .interactiveDismissDisabled()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.pomodoroTitle)
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    vm.setPomodoroTimer(newPomodoroTimer: seconds)
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
