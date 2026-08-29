import SwiftUI
import shared

struct WidgetFullView : View {
    
    let widgetUi: WidgetUi
    
    var body: some View {
        VStack {
            
            let timerStateUi: TimerStateUi = widgetUi.timerStateUi
            let widgetTimerData = WidgetTimerData.build(timerType: timerStateUi.timerType)
            
            Link(destination: WidgetLink.Toggle.buildUrl()) {
                ZStack(alignment: .top) {
                    
                    Text(timerStateUi.note)
                        .foregroundColor(timerStateUi.noteColor.toColor())
                        .font(.system(size: 16, weight: .semibold))
                    
                    Text(
                        timerInterval: widgetTimerData.date.widgetTimerRange(isTimerOrStopwatch: widgetTimerData.isTimerOrStopwatch),
                        countsDown: widgetTimerData.isTimerOrStopwatch,
                    )
                    .textAlign(.center)
                    .foregroundColor(timerStateUi.timerColor.toColor())
                    .font(.system(size: 40, weight: .bold))
                    .padding(.top, 16)
                }
            }
            .padding(.top, 8)
            
            HStack {
                if let widgetChecklistUi = widgetUi.widgetChecklistUi {
                    WidgetFullChecklistView(widgetChecklistUi: widgetChecklistUi)
                }
            }
            
            Spacer()
            
            WidgetFullButtonsView(
                widgetUi: widgetUi,
            )
            .padding(.bottom, 8)
        }
    }
}
