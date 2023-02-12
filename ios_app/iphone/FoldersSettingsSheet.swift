import SwiftUI
import shared

struct FoldersSettingsSheet: View {

    @Binding var isPresented: Bool

    @State private var vm = FoldersSettingsVM()
    @State private var sheetHeaderScroll = 0

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: state.headerDoneText,
                    isDoneEnabled: true,
                    scrollToHeader: sheetHeaderScroll
            ) {
                isPresented = false
            }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

            }
        }
                .background(Color(.mySheetFormBg))
    }
}
