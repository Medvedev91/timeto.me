import SwiftUI
import shared

struct PrivacyScreen: View {
    
    let toForceChoice: Bool
    let titleDisplayMode: ToolbarTitleDisplayMode
    let scrollBottomMargin: CGFloat
    
    var body: some View {
        VmView({
            PrivacyVm()
        }) { vm, state in
            PrivacyScreenInner(
                vm: vm,
                state: state,
                toForceChoice: toForceChoice,
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
    
    let toForceChoice: Bool
    @State var isSendingReportsEnabled: Bool
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
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
            .padding(.vertical, 12)
            .padding(.horizontal, 12)
            .background(
                RoundedRectangle(
                    cornerRadius: 12,
                    style: .continuous,
                )
                .fill(Color(.secondarySystemBackground))
            )
            .padding(.top, 20)
            .padding(.horizontal, H_PADDING - 4)
            
            if toForceChoice && !isSendingReportsEnabled {
                BottomButton(
                    text: "Keep Turned Off",
                    color: .secondary,
                    onTap: {
                        vm.setIsSendingReports(isEnabled: false)
                        dismiss()
                    },
                )
            }
            
            BottomButton(
                text: "Open Source",
                color: .blue,
                onTap: {
                    showOpenSource()
                },
            )
            
            Padding(vertical: 20)
                .customListItem()
        }
        .customList()
        .navigationTitle(state.title)
        .toolbar {
            if toForceChoice && isSendingReportsEnabled {
                ToolbarItem(placement: .primaryAction) {
                    Button("Done") {
                        dismiss()
                    }
                    .fontWeight(.semibold)
                    .tint(.blue)
                }
            }
        }
    }
}

private struct BottomButton: View {
    
    let text: String
    let color: Color
    let onTap: () -> Void
    
    var body: some View {
        Button(
            action: {
                onTap()
            },
            label: {
                Text(text)
                    .foregroundColor(color)
                    .textAlign(.leading)
            }
        )
        .customListItem()
        .padding(.top, 16)
        .padding(.leading, H_PADDING)
    }
}
