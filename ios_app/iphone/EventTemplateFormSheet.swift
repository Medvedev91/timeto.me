import SwiftUI
import shared

struct EventTemplateFormSheet: View {

    @Binding private var isPresented: Bool

    @State private var vm: EventTemplateFormSheetVM

    @EnvironmentObject private var nativeSheet: NativeSheet

    @State private var scroll = 0

    init(
            isPresented: Binding<Bool>,
            eventTemplateDB: EventTemplateDB?
    ) {
        _isPresented = isPresented
        _vm = State(initialValue: EventTemplateFormSheetVM(
                eventTemplateDB: eventTemplateDB
        ))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Sheet__HeaderView(
                    title: state.headerTitle,
                    scrollToHeader: scroll,
                    bgColor: c.sheetBg
            )

            ScrollViewWithVListener(showsIndicators: false, vScroll: $scroll) {

            }

            Sheet__BottomViewDefault(
                    primaryText: state.doneText,
                    primaryAction: {
                        vm.save {
                            isPresented = false
                        }
                    },
                    secondaryText: "Cancel",
                    secondaryAction: {
                        isPresented = false
                    },
                    topContent: { EmptyView() },
                    startContent: { EmptyView() }
            )
        }
    }
}
