import SwiftUI
import shared

struct TextFeaturesTriggersFormView: View {

    @State private var vm: TextFeaturesFormVM

    private let onChange: (TextFeatures) -> Void

    @State private var isChecklistsPickerPresented = false
    @State private var isShortcutsPickerPresented = false

    private let _textFeaturesState: TextFeatures

    init(
            textFeatures: TextFeatures,
            onChange: @escaping (TextFeatures) -> Void
    ) {
        _textFeaturesState = textFeatures
        self.onChange = onChange
        _vm = State(initialValue: TextFeaturesFormVM(initTextFeatures: textFeatures))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            EmptyView()
                    .onChange(of: _textFeaturesState) { newTextFeatures in
                        vm = TextFeaturesFormVM(initTextFeatures: newTextFeatures)
                    }

            MyListView__ItemView(
                    isFirst: true,
                    isLast: false
            ) {

                MyListView__ItemView__ButtonView(
                        text: state.titleChecklist,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: state.noteChecklists,
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
                                selectedChecklists: state.textFeatures.checklists
                        ) { checklists in
                            onChange(vm.upChecklists(checklists: checklists))
                        }
                    }

            MyListView__ItemView(
                    isFirst: false,
                    isLast: true,
                    withTopDivider: true
            ) {

                MyListView__ItemView__ButtonView(
                        text: state.titleShortcuts,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: state.noteShortcuts,
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
                                selectedShortcuts: state.textFeatures.shortcuts
                        ) { shortcut in
                            onChange(vm.upShortcuts(shortcuts: shortcut))
                        }
                    }
        }
    }
}
