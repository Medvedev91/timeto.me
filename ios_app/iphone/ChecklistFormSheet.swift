import SwiftUI
import shared

struct ChecklistFormSheet: View {

    @State private var vm: ChecklistFormSheetVM
    private let onDelete: () -> Void

    init(
        checklistDb: ChecklistDb,
        onDelete: @escaping () -> Void
    ) {
        self._vm = State(initialValue: ChecklistFormSheetVM(checklistDb: checklistDb))
        self.onDelete = onDelete
    }

    var body: some View {

        VMView(vm: vm) { state in
            Text("ttdd")
        }
    }
}
