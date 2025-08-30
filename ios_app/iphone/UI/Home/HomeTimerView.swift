import SwiftUI
import shared

private let timerFont1 = buildTimerFont(size: 44)
private let timerFont2 = buildTimerFont(size: 38)
private let timerFont3 = buildTimerFont(size: 30)

struct HomeTimerView: View {
    
    let vm: HomeVm
    let state: HomeVm.State
    
    ///
    
    @Environment(Navigation.self) private var navigation

    private var controlsColor: Color {
        state.timerStateUi.controlsColorEnum?.toColor() ?? HomeScreen__secondaryColor
    }
    
    private var noteColor: Color { state.timerStateUi.noteColor.toColor() }
    private var timerColor: Color { state.timerStateUi.timerColor.toColor() }
    
    var body: some View {
        
        let timerStateUi = state.timerStateUi
        
        let timerFont: Font = {
            let len = timerStateUi.timerText.count
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
                text: state.timerStateUi.note,
                color: noteColor
            )
            
            HStack {
                
                VStack {
                    
                    TimerDataNoteText(text: " ", color: .clear)
                    
                    Button(
                        action: {
                            vm.toggleIsPurple()
                        },
                        label: {
                            
                            ZStack {
                                
                                TimerDataTimerText(
                                    text: " ",
                                    font: timerFont,
                                    color: .clear
                                )
                                
                                Image(systemName: "info")
                                    .foregroundColor(controlsColor)
                                    .font(.system(size: 23, weight: .thin))
                                    .frame(maxWidth: .infinity)
                            }
                        }
                    )
                }
                
                Button(
                    action: {
                        timerStateUi.togglePomodoro()
                    },
                    label: {
                        
                        VStack {
                            
                            TimerDataNoteText(text: " ", color: .clear)
                            
                            TimerDataTimerText(
                                text: timerStateUi.timerText,
                                font: timerFont,
                                color: timerColor
                            )
                        }
                    }
                )
                
                VStack {
                    
                    TimerDataNoteText(text: " ", color: .clear)
                    
                    Button(
                        action: {
                            timerStateUi.prolong()
                        },
                        label: {
                            
                            ZStack {
                                
                                TimerDataTimerText(
                                    text: " ",
                                    font: timerFont,
                                    color: .clear
                                )
                                
                                if let prolongText = timerStateUi.prolongText {
                                    Text(prolongText)
                                        .font(.system(size: 22, weight: .thin))
                                        .foregroundColor(controlsColor)
                                } else {
                                    Image(systemName: "plus")
                                        .foregroundColor(controlsColor)
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
            
            let infoUi = timerStateUi.infoUi
            
            HStack {
                
                TimerInfoButton(
                    text: infoUi.untilDaytimeUi.text,
                    color: timerColor,
                    onClick: {
                        navigation.sheet {
                            DaytimePickerSheet(
                                title: infoUi.untilPickerTitle,
                                doneText: "Start",
                                daytimeUi: infoUi.untilDaytimeUi,
                                onDone: { daytimePickerUi in
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
                        navigation.showActivityTimerSheet(
                            activityDb: state.activeActivityDb,
                            strategy: timerStateUi.infoUi.timerStrategy,
                            hideOnStart: true
                        )
                    }
                )
                
                TimerInfoButton(
                    text: "?",
                    color: timerColor,
                    onClick: {
                        navigation.fullScreen {
                            ReadmeFullScreen(defaultItem: .pomodoro)
                        }
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
