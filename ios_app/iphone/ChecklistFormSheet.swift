import SwiftUI
import shared

struct ChecklistFormSheet: View {

    @State private var vm: ChecklistFormSheetVM
    private let onDelete: () -> Void

    @EnvironmentObject private var nativeSheet: NativeSheet

    init(
        checklistDb: ChecklistDb,
        onDelete: @escaping () -> Void
    ) {
        self._vm = State(initialValue: ChecklistFormSheetVM(checklistDb: checklistDb))
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
                                checklist: state.checklistDb
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

                Image(systemName: "trash")
                    .foregroundColor(c.red)
                    .font(.system(size: 19, weight: .regular))
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
                        }

                        Spacer()

                        HStack(spacing: 8) {

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
                                        .font(.system(size: 14))
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
                                        .font(.system(size: 14))
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
            }
        }
        .background(c.sheetBg)
    }
}
