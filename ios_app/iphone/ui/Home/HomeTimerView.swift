import SwiftUI
import shared

private let timerFont1 = buildTimerFont(size: 44)
private let timerFont2 = buildTimerFont(size: 38)
private let timerFont3 = buildTimerFont(size: 30)

struct HomeTimerView: View {
    
    let vm: HomeVm
    let state: HomeVm.State
    
    ///
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    @EnvironmentObject private var navigation: Navigation

    var body: some View {
        
        let timerData = state.timerData
        let noteColor = timerData.noteColor.toColor()
        let timerColor = timerData.timerColor.toColor()
        let timerControlsColor = state.timerData.controlsColor.toColor()
        
        let timerFont: Font = {
            let len = timerData.timerText.count
            if len <= 5 {
                return timerFont1
            }
            if len <= 7 {
                return timerFont2
            }
            return timerFont3
        }()
        
        ZStack(alignment: .top) {
            
            TimerDataNoteText(
                text: state.timerData.note,
                color: noteColor
            )
            
            HStack {
                
                VStack {
                    
                    TimerDataNoteText(text: " ", color: c.transparent)
                    
                    Button(
                        action: {
                            vm.toggleIsPurple()
                        },
                        label: {
                            
                            ZStack {
                                
                                TimerDataTimerText(
                                    text: " ",
                                    font: timerFont,
                                    color: c.transparent
                                )
                                
                                Image(systemName: "info")
                                    .foregroundColor(timerControlsColor)
                                    .font(.system(size: 23, weight: .thin))
                                    .frame(maxWidth: .infinity)
                            }
                        }
                    )
                }
                
                Button(
                    action: {
                        state.timerData.togglePomodoro()
                    },
                    label: {
                        
                        VStack {
                            
                            TimerDataNoteText(text: " ", color: c.transparent)
                            
                            TimerDataTimerText(
                                text: timerData.timerText,
                                font: timerFont,
                                color: timerColor
                            )
                        }
                    }
                )
                
                VStack {
                    
                    TimerDataNoteText(text: " ", color: c.transparent)
                    
                    Button(
                        action: {
                            state.timerData.prolong()
                        },
                        label: {
                            
                            ZStack {
                                
                                TimerDataTimerText(
                                    text: " ",
                                    font: timerFont,
                                    color: c.transparent
                                )
                                
                                if let prolongText = timerData.prolongText {
                                    Text(prolongText)
                                        .font(.system(size: 22, weight: .thin))
                                        .foregroundColor(timerControlsColor)
                                } else {
                                    Image(systemName: "plus")
                                        .foregroundColor(timerControlsColor)
                                        .font(.system(size: 22, weight: .thin))
                                }
                            }
                            .frame(maxWidth: .infinity)
                        }
                    )
                }
            }
        }
        .padding(.bottom, 11)
        
        if state.isPurple {
            
            let infoUi = state.timerData.infoUi
            
            HStack {
                
                TimerInfoButton(
                    text: infoUi.untilDaytimeUi.text,
                    color: timerColor,
                    onClick: {
                        nativeSheet.show { isTimerPickerPresented in
                            DaytimePickerSheet(
                                isPresented: isTimerPickerPresented,
                                title: infoUi.untilPickerTitle,
                                doneText: "Start",
                                daytimeUi: infoUi.untilDaytimeUi,
                                onPick: { daytimePickerUi in
                                    infoUi.setUntilDaytime(daytimeUi: daytimePickerUi)
                                    vm.toggleIsPurple()
                                    
                                },
                                onRemove: {}
                            )
                            .presentationDetents([.medium])
                            .presentationDragIndicator(.visible)
                        }
                    }
                )
                
                TimerInfoButton(
                    text: infoUi.timerText,
                    color: timerColor,
                    onClick: {
                        nativeSheet.showActivityTimerSheet(
                            activity: state.activeActivityDb,
                            timerContext: state.timerData.infoUi.timerContext,
                            hideOnStart: true,
                            onStart: {}
                        )
                    }
                )
                
                TimerInfoButton(
                    text: "?",
                    color: timerColor,
                    onClick: {
                        navigation.push(.readme(defaultItem: .pomodoro))
                    }
                )
            }
            .offset(y: -4)
        }
    }
}

///

private struct TimerInfoButton: View {

    let text: String
    let color: Color
    let onClick: () -> Void

    var body: some View {

        Button(
            action: {
                onClick()
            },
            label: {
                Text(text)
                    .font(.system(size: 22, weight: .thin))
                    .foregroundColor(color)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .cornerRadius(99)
            }
        )
        .buttonStyle(.borderless)
    }
}


private struct TimerDataNoteText: View {

    let text: String
    let color: Color

    var body: some View {

        Text(text)
            .font(.system(size: 20, weight: .bold))
            .foregroundColor(color)
            .multilineTextAlignment(.center)
            .padding(.horizontal, H_PADDING)
            .padding(.bottom, 9)
            .lineLimit(1)
    }
}

private struct TimerDataTimerText: View {

    let text: String
    let font: Font
    let color: Color

    var body: some View {

        Text(text)
            .font(font)
            .foregroundColor(color)
            .lineLimit(1)
            .fixedSize()
    }
}
