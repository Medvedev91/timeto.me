import SwiftUI
import shared

struct SummaryCalendarFullScreen: View {
    
    let selectedStartTime: UnixTime
    let selectedFinishTime: UnixTime
    
    var body: some View {
        VmView({
            SummaryCalendarVm(
                selectedStartTime: selectedStartTime,
                selectedFinishTime: selectedFinishTime,
            )
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
                    
                    Divider()
                    
                    HStack {
                        ForEachIndexed(weekUi.daysUi) { idx, dayUi in
                            Button(
                                action: {
                                },
                                label: {
                                    ZStack {
                                        let dayUi = dayUi as! SummaryCalendarVm.DayUi?
                                        if let dayUi = dayUi {
                                            if let subtitle = dayUi.subtitle {
                                                Text(subtitle)
                                                    .font(.system(size: 12, weight: .medium))
                                                    .foregroundColor(.red)
                                                    .offset(y: -20)
                                                    .zIndex(2)
                                            }
                                            let isSelected: Bool = state.selectedDays.contains(dayUi.unixDay.toKotlinInt())
                                            Text(dayUi.title)
                                                .foregroundColor(.primary)
                                                .frame(width: 32, height: 32)
                                                .font(.system(size: isSelected ? 14 : 16, weight: isSelected ? .semibold : .regular))
                                                .background(roundedShape.fill(isSelected ? .blue : .clear))
                                                .zIndex(1)
                                        } else {
                                        }
                                    }
                                }
                            )
                            .frame(minWidth: 0, maxWidth: .infinity)
                        }
                    }
                    .frame(height: 68)
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
