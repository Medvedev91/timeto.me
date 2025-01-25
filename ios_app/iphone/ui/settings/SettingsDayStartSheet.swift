import SwiftUI
import shared

struct SettingsDayStartSheet: View {
    
    let vm: SettingsVm
    let state: SettingsVm.State
    
    @State var dayStart: Int32
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        VStack {
            
            Picker("", selection: $dayStart) {
                ForEach(state.dayStartListItems, id: \.seconds) { item in
                    Text(item.note)
                        .tag(item.seconds)
                }
            }
            .pickerStyle(.wheel)
        }
        .presentationDetents([.medium])
        .interactiveDismissDisabled()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle("Day Start")
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button("Save") {
                    vm.setDayStartOffsetSeconds(seconds: dayStart)
                    dismiss()
                }
                .fontWeight(.bold)
            }
        }
    }
}
