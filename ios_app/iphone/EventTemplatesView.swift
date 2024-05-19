import SwiftUI
import shared

private let buttonFont: Font = .system(size: 15, weight: .light)
private let buttonHPadding = 2.0
private let buttonVPadding = 4.0

struct EventTemplatesView: View {

    let spaceAround: Double
    let paddingTop: Double

    @State private var vm = EventTemplatesVM()

    @EnvironmentObject private var nativeSheet: NativeSheet

    var body: some View {

        VMView(vm: vm, stack: .ZStack()) { state in

            ScrollView(.horizontal, showsIndicators: false) {

                HStack {

                    MySpacerSize(width: spaceAround)

                    ForEach(state.templatesUI, id: \.templateDB.id) { templateUI in

                        Button(
                            action: {
                                // In onTapGesture()/onLongPressGesture()
                            },
                            label: {
                                Text(templateUI.text)
                                .padding(.vertical, buttonVPadding)
                                .padding(.horizontal, buttonHPadding)
                                /// Ordering is important
                                .onTapGesture {
                                    nativeSheet.EventFormSheet__show(
                                        editedEvent: nil,
                                        defText: templateUI.templateDB.text,
                                        defTime: templateUI.timeForEventForm.toInt()
                                    ) {
                                    }
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
                                .foregroundColor(.blue)
                                .font(buttonFont)
                            }
                        )

                        MySpacerSize(width: 8)
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
                            Text(state.newTemplateText)
                                .padding(.vertical, buttonVPadding)
                                .padding(.horizontal, buttonHPadding)
                                .foregroundColor(.blue)
                                .font(buttonFont)
                        }
                    )

                    MySpacerSize(width: spaceAround)
                }
            }
            .padding(.top, paddingTop)
        }
    }
}
