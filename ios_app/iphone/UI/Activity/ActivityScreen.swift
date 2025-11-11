import SwiftUI
import shared

struct ActivityScreen: View {
    
    @Binding var tab: MainTabEnum
    
    ///
    
    @State private var isListOrSummary = true
    
    var body: some View {
        VmView({
            SummaryVm()
        }) { summaryVm, summaryState in
            
            ZStack(alignment: .bottom) {
                
                HistoryScreen(
                    tab: $tab
                )
                
                if !isListOrSummary {
                    SummarySheet(
                        vm: summaryVm,
                        state: summaryState,
                    )
                    .background(.black)
                }
                
                MenuView(
                    summaryVm: summaryVm,
                    summaryState: summaryState,
                    isListOrSummary: $isListOrSummary,
                )
            }
        }
        .padding(.bottom, MainTabsView__HEIGHT)
    }
}

///

private struct MenuView: View {
    
    let summaryVm: SummaryVm
    let summaryState: SummaryVm.State
    
    @Binding var isListOrSummary: Bool
    
    var body: some View {
        HStack {
            MenuButton(text: "List", isSelected: isListOrSummary) {
                isListOrSummary = true
                let firstPeriodHintUi = summaryState.periodHints.first!
                summaryVm.setPeriod(
                    pickerTimeStart: firstPeriodHintUi.pickerTimeStart,
                    pickerTimeFinish: firstPeriodHintUi.pickerTimeFinish,
                )
            }
            MenuSeparator()
            ForEach(summaryState.periodHints, id: \.title) { periodHintUi in
                MenuButton(
                    text: periodHintUi.title,
                    isSelected: !isListOrSummary && periodHintUi.isActive,
                ) {
                    isListOrSummary = false
                    summaryVm.setPeriod(
                        pickerTimeStart: periodHintUi.pickerTimeStart,
                        pickerTimeFinish: periodHintUi.pickerTimeFinish,
                    )
                }
            }
            MenuSeparator()
            MenuButton(text: summaryState.dateTitle, isSelected: !isListOrSummary && false) {
                isListOrSummary = false
            }
        }
        .padding(.horizontal, 4)
        .padding(.vertical, 8)
        .background(squircleShape.fill(.black.opacity(0.9)))
        .padding(.bottom, 12)
    }
}

private struct MenuButton: View {
    
    let text: String
    let isSelected: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(
            action: {
                onTap()
            },
            label: {
                Text(text)
                    .lineLimit(1)
                    .padding(.horizontal, 6)
                    .foregroundColor(isSelected ? .primary : .secondary)
                    .font(.system(size: 15, weight: isSelected ? .semibold : .regular))
                    .minimumScaleFactor(0.2)
            }
        )
    }
}

private struct MenuSeparator: View {
    
    var body: some View {
        Divider()
            .background(.white.opacity(0.9))
            .frame(height: 16)
            .padding(.horizontal, 6)
    }
}
