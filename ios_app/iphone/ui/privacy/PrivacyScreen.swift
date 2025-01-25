import SwiftUI
import shared

struct PrivacyScreen: View {
    
    let titleDisplayMode: ToolbarTitleDisplayMode
    
    var body: some View {
        VmView({
            PrivacySheetVm()
        }) { vm, state in
            PrivacyScreenInner(
                vm: vm,
                state: state
            )
            .toolbarTitleDisplayMode(titleDisplayMode)
        }
    }
}

private struct PrivacyScreenInner: View {
    
    let vm: PrivacySheetVm
    let state: PrivacySheetVm.State
    
    ///
    
    var body: some View {
        
        List {
            
            ForEach(state.textsUi, id: \.self) { textUi in
                
                Text(textUi.text)
                    .fontWeight(textUi.isBold ? .bold : .regular)
                    .foregroundColor(.primary)
                    .padding(.top, 16)
                    .padding(.horizontal, H_PADDING)
                    .lineSpacing(3.2)
                    .textAlign(.leading)
                    .customListItem()
            }
            
            Button(
                action: {
                    showOpenSource()
                },
                label: {
                    Text("Open Source")
                        .foregroundColor(.blue)
                        .textAlign(.leading)
                }
            )
            .padding(.top, 16)
            .padding(.leading, H_PADDING)
            .customListItem()
        }
        .customList()
        .navigationTitle(state.title)
    }
}
