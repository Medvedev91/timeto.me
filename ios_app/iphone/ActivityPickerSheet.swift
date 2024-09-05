import SwiftUI
import shared

struct ActivityPickerSheet: View {

    @Binding var isPresented: Bool
    let onPick: (ActivityDb) -> Void

    @State private var vm = ActivityPickerSheetVm()
    @State private var sheetHeaderScroll = 0

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: nil,
                    isDoneEnabled: false,
                    scrollToHeader: sheetHeaderScroll,
                    cancelText: "Back"
            ) {}

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack(spacing: 0) {

                    let activitiesUI = state.activitiesUI
                    ForEach(activitiesUI, id: \.activity.id) { activityUI in

                        let isFirst = activitiesUI.first == activityUI

                        MyListView__ItemView(
                                isFirst: isFirst,
                                isLast: activitiesUI.last == activityUI,
                                withTopDivider: !isFirst
                        ) {

                            MyListView__ItemView__ButtonView(
                                    text: activityUI.text
                            ) {
                                onPick(activityUI.activity)
                                isPresented = false
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
