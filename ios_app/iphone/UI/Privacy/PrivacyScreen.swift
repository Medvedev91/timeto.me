import SwiftUI
import shared

struct PrivacyScreen: View {
    
    let titleDisplayMode: ToolbarTitleDisplayMode
    let scrollBottomMargin: CGFloat
    
    var body: some View {
        VmView({
            PrivacyVm()
        }) { vm, state in
            PrivacyScreenInner(
                vm: vm,
                state: state,
                isSendingReportsEnabled: state.isSendingReportsEnabled
            )
            .toolbarTitleDisplayMode(titleDisplayMode)
            .contentMargins(.bottom, scrollBottomMargin)
        }
    }
}

private struct PrivacyScreenInner: View {
    
    let vm: PrivacyVm
    let state: PrivacyVm.State
    
    @State var isSendingReportsEnabled: Bool
    
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
                isOn: $isSendingReportsEnabled
            )
            .customListItem()
            .animateVmValue(vmValue: state.isSendingReportsEnabled, swiftState: $isSendingReportsEnabled)
            .onChange(of: isSendingReportsEnabled) { _, newValue in
                vm.setIsSendingReports(isEnabled: newValue)
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
