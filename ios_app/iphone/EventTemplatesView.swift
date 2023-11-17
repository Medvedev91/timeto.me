import SwiftUI
import shared

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

                        MySpacerSize(width: 8)

                        Button(
                                action: {
                                    // In onTapGesture()/onLongPressGesture()
                                },
                                label: {
                                    Text(templateUI.text)
                                            .padding(.vertical, 6)
                                            .padding(.horizontal, 11)
                                            .background(Capsule(style: .circular).fill(.blue))
                                            /// Ordering is important
                                            .onTapGesture {
                                                nativeSheet.EventFormSheet__show(
                                                        editedEvent: nil,
                                                        defText: templateUI.templateDB.text,
                                                        defTime: templateUI.timeForEventForm.toInt()
                                                ) {}
                                            }
                                            .onLongPressGesture(minimumDuration: 0.1) {
                                            }
                                            //////
                                            .foregroundColor(.white)
                                            .font(.system(size: 14, weight: .semibold))
                                }
                        )
                    }

                    MySpacerSize(width: spaceAround)
                }
            }
                    .padding(.top, paddingTop)
        }
    }
}
