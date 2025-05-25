import SwiftUI
import shared

extension Navigation {
    
    func showActivitiesTimerSheet(
        strategy: ActivityTimerStrategy
    ) {
        self.sheet {
            VmView({
                ActivitiesTimerVm()
            }) { _, state in
                ActivitiesTimerSheetInner(
                    state: state,
                    strategy: strategy
                )
            }
        }
    }
}

private struct ActivitiesTimerSheetInner: View {
    
    let state: ActivitiesTimerVm.State
    let strategy: ActivityTimerStrategy
    
    ///

    @Environment(\.dismiss) private var dismiss

    private let sheetHeight: CGFloat =
    Cache.shared.activitiesDbSorted.count.toDouble() * ActivitiesView__listItemHeight
    
    var body: some View {
        ActivitiesView(
            timerStrategy: strategy,
            presentationMode: .sheet
        )
        .onChange(of: state.lastIntervalId) {
            dismiss()
        }
        .presentationDetents([.height(sheetHeight)])
        .presentationDragIndicator(.visible)
    }
}
