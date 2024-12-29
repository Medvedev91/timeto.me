import SwiftUI
import shared

struct TextFeaturesTimerFormView: View {
    
    private let formUI: TextFeaturesTimerFormUi
    private let onChange: (TextFeatures) -> Void
    
    private let bgColor: Color
    
    @State private var isActivitySheetPresented = false
    @State private var isTimerSheetPresented = false
    
    init(
        textFeatures: TextFeatures,
        bgColor: Color = c.sheetFg,
        onChange: @escaping (TextFeatures) -> Void
    ) {
        self.bgColor = bgColor
        self.onChange = onChange
        formUI = TextFeaturesTimerFormUi(textFeatures: textFeatures)
    }
    
    var body: some View {
        
        VStack {
            
            MyListView__ItemView(
                isFirst: true,
                isLast: false,
                bgColor: bgColor
            ) {
                
                MyListView__Item__Button(
                    text: formUI.activityTitle,
                    rightView: {
                        MyListView__Item__Button__RightText(
                            text: formUI.activityNote,
                            color: formUI.activityColorOrNull?.toColor(),
                            paddingEndExtra: -1.0
                        )
                    }
                ) {
                    isActivitySheetPresented = true
                }
                .sheetEnv(isPresented: $isActivitySheetPresented) {
                    ActivityPickerSheet(
                        isPresented: $isActivitySheetPresented
                    ) { activity in
                        onChange(formUI.setActivity(activity: activity))
                    }
                }
            }
            
            MyListView__ItemView(
                isFirst: false,
                isLast: true,
                bgColor: bgColor,
                withTopDivider: true
            ) {
                
                MyListView__Item__Button(
                    text: formUI.timerTitle,
                    rightView: {
                        MyListView__Item__Button__RightText(
                            text: formUI.timerNote,
                            color: formUI.timerColorOrNull?.toColor()
                        )
                    }
                ) {
                    isTimerSheetPresented = true
                }
                .sheetEnv(isPresented: $isTimerSheetPresented) {
                    TimerPickerSheet(
                        isPresented: $isTimerSheetPresented,
                        title: "Timer",
                        doneText: "Done",
                        defMinutes: 30
                    ) { seconds in
                        onChange(formUI.setTimer(seconds: seconds.toInt32()))
                    }
                    .presentationDetentsMediumIf16()
                }
            }
        }
    }
}
