import SwiftUI
import shared

struct TextFeaturesTriggersFormView: View {

    @State private var vm: TextFeaturesFormVM

    private let onChange: (TextFeatures) -> Void

    @State private var isChecklistsPickerPresented = false

    init(
            textFeatures: TextFeatures,
            onChange: @escaping (TextFeatures) -> Void
    ) {
        self.onChange = onChange
        _vm = State(initialValue: TextFeaturesFormVM(initTextFeatures: textFeatures))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

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
        }
    }
}
