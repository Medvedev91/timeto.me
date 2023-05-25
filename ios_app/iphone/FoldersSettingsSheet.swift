import SwiftUI
import shared

struct FoldersSettingsSheet: View {

    @Binding var isPresented: Bool

    @State private var vm = FoldersSettingsVM()
    @State private var sheetHeaderScroll = 0

    @State private var isAddSheetPresented = false

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: nil,
                    isDoneEnabled: false,
                    scrollToHeader: sheetHeaderScroll,
                    cancelText: "Back"
            ) {
                isPresented = false
            }

            ZStack {}.frame(height: 20)

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack(spacing: 0) {

                    let folders = state.folders
                    ForEach(folders, id: \.id) { folder in
                        let isFirst = folders.first == folder
                        MyListView__ItemView(
                                isFirst: isFirst,
                                isLast: folders.last == folder,
                                withTopDivider: !isFirst
                        ) {
                            FoldersSettingsSheet__FolderItem(
                                    vm: vm,
                                    folder: folder
                            )
                        }
                    }

                    MyListView__Padding__SectionSection()

                    MyListView__ItemView(
                            isFirst: true,
                            isLast: true
                    ) {
                        MyListView__ItemView__ButtonView(
                                text: "New Folder"
                        ) {
                            isAddSheetPresented = true
                        }
                                .sheetEnv(isPresented: $isAddSheetPresented) {
                                    FolderFormSheet(
                                            isPresented: $isAddSheetPresented,
                                            folder: nil
                                    )
                                }
                    }

                    if let tmrwButtonUI = state.tmrwButtonUI {

                        MyListView__Padding__SectionSection()

                        MyListView__ItemView(
                                isFirst: true,
                                isLast: true
                        ) {
                            MyListView__ItemView__ButtonView(
                                    text: tmrwButtonUI.text
                            ) {
                                tmrwButtonUI.add()
                            }
                        }
                    }
                }
            }
        }
                .background(Color(.mySheetFormBg))
    }
}

private struct FoldersSettingsSheet__FolderItem: View {

    let vm: FoldersSettingsVM
    let folder: TaskFolderModel

    @State private var isEditPresented = false

    var body: some View {

        MyListView__ItemView__ButtonView(
                text: folder.name,
                rightView: AnyView(
                        Button(
                                action: {
                                    vm.sortUp(folder: folder)
                                },
                                label: {
                                    Image(systemName: "arrow.up")
                                            .font(.system(size: 14))
                                            .foregroundColor(.blue)
                                }
                        )
                                .buttonStyle(.plain)
                                .padding(.trailing, MyListView.PADDING_INNER_HORIZONTAL)
                )
        ) {
            isEditPresented = true
        }
                .sheetEnv(isPresented: $isEditPresented) {
                    FolderFormSheet(
                            isPresented: $isEditPresented,
                            folder: folder
                    )
                }
    }
}
