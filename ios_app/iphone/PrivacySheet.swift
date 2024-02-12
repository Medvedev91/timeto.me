import SwiftUI
import shared

private let hPadding = MyListView.PADDING_OUTER_HORIZONTAL

struct PrivacySheet: View {

    @Binding var isPresented: Bool

    @State private var vm = PrivacySheetVM()
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

                    PView(text: state.text1, topPadding: 8)

                    PView(text: state.text2)

                    PView(text: state.text3)

                    PView(text: state.text4, fontWeight: .bold)

                    PView(text: state.text5)

                    VStack {
                        ForEachIndexed(state.sendItems) { _, sendItem in
                            PView(text: sendItem, topPadding: 4)
                        }
                    }
                            .padding(.top, 8)
                            .padding(.bottom, 12)
                            .background(c.sheetFg)
                            .padding(.top, 16)

                    PView(text: state.text6)

                    /*
                MyListView__ItemView(
                    isFirst = true,
                    isLast = true,
                    modifier = Modifier
                            .padding(top = 24.dp),
                    withTopDivider = false,
                    ) {
                    MyListView__ItemView__SwitchView(
                        text = state.sendReportsTitle,
                        isActive = state.isSendReportsEnabled,
                        ) {
                        vm.toggleIsSendingReports()
                    }
                }
            */

                    Button(
                        action: {
                            showOpenSource()
                        },
                        label: {
                            HStack {

                                Text("Open Source")
                                    .padding(.top, 16)
                                    .padding(.horizontal, MyListView.PADDING_OUTER_HORIZONTAL)
                                    .padding(.top, 8)
                                    .foregroundColor(c.blue)
                                    .font(.system(size: 15))

                                Spacer()
                            }
                        }
                    )
                }
                        .padding(.bottom, 20)
            }

            Sheet__BottomViewClose {
                isPresented = false
            }
        }
    }
}

private struct PView: View {

    let text: String
    var topPadding: CGFloat = 16
    var fontWeight: Font.Weight = .regular

    var body: some View {

        Text(text)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, hPadding)
                .padding(.top, topPadding)
                .foregroundColor(c.white)
                .font(.system(size: 17, weight: fontWeight))
                .multilineTextAlignment(.leading)
                .lineSpacing(2)
    }
}
