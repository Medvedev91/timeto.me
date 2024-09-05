import SwiftUI
import shared

struct ChecklistFormSheet: View {

    @State private var vm: ChecklistFormSheetVm
    @Binding private var isPresented: Bool
    private let onDelete: () -> Void

    @EnvironmentObject private var nativeSheet: NativeSheet

    init(
        checklistDb: ChecklistDb,
        isPresented: Binding<Bool>,
        onDelete: @escaping () -> Void
    ) {
        _vm = State(initialValue: ChecklistFormSheetVm(checklistDb: checklistDb))
        _isPresented = isPresented
        self.onDelete = onDelete
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            HStack {

                Text(state.checklistName)
                    .padding(.trailing, 8)
                    .font(.system(size: 24, weight: .bold))

                Button(
                    action: {
                        nativeSheet.show { isPresented in
                            ChecklistNameDialog(
                                isPresented: isPresented,
                                checklist: state.checklistDb,
                                onSave: { _ in }
                            )
                        }
                    },
                    label: {
                        Image(systemName: "pencil")
                            .font(.system(size: 20, weight: .regular))
                            .foregroundColor(c.white)
                    }
                )
                .offset(y: 1)

                Spacer()

                Button(
                    action: {
                        vm.deleteChecklist(
                            onDelete: {
                                onDelete()
                                isPresented = false
                            }
                        )
                    },
                    label: {
                        Image(systemName: "trash")
                            .foregroundColor(c.red)
                            .font(.system(size: 19, weight: .regular))
                    }
                )
            }
            .padding(.top, 24)
            .padding(.horizontal, H_PADDING)

            DividerBg()
                .padding(.horizontal, H_PADDING)
                .padding(.top, 16)

            ScrollView {

                Padding(vertical: 8)

                ForEach(state.checklistItemsUi, id: \.checklistItemDb.id) { checklistItemUi in

                    VStack {

                        if !checklistItemUi.isFirst {
                            DividerFg()
                                .padding(.leading, 27)
                        }

                        Spacer()

                        HStack(spacing: 8) {

                            Button(
                                action: {
                                    vm.deleteItem(itemDb: checklistItemUi.checklistItemDb)
                                },
                                label: {
                                    Image(systemName: "minus.circle.fill")
                                        .font(.system(size: 16))
                                        .foregroundColor(.red)
                                }
                            )
                            .buttonStyle(.plain)

                            Text(checklistItemUi.checklistItemDb.text)
                                .lineLimit(1)

                            Spacer()

                            Button(
                                action: {
                                    nativeSheet.show { isPresented in
                                        ChecklistItemFormSheet(
                                            isPresented: isPresented,
                                            checklist: state.checklistDb,
                                            checklistItem: checklistItemUi.checklistItemDb
                                        )
                                    }
                                },
                                label: {
                                    Image(systemName: "pencil")
                                        .font(.system(size: 16))
                                        .foregroundColor(.blue)
                                }
                            )
                            .buttonStyle(.plain)
                            .padding(.leading, 8)

                            Button(
                                action: {
                                    vm.down(itemUi: checklistItemUi)
                                },
                                label: {
                                    Image(systemName: "arrow.down")
                                        .font(.system(size: 15))
                                        .foregroundColor(.blue)
                                }
                            )
                            .buttonStyle(.plain)
                            .padding(.leading, 8)

                            Button(
                                action: {
                                    vm.up(itemUi: checklistItemUi)
                                },
                                label: {
                                    Image(systemName: "arrow.up")
                                        .font(.system(size: 15))
                                        .foregroundColor(.blue)
                                }
                            )
                            .buttonStyle(.plain)
                            .padding(.leading, 6)
                        }
                        .padding(.vertical, 12)

                        Spacer()
                    }
                    .padding(.horizontal, H_PADDING)
                }

                HStack {

                    Button(
                        action: {
                            nativeSheet.show { isPresented in
                                ChecklistItemFormSheet(
                                    isPresented: isPresented,
                                    checklist: state.checklistDb,
                                    checklistItem: nil
                                )
                            }
                        },
                        label: {
                            Text(state.newItemButton)
                                .foregroundColor(c.blue)
                                .padding(.leading, H_PADDING)
                        }
                    )

                    Spacer()
                }
                .padding(.top, 12)
            }


            Sheet__BottomViewDone(text: "Done") {
                if vm.isDoneAllowed() {
                    isPresented = false
                }
            }
        }
        .background(c.sheetBg)
    }
}
