import SwiftUI
import shared

struct ChecklistDialog: View {

    @State private var vm: ChecklistDialogVM

    @Binding private var isPresented: Bool
    private var checklist: ChecklistModel

    @State private var isAddItemPresented = false

    init(
            isPresented: Binding<Bool>,
            checklist: ChecklistModel
    ) {
        _isPresented = isPresented
        self.checklist = checklist
        _vm = State(initialValue: ChecklistDialogVM(checklist: checklist))
    }

    var body: some View {

        VMView(vm: vm, stack: .ZStack(alignment: .top)) { state in

            let items = state.items

            ScrollView(showsIndicators: false) {

                VStack(spacing: 0) {

                    Text(checklist.name)
                            .font(.system(size: 28, weight: .bold))
                            .padding(.top, 70)
                            .padding(.bottom, 15)

                    ForEach(items, id: \.id) { item in
                        let isFirst = items.first == item
                        MyListView__ItemView(
                                isFirst: isFirst,
                                isLast: items.last == item,
                                withTopDivider: !isFirst
                        ) {
                            ChecklistView__ItemView(item: item)
                        }
                    }
                }
            }

            HStack {

                Button(
                        action: {
                            isPresented = false
                        },
                        label: { Text("Back") }
                )
                        .foregroundColor(.blue)
                        .padding(.leading, 25)

                Spacer()

                let isCheckedExists = !items.filter { item in item.isChecked() }.isEmpty
                if isCheckedExists {
                    Button(
                            action: {
                                items.forEach { item in
                                    if item.isChecked() {
                                        item.toggle { _ in
                                            // todo
                                        }
                                    }
                                }
                            },
                            label: { Text("Uncheck") }
                    )
                            .foregroundColor(.blue)
                            .padding(.trailing, 15)
                }

                Button(
                        action: {
                            isAddItemPresented = true
                        },
                        label: {
                            Image(systemName: "plus")
                                    .foregroundColor(.blue)
                                    .padding(.trailing, 30)
                        }
                )
            }
                    .padding(.top, 20)
        }
                .background(Color(.mySheetFormBg))
                .sheetEnv(isPresented: $isAddItemPresented) {
                    ChecklistItemFormSheet(isPresented: $isAddItemPresented, checklist: checklist, checklistItem: nil)
                }
    }
}

struct ChecklistView__ItemView: View {

    let item: ChecklistItemModel

    @State private var isEditPresented = false

    var body: some View {
        MyListSwipeToActionItem(
                deletionHint: item.text,
                deletionConfirmationNote: nil,
                onEdit: {
                    isEditPresented = true
                },
                onDelete: {
                    withAnimation {
                        item.delete { _ in
                            // todo
                        }
                    }
                }
        ) {
            Button(
                    action: {
                        item.toggle { _ in
                            // todo report
                        }
                    },
                    label: {
                        HStack {
                            Text(item.text)
                            Spacer()
                            if item.isChecked() {
                                Image(systemName: "checkmark")
                                        .foregroundColor(.blue)
                                        .offset(x: 4)
                            }
                        }
                                .frame(minHeight: MyListView.ITEM_MIN_HEIGHT)
                    }
            )
                    .foregroundColor(.primary)
                    .padding(.horizontal, DEF_LIST_H_PADDING)
        }
                .sheetEnv(isPresented: $isEditPresented) {
                    ChecklistItemFormSheet(
                            isPresented: $isEditPresented,
                            checklist: DI.checklists.filter { $0.id == item.list_id }.first!,
                            checklistItem: item
                    )
                }
    }
}
