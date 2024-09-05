import SwiftUI
import shared

private let buttonFont: Font = .system(size: 15, weight: .light)

struct EventTemplatesView: View {

    let onPick: (EventTemplatesVm.TemplateUI) -> Void

    @State private var vm = EventTemplatesVm()

    @EnvironmentObject private var nativeSheet: NativeSheet

    var body: some View {

        VMView(vm: vm, stack: .ZStack()) { state in

            ScrollView(.horizontal, showsIndicators: false) {

                HStack {

                    MySpacerSize(width: H_PADDING_HALF)

                    ForEach(state.templatesUI, id: \.templateDB.id) { templateUI in

                        Button(
                            action: {
                                // In onTapGesture()/onLongPressGesture()
                            },
                            label: {
                                ListButton(
                                    text: templateUI.text
                                )
                                    /// Ordering is important
                                .onTapGesture {
                                    onPick(templateUI)
                                }
                                .onLongPressGesture(minimumDuration: 0.1) {
                                    nativeSheet.show { isPresented in
                                        EventTemplateFormSheet(
                                            isPresented: isPresented,
                                            eventTemplateDb: templateUI.templateDB
                                        )
                                    }
                                }
                                //////
                            }
                        )
                    }

                    Button(
                        action: {
                            nativeSheet.show { isPresented in
                                EventTemplateFormSheet(
                                    isPresented: isPresented,
                                    eventTemplateDb: nil
                                )
                            }
                        },
                        label: {
                            ListButton(
                                text: state.newTemplateText
                            )
                        }
                    )

                    MySpacerSize(width: H_PADDING_HALF)
                }
            }
        }
    }
}

private struct ListButton: View {

    let text: String

    var body: some View {
        Text(text)
            .padding(.vertical, 4)
            .padding(.horizontal, H_PADDING_HALF)
            .foregroundColor(.blue)
            .font(buttonFont)
    }
}
