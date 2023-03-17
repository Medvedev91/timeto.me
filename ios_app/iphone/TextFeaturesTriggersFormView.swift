import SwiftUI
import shared

struct TextFeaturesTriggersFormView: View {

    private let formUI: TextFeaturesFormUI
    private let onChange: (TextFeatures) -> Void

    @State private var isChecklistsPickerPresented = false
    @State private var isShortcutsPickerPresented = false

    init(
            textFeatures: TextFeatures,
            onChange: @escaping (TextFeatures) -> Void
    ) {
        self.onChange = onChange
        formUI = TextFeaturesFormUI(textFeatures: textFeatures)
    }

    var body: some View {

        VStack(spacing: 0) {

            MyListView__ItemView(
                    isFirst: true,
                    isLast: false
            ) {

                MyListView__ItemView__ButtonView(
                        text: formUI.checklistsTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: formUI.checklistsNote,
                                        paddingEnd: 2
                                )
                        )
                ) {
                    isChecklistsPickerPresented = true
                }
            }
                    .sheetEnv(isPresented: $isChecklistsPickerPresented) {
                        ChecklistsPickerSheet(
                                isPresented: $isChecklistsPickerPresented,
                                selectedChecklists: formUI.textFeatures.checklists
                        ) { checklists in
                            onChange(formUI.upChecklists(checklists: checklists))
                        }
                    }

            MyListView__ItemView(
                    isFirst: false,
                    isLast: true,
                    withTopDivider: true
            ) {

                MyListView__ItemView__ButtonView(
                        text: formUI.shortcutsTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: formUI.shortcutsNote,
                                        paddingEnd: 2
                                )
                        )
                ) {
                    isShortcutsPickerPresented = true
                }
            }
                    .sheetEnv(isPresented: $isShortcutsPickerPresented) {
                        ShortcutsPickerSheet(
                                isPresented: $isShortcutsPickerPresented,
                                selectedShortcuts: formUI.textFeatures.shortcuts
                        ) { shortcut in
                            onChange(formUI.upShortcuts(shortcuts: shortcut))
                        }
                    }
        }
    }
}
