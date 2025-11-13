import SwiftUI
import shared

struct SummaryCalendarFullScreen: View {
    
    var body: some View {
        VmView({
            SummaryCalendarVm()
        }) { vm, state in
            SummaryCalendarFullScreenInner(
                vm: vm,
                state: state,
            )
        }
    }
}

private struct SummaryCalendarFullScreenInner: View {
    
    let vm: SummaryCalendarVm
    let state: SummaryCalendarVm.State
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                
                Padding(vertical: 16)
                
                ForEach(state.weeksUi, id: \.id) { weekUi in
                    HStack {
                        ForEachIndexed(weekUi.daysUi) { idx, dayUi in
                            let dayUi = dayUi as! SummaryCalendarVm.DayUi?
                            if let dayUi = dayUi {
                                ZStack {
                                    if let subtitle = dayUi.subtitle {
                                        Text(subtitle)
                                            .font(.system(size: 12, weight: .semibold))
                                            .foregroundColor(.red)
                                            .offset(y: -20)
                                    }
                                    Text(dayUi.title)
                                        .frame(minWidth: 0, maxWidth: .infinity)
                                }
                            } else {
                                ZStack {
                                }
                                .frame(minWidth: 0, maxWidth: .infinity)
                            }
                        }
                    }
                    .frame(height: 48)
                }
                
                Padding(vertical: 16)
            }
        }
        .defaultScrollAnchor(.bottom)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
