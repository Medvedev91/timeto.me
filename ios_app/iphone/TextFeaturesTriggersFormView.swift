import SwiftUI
import shared

struct TextFeaturesTriggersFormView: View {

    private let formUI: TextFeaturesTriggersFormUi
    private let onChange: (TextFeatures) -> Void

    private let bgColor: Color

    @State private var isChecklistsPickerPresented = false
    @State private var isShortcutsPickerPresented = false

    init(
        textFeatures: TextFeatures,
        bgColor: Color = c.sheetFg,
        onChange: @escaping (TextFeatures) -> Void
    ) {
        self.bgColor = bgColor
        self.onChange = onChange
        formUI = TextFeaturesTriggersFormUi(textFeatures: textFeatures)
    }

    var body: some View {

        VStack {

            MyListView__ItemView(
                isFirst: true,
                isLast: false,
                bgColor: bgColor
            ) {

                MyListView__Item__Button(
                    text: formUI.checklistsTitle,
                    rightView: {
                        MyListView__Item__Button__RightText(
                            text: formUI.checklistsNote
                        )
                    }
                ) {
                    isChecklistsPickerPresented = true
                }
            }
            .sheetEnv(isPresented: $isChecklistsPickerPresented) {
                ChecklistPickerSheet(
                    selectedChecklistsDb: formUI.textFeatures.checklists
                ) { checklists in
                    onChange(formUI.setChecklists(checklists: checklists))
                }
            }

            MyListView__ItemView(
                isFirst: false,
                isLast: true,
                bgColor: bgColor,
                withTopDivider: true
            ) {

                MyListView__Item__Button(
                    text: formUI.shortcutsTitle,
                    rightView: {
                        MyListView__Item__Button__RightText(
                            text: formUI.shortcutsNote
                        )
                    }
                ) {
                    isShortcutsPickerPresented = true
                }
            }
            .sheetEnv(isPresented: $isShortcutsPickerPresented) {
                ShortcutsPickerSheet(
                    isPresented: $isShortcutsPickerPresented,
                    selectedShortcuts: formUI.textFeatures.shortcuts
                ) { shortcut in
                    onChange(formUI.setShortcuts(shortcuts: shortcut))
                }
            }
        }
    }
}
