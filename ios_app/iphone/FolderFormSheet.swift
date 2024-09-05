import SwiftUI
import shared

struct FolderFormSheet: View {

    @State private var vm: FolderFormSheetVm
    @Binding private var isPresented: Bool
    @State private var sheetHeaderScroll = 0

    init(
            isPresented: Binding<Bool>,
            folder: TaskFolderDb?
    ) {
        _isPresented = isPresented
        vm = FolderFormSheetVm(folder: folder)
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: state.headerDoneText,
                    isDoneEnabled: state.isHeaderDoneEnabled,
                    scrollToHeader: sheetHeaderScroll
            ) {
                vm.save {
                    isPresented = false
                }
            }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack(spacing: 0) {

                    VStack(spacing: 0) {

                        MyListView__HeaderView(title: state.inputNameHeader)
                                .padding(.top, 20)

                        MyListView__Padding__HeaderSection()

                        MyListView__ItemView(
                                isFirst: true,
                                isLast: true
                        ) {

                            MyListView__ItemView__TextInputView(
                                    text: state.inputNameValue,
                                    placeholder: state.inputNamePlaceholder,
                                    isAutofocus: false,
                                    onValueChanged: { newValue in
                                        vm.setInputNameValue(name: newValue)
                                    }
                            )
                        }

                        if let folder = vm.folder, !folder.isToday {

                            MyListView__Padding__SectionSection()

                            MyListView__ItemView(
                                    isFirst: true,
                                    isLast: true
                            ) {

                                MyListView__ItemView__ActionView(
                                        text: state.deleteFolderText
                                ) {
                                    vm.delete {
                                        isPresented = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
                .background(c.sheetBg)
    }
}
