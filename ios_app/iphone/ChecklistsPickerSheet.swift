import SwiftUI
import shared

struct ChecklistsPickerSheet: View {

    @Binding private var isPresented: Bool
    private let onPick: ([ChecklistDb]) -> Void

    @State private var vm: ChecklistsPickerSheetVm
    @State private var sheetHeaderScroll = 0

    @EnvironmentObject private var nativeSheet: NativeSheet

    init(
        isPresented: Binding<Bool>,
        selectedChecklists: [ChecklistDb],
        onPick: @escaping ([ChecklistDb]) -> Void
    ) {
        self.onPick = onPick
        _isPresented = isPresented
        _vm = State(initialValue: ChecklistsPickerSheetVm(selectedChecklists: selectedChecklists))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

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

                VStack {

                    let checklistsUI = state.checklistsUI
                    ForEach(checklistsUI, id: \.checklist.id) { checklistUI in

                        let isFirst = checklistsUI.first == checklistUI

                        MyListView__ItemView(
                            isFirst: isFirst,
                            isLast: checklistsUI.last == checklistUI,
                            withTopDivider: !isFirst
                        ) {

                            MyListView__ItemView__RadioView(
                                text: checklistUI.text,
                                isActive: checklistUI.isSelected
                            ) {
                                vm.toggleChecklist(checklistUI: checklistUI)
                            }
                        }
                    }

                    HStack {

                        Button(
                            action: {
                                nativeSheet.show { isPresentedNameDialog in
                                    ChecklistNameDialog(
                                        isPresented: isPresentedNameDialog,
                                        checklist: nil,
                                        onSave: { newChecklistDb in
                                            vm.selectById(id: newChecklistDb.id)
                                            nativeSheet.show { isPresentedForm in
                                                ChecklistFormSheet(
                                                    checklistDb: newChecklistDb,
                                                    onDelete: {}
                                                )
                                            }
                                        }
                                    )
                                }
                            },
                            label: {
                                Text(state.newChecklistButton)
                                    .foregroundColor(c.blue)
                                    .padding(.leading, H_PADDING + 6)
                                    .padding(.top, 16)
                            }
                        )

                        Spacer()
                    }
                }
                .padding(.top, 20)
            }
        }
        .background(c.sheetBg)
    }
}
