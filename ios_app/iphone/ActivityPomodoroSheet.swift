import SwiftUI
import shared

struct ActivityPomodoroSheet: View {

    @Binding private var isPresented: Bool
    private let onPick: (Int) -> Void

    @State private var vm: ActivityPomodoroSheetVm
    @State private var sheetHeaderScroll = 0

    init(
        isPresented: Binding<Bool>,
        selectedTimer: Int,
        onPick: @escaping (Int) -> Void
    ) {
        self.onPick = onPick
        _isPresented = isPresented
        _vm = State(initialValue: ActivityPomodoroSheetVm(selectedTimer: selectedTimer.toInt32()))
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
                onPick(state.prepSelectedTime().toInt())
                isPresented = false
            }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack {

                    let listItemsUi = state.listItemsUi
                    ForEach(listItemsUi, id: \.time) { listItemUi in

                        let isFirst = listItemsUi.first == listItemUi

                        MyListView__ItemView(
                            isFirst: isFirst,
                            isLast: listItemsUi.last == listItemUi,
                            withTopDivider: !isFirst
                        ) {

                            MyListView__ItemView__RadioView(
                                text: listItemUi.text,
                                isActive: listItemUi.isSelected
                            ) {
                                vm.setTimer(time: listItemUi.time)
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
