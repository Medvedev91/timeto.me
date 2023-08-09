import SwiftUI
import shared

struct ChecklistsPickerSheet: View {

    @Binding private var isPresented: Bool
    private let onPick: ([ChecklistModel]) -> Void

    @State private var vm: ChecklistsPickerSheetVM
    @State private var sheetHeaderScroll = 0

    init(
            isPresented: Binding<Bool>,
            selectedChecklists: [ChecklistModel],
            onPick: @escaping ([ChecklistModel]) -> Void
    ) {
        self.onPick = onPick
        _isPresented = isPresented
        _vm = State(initialValue: ChecklistsPickerSheetVM(selectedChecklists: selectedChecklists))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            SheetHeaderView(
                    onCancel: { isPresented = false },
                    title: state.headerTitle,
                    doneText: state.doneTitle,
                    isDoneEnabled: true,
                    scrollToHeader: sheetHeaderScroll
            ) {
                onPick(vm.getSelectedChecklists())
                isPresented = false
            }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack(spacing: 0) {

                    let checklistsUI = state.checklistsUI
                    ForEach(checklistsUI, id: \.checklist.id) { checklistUI in

                        let isFirst = checklistsUI.first == checklistUI

                        MyListView__ItemView(
                                isFirst: isFirst,
                                isLast: checklistsUI.last == checklistUI,
                                withTopDivider: !isFirst
                        ) {

                            MyListView__ItemView__CheckboxView(
                                    text: checklistUI.text,
                                    isChecked: checklistUI.isSelected
                            ) {
                                vm.toggleChecklist(checklistUI: checklistUI)
                            }
                        }
                    }
                }
                        .padding(.top, 20)
            }
        }
                .background(c.sheetBg)
    }
}
