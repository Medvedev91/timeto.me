import SwiftUI
import shared

struct PrivacyScreen: View {
    
    let titleDisplayMode: ToolbarTitleDisplayMode
    let scrollBottomMargin: CGFloat
    
    var body: some View {
        VmView({
            PrivacySheetVm()
        }) { vm, state in
            PrivacyScreenInner(
                vm: vm,
                state: state,
                isSendReportsEnabled: state.isSendReportsEnabled
            )
            .toolbarTitleDisplayMode(titleDisplayMode)
            .contentMargins(.bottom, scrollBottomMargin)
        }
    }
}

private struct PrivacyScreenInner: View {
    
    let vm: PrivacySheetVm
    let state: PrivacySheetVm.State
    
    @State var isSendReportsEnabled: Bool
    
    ///
    
    var body: some View {
        
        List {
            
            ForEach(state.textsUi, id: \.self) { textUi in
                
                Text(textUi.text)
                    .customListItem()
                    .fontWeight(textUi.isBold ? .bold : .regular)
                    .foregroundColor(.primary)
                    .padding(.top, 16)
                    .padding(.horizontal, H_PADDING)
                    .lineSpacing(3.2)
                    .textAlign(.leading)
            }
            
            Toggle(
                state.sendReportsTitle,
                isOn: $isSendReportsEnabled
            )
            .customListItem()
            .animateVmValue(value: state.isSendReportsEnabled, state: $isSendReportsEnabled)
            .onChange(of: isSendReportsEnabled) { _, new in
                vm.setIsSendingReports(isOn: new)
            }
            .padding(.vertical, 8)
            .padding(.horizontal, 12)
            .background(
                RoundedRectangle(
                    cornerRadius: 12,
                    style: .continuous
                )
                .fill(Color(.secondarySystemBackground))
            )
            .padding(.top, 20)
            .padding(.horizontal, H_PADDING - 4)
            
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
            .customListItem()
            .padding(.top, 16)
            .padding(.leading, H_PADDING)
            
            Padding(vertical: 20)
                .customListItem()
        }
        .customList()
        .navigationTitle(state.title)
    }
}
