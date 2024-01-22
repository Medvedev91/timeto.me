import SwiftUI
import shared

struct TimerHintsView: View {

    let timerHintsUI: [ActivityDb__Data.TimerHintsTimerHintUI]
    let hintHPadding: CGFloat
    let fontSize: CGFloat
    let fontWeight: Font.Weight
    var fontColor: Color = .blue
    let onStart: () -> Void

    var body: some View {

        ForEach(timerHintsUI, id: \.seconds) { hintUI in
            Button(
                    action: {
                        hintUI.startInterval {
                            onStart()
                        }
                    },
                    label: {
                        Text(hintUI.text)
                                .font(.system(size: fontSize, weight: fontWeight))
                                .foregroundColor(fontColor)
                                .padding(.horizontal, hintHPadding)
                                .padding(.vertical, 4)
                                .cornerRadius(99)
                    }
            )
                    .buttonStyle(.borderless)
        }
    }
}
