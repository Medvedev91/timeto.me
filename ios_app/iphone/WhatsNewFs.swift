import SwiftUI
import shared

private let fontSize = 17.0

struct WhatsNewFs: View {

    @Binding var isPresented: Bool

    @State private var vm = WhatsNewVm()
    @State private var scroll = 0

    var body: some View {


        VMView(vm: vm, stack: .VStack()) { state in

            Sheet__HeaderView(
                title: state.headerTitle,
                scrollToHeader: scroll,
                bgColor: c.sheetBg
            )

            ScrollViewWithVListener(showsIndicators: false, vScroll: $scroll) {

                VStack {

                    ForEachIndexed(state.historyItemsUi) { _, historyItemUi in

                        VStack {

                            HStack {

                                Text(historyItemUi.title)
                                    .foregroundColor(c.text)
                                    .font(.system(size: fontSize, weight: .bold))

                                Spacer()

                                Text(historyItemUi.timeAgoText)
                                    .foregroundColor(c.text)
                                    .font(.system(size: fontSize))
                            }

                            HStack {

                                Text(historyItemUi.text)
                                    .foregroundColor(c.text)
                                    .font(.system(size: fontSize))
                                    .padding(.top, 8)

                                Spacer()
                            }

                            if state.historyItemsUi.last != historyItemUi {
                                SheetDividerBg().padding(.top, 16)
                            }
                        }
                        .padding(.top, 16)
                        .padding(.horizontal, H_PADDING)
                    }
                }
                .padding(.bottom, 16)
            }

            Sheet__BottomViewClose {
                isPresented = false
            }
        }
    }
}
