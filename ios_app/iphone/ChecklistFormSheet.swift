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
                .offset(y: 2)

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

            Spacer()
        }
        .background(c.sheetBg)
    }
}
